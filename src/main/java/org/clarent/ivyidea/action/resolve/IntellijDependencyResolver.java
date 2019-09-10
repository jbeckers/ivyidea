/*
 * Copyright 2010 Guy Mahieu
 * Copyright 2011 Maarten Coene
 * Copyright 2019 Joachim Beckers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.clarent.ivyidea.action.resolve;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import io.vavr.control.Try;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ConfigurationResolveReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.resolve.IvyNode;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.clarent.ivyidea.IvyIdeaConstants;
import org.clarent.ivyidea.model.dependency.DependencyCategory;
import org.clarent.ivyidea.model.dependency.ExternalDependency;
import org.clarent.ivyidea.model.dependency.ExternalJarDependency;
import org.clarent.ivyidea.model.dependency.ExternalJavaDocDependency;
import org.clarent.ivyidea.model.dependency.ExternalSourceDependency;
import org.clarent.ivyidea.model.dependency.InternalDependency;
import org.clarent.ivyidea.model.dependency.ResolvedDependency;
import org.clarent.ivyidea.settings.IvyIdeaProjectState;
import org.clarent.ivyidea.util.IvyIdeaFacetUtil;
import org.clarent.ivyidea.util.exception.IvyFileReadException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wraps the actual resolve process and manages that it is done with the right locking level for
 * IntelliJ.
 *
 * @author Guy Mahieu
 */
class IntellijDependencyResolver {

  @NotNull
  private static final Logger LOGGER =
      Logger.getInstance("#org.clarent.ivyidea.action.resolve.IntellijDependencyResolver");

  @NotNull
  private final IvyManager ivyManager;
  @NotNull
  private final Module intellijModule;
  @NotNull
  private final List<ResolvedDependency> dependencies = new ArrayList<>();
  @NotNull
  private final List<ResolveProblem> problems = new ArrayList<>();

  IntellijDependencyResolver(@NotNull final IvyManager manager, @NotNull final Module module) {
    this.ivyManager = manager;
    this.intellijModule = module;
  }

  @NotNull
  Module getIntellijModule() {
    return intellijModule;
  }

  @NotNull
  List<ResolveProblem> getProblems() {
    return Collections.unmodifiableList(problems);
  }

  @NotNull
  List<ResolvedDependency> getDependencies() {
    return Collections.unmodifiableList(dependencies);
  }

  public void resolve() {
    final DependencyResolver dependencyResolver = new DependencyResolver();
    dependencyResolver.resolve(ivyManager, intellijModule);
    dependencies.addAll(dependencyResolver.getResolvedDependencies());
    problems.addAll(dependencyResolver.getResolveProblems());
  }

  /**
   * @author Guy Mahieu
   */
  private static final class DependencyResolver {

    @NotNull
    private final Map<ModuleId, Module> ivyToIntellijModuleMap = new HashMap<>();
    @NotNull
    private final Map<Module, ModuleId> intellijToIvyModuleMap = new HashMap<>();

    @NotNull
    private final List<ResolveProblem> resolveProblems = new ArrayList<>();
    @NotNull
    private final List<ResolvedDependency> resolvedDependencies = new ArrayList<>();

    private DependencyResolver() {}

    private static boolean isSource(
        @NotNull final Project project, @NotNull final Artifact artifact) {
      return DependencyCategory.Sources
          == IvyIdeaProjectState.getInstance(project)
          .getDependencyCategoryManager()
          .getCategoryForType(artifact.getType());
    }

    private static boolean isJavadoc(
        @NotNull final Project project, @NotNull final Artifact artifact) {
      return DependencyCategory.Javadoc
          == IvyIdeaProjectState.getInstance(project)
          .getDependencyCategoryManager()
          .getCategoryForType(artifact.getType());
    }

    @NotNull
    private static Stream<Module> getOtherIvyModules(@NotNull final Module module) {
      return Arrays.stream(ModuleManager.getInstance(module.getProject()).getModules())
          .filter(IvyIdeaFacetUtil::isIvyModule)
          .filter(intellijModule -> !module.equals(intellijModule));
    }

    List<ResolveProblem> getResolveProblems() {
      return Collections.unmodifiableList(resolveProblems);
    }

    List<ResolvedDependency> getResolvedDependencies() {
      return Collections.unmodifiableList(resolvedDependencies);
    }

    @NotNull
    Try<Void> resolve(@NotNull final IvyManager ivyManager, @NotNull final Module module) {
      final Try<File> ivyFile = IvyIdeaFacetUtil.getIvyFile(module);
      if (!ivyFile.isSuccess()) {
        return Try.failure(new IvyFileReadException(null, module.getName(), null));
      }
      ivyManager
          .getIvy(module)
          .andThenTry(
              ivy -> {
                final ResolveOptions options = new ResolveOptions();
                final IvyIdeaProjectState state =
                    IvyIdeaProjectState.getInstance(module.getProject());
                options.setValidate(state.isValidateIvyFiles());
                options.setTransitive(state.isResolveTransitively());
                options.setUseCacheOnly(state.isResolveCacheOnly());
                IvyIdeaFacetUtil.getConfiguration(module)
                    .filter(
                        moduleConfiguration ->
                            moduleConfiguration.getState().isOnlyResolveSelectedConfigs()
                                && !moduleConfiguration.getState().getConfigsToResolve().isEmpty())
                    .map(
                        moduleConfiguration -> moduleConfiguration.getState().getConfigsToResolve())
                    .forEach(
                        configsToResolve ->
                            options.setConfs(
                                configsToResolve.toArray(
                                    IvyIdeaConstants.ZERO_LENGTH_STRING_ARRAY)));

                extractDependencies(
                    ivy,
                    ivy.resolve(ivyFile.get().toURI().toURL(), options),
                    module,
                    ivyManager,
                    module.getProject());
              });
      return Try.success(null);
    }

    // TODO: This method performs way too much tasks -- refactor it!
    void extractDependencies(
        @NotNull final Ivy ivy,
        @NotNull final ResolveReport resolveReport,
        @NotNull final Module module,
        @NotNull final IvyManager ivyManager,
        @NotNull final Project project) {
      final Try<ModuleDescriptor> moduleDescriptor =
          ivyManager
              .getModuleDescriptor(module)
              .onSuccess(
                  descriptor ->
                      getOtherIvyModules(module)
                          .forEach(
                              intellijModule -> {
                                for (final DependencyDescriptor ivyDependency :
                                    descriptor.getDependencies()) {
                                  ModuleId dependencyModuleId = null;
                                  if (!intellijToIvyModuleMap.containsKey(intellijModule)) {
                                    ivyManager
                                        .getModuleDescriptor(intellijModule)
                                        .map(
                                            moduleDescriptor1 ->
                                                moduleDescriptor1
                                                    .getModuleRevisionId()
                                                    .getModuleId())
                                        .onSuccess(moduleId -> putModule(intellijModule, moduleId));
                                  }
                                  for (final Entry<ModuleId, Module> entry :
                                      ivyToIntellijModuleMap.entrySet()) {
                                    if (intellijModule.equals(entry.getValue())) {
                                      dependencyModuleId = entry.getKey();
                                      break;
                                    }
                                  }
                                  if (dependencyModuleId != null
                                      && ivyDependency
                                      .getDependencyId()
                                      .equals(dependencyModuleId)) {
                                    LOGGER.info(
                                        "LOG00130: Recognized dependency "
                                            + ivyDependency
                                            + " as intellij module '"
                                            + intellijModule.getName()
                                            + "' in this project!");
                                    putModule(intellijModule, dependencyModuleId);
                                    break;
                                  }
                                }
                              }));
      for (final String resolvedConfiguration : resolveReport.getConfigurations()) {
        moduleDescriptor.onSuccess(
            descriptor -> {
              // TODO: Refactor this a bit
              registerProblems(resolveReport.getConfigurationReport(resolvedConfiguration));

              for (final ModuleRevisionId dependency :
                  resolveReport
                      .getConfigurationReport(resolvedConfiguration)
                      .getModuleRevisionIds()) {
                if (ivyToIntellijModuleMap.containsKey(dependency.getModuleId())) {
                  resolvedDependencies.add(
                      new InternalDependency(ivyToIntellijModuleMap.get(dependency.getModuleId())));
                } else {
                  for (final ArtifactDownloadReport artifactDownloadReport :
                      resolveReport
                          .getConfigurationReport(resolvedConfiguration)
                          .getDownloadReports(dependency)) {
                    addExternalDependency(
                        artifactDownloadReport.getArtifact(),
                        artifactDownloadReport.getLocalFile(),
                        resolvedConfiguration,
                        project);
                  }

                  // If activated manually download any missing javadoc or source dependencies,
                  // in case they weren't selected by the Ivy configuration.
                  // This means that dependencies in ivy.xml don't need to explicitly include
                  // configurations
                  // for javadoc or sources, just to ensure that the plugin can see them. The
                  // plugin
                  // will
                  // get all javadocs and sources it can find for each dependency.
                  final IvyIdeaProjectState state = IvyIdeaProjectState.getInstance(project);
                  if (state.isAlwaysAttachSources() || state.isAlwaysAttachJavadocs()) {
                    Arrays.stream(
                        resolveReport
                            .getConfigurationReport(resolvedConfiguration)
                            .getDependency(dependency)
                            .getDescriptor()
                            .getAllArtifacts())
                        .filter(
                            artifact ->
                                ((state.isAlwaysAttachSources() && isSource(project, artifact))
                                    || (state.isAlwaysAttachJavadocs()
                                    && isJavadoc(project, artifact)))
                                    && !resolveReport.getArtifacts().contains(artifact))
                        .forEach(
                            artifact -> {
                              // TODO: if sources are found, don't bother attaching javadoc?
                              //       That way, IDEA will generate the javadoc and resolve links to
                              //       other javadocs
                              addExternalDependency(
                                  artifact,
                                  ivy.getResolveEngine()
                                      .download(artifact, new DownloadOptions())
                                      .getLocalFile(),
                                  resolvedConfiguration,
                                  project);
                            });
                  }
                }
              }
            });
      }
    }

    private void putModule(@NotNull final Module intellijModule, @NotNull final ModuleId moduleId) {
      ivyToIntellijModuleMap.put(moduleId, intellijModule);
      intellijToIvyModuleMap.put(intellijModule, moduleId);
    }

    private void addExternalDependency(
        @NotNull final Artifact artifact,
        @Nullable final File artifactFile,
        @NotNull final String resolvedConfiguration,
        @NotNull final Project project) {
      ExternalDependency externalDependency = null;
      final DependencyCategory category =
          IvyIdeaProjectState.getInstance(project)
              .getDependencyCategoryManager()
              .getCategoryForType(artifact.getType());
      if (category != null) {
        switch (category) {
          case Classes:
            externalDependency =
                new ExternalJarDependency(artifact, artifactFile, resolvedConfiguration);
            break;
          case Sources:
            externalDependency =
                new ExternalSourceDependency(artifact, artifactFile, resolvedConfiguration);
            break;
          case Javadoc:
            externalDependency =
                new ExternalJavaDocDependency(artifact, artifactFile, resolvedConfiguration);
            break;
        }
      }
      if (externalDependency == null) {
        resolveProblems.add(
            new ResolveProblem(
                artifact.getModuleRevisionId().toString(),
                "Unrecognized artifact type: "
                    + artifact.getType()
                    + ", will not add this as a dependency in IntelliJ.",
                null));
        LOGGER.warn(
            "LOG00110: Artifact of unrecognized type "
                + artifact.getType()
                + " found, *not* adding as a dependency.");
      } else {
        final File localFile = externalDependency.getLocalFile();
        if (localFile != null && !new File(localFile.getAbsolutePath()).exists()) {
          resolveProblems.add(
              new ResolveProblem(
                  artifact.getModuleRevisionId().toString(),
                  "File not found: " + localFile.getAbsolutePath()));
        } else {
          resolvedDependencies.add(externalDependency);
        }
      }
    }

    private void registerProblems(@NotNull final ConfigurationResolveReport configurationReport) {
      for (final IvyNode unresolvedDependency : configurationReport.getUnresolvedDependencies()) {
        if (ivyToIntellijModuleMap.containsKey(unresolvedDependency.getModuleId())) {
          // centralize  this!
          resolvedDependencies.add(
              new InternalDependency(
                  ivyToIntellijModuleMap.get(unresolvedDependency.getModuleId())));
        } else {
          resolveProblems.add(
              new ResolveProblem(
                  unresolvedDependency.getId().toString(),
                  unresolvedDependency.getProblemMessage(),
                  unresolvedDependency.getProblem()));
          LOGGER.info(
              "LOG00190: DEPENDENCY PROBLEM: "
                  + unresolvedDependency.getId()
                  + ": "
                  + unresolvedDependency.getProblemMessage());
        }
      }
    }
  }
}
