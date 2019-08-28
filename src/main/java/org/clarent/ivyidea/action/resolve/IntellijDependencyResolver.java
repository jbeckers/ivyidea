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
import java.util.Set;
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
import org.clarent.ivyidea.facet.IvyIdeaFacetType;
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.model.dependency.ExternalDependency;
import org.clarent.ivyidea.model.dependency.ExternalJarDependency;
import org.clarent.ivyidea.model.dependency.ExternalJavaDocDependency;
import org.clarent.ivyidea.model.dependency.ExternalSourceDependency;
import org.clarent.ivyidea.model.dependency.InternalDependency;
import org.clarent.ivyidea.model.dependency.ResolvedDependency;
import org.clarent.ivyidea.settings.IvyIdeaProjectStateComponent;
import org.clarent.ivyidea.util.DependencyCategory;
import org.clarent.ivyidea.util.IvyUtil;
import org.clarent.ivyidea.util.exception.IvyFileReadException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Wraps the actual resolve process and manages that it is done with the right locking level for
 * IntelliJ.
 *
 * @author Guy Mahieu
 */
public class IntellijDependencyResolver {

  private static final Logger LOGGER =
      Logger.getInstance("#org.clarent.ivyidea.action.resolve.IntellijDependencyResolver");

  private final IvyManager ivyManager;
  private final Module intellijModule;
  private final List<ResolvedDependency> dependencies = new ArrayList<>();
  private final List<ResolveProblem> problems = new ArrayList<>();

  IntellijDependencyResolver(final IvyManager manager, final Module module) {
    this.ivyManager = manager;
    this.intellijModule = module;
  }

  Module getIntellijModule() {
    return intellijModule;
  }

  List<ResolveProblem> getProblems() {
    return Collections.unmodifiableList(problems);
  }

  List<ResolvedDependency> getDependencies() {
    return Collections.unmodifiableList(dependencies);
  }

  public Try<Void> resolve() {
    final DependencyResolver dependencyResolver = new DependencyResolver();
    final Try<Void> resolve = dependencyResolver.resolve(ivyManager, intellijModule);
    dependencies.addAll(dependencyResolver.getResolvedDependencies());
    problems.addAll(dependencyResolver.getResolveProblems());
    return resolve;
  }

  /**
   * @author Guy Mahieu
   */
  private static final class DependencyResolver {

    @NotNull
    private final Map<ModuleId, Module> ivyToIntellijModuleMap = new HashMap<>();

    private final List<ResolveProblem> resolveProblems;
    private final List<ResolvedDependency> resolvedDependencies;

    private DependencyResolver() {
      resolveProblems = new ArrayList<>();
      resolvedDependencies = new ArrayList<>();
    }

    private static boolean isSource(final Project project, final Artifact artifact) {
      return DependencyCategory.Sources == DependencyCategory.determineCategory(project, artifact);
    }

    private static boolean isJavadoc(final Project project, final Artifact artifact) {
      return DependencyCategory.Javadoc == DependencyCategory.determineCategory(project, artifact);
    }

    List<ResolveProblem> getResolveProblems() {
      return Collections.unmodifiableList(resolveProblems);
    }

    List<ResolvedDependency> getResolvedDependencies() {
      return Collections.unmodifiableList(resolvedDependencies);
    }

    public Try<Void> resolve(final IvyManager ivyManager, final Module module) {
      final Try<File> ivyFile = IvyUtil.getIvyFile(module);
      if (!ivyFile.isSuccess()) {
        return Try.failure(new IvyFileReadException(null, module.getName(), null));
      }
      ivyManager
          .getIvy(module)
          .andThenTry(
              ivy -> {
                final ResolveOptions options = new ResolveOptions();
                module
                    .getProject()
                    .getComponent(IvyIdeaProjectStateComponent.class)
                    .getState()
                    .updateResolveOptions(options);
                final Set<String> configsToResolve;
                final IvyIdeaFacetConfiguration moduleConfiguration =
                    IvyIdeaFacetConfiguration.getInstance(module);
                if (moduleConfiguration != null
                    && moduleConfiguration.isOnlyResolveSelectedConfigs()
                    && moduleConfiguration.getConfigsToResolve() != null) {
                  configsToResolve =
                      Collections.unmodifiableSet(moduleConfiguration.getConfigsToResolve());
                } else {
                  configsToResolve = Collections.emptySet();
                }
                if (!configsToResolve.isEmpty()) {
                  options.setConfs(configsToResolve.toArray(new String[0]));
                }
                extractDependencies(
                    ivy,
                    ivy.resolve(ivyFile.get().toURI().toURL(), options),
                    module,
                    ivyManager,
                    module.getProject());
              });
      return Try.success(null);
    }

    @Contract(pure = true)
    boolean isIntellijModule(final ModuleId ivyModule) {
      return ivyToIntellijModuleMap.containsKey(ivyModule);
    }

    Module getIntellijModule(final ModuleId ivyModule) {
      return ivyToIntellijModuleMap.get(ivyModule);
    }

    // TODO: This method performs way too much tasks -- refactor it!
    void extractDependencies(
        final Ivy ivy,
        final ResolveReport resolveReport,
        final Module module,
        final IvyManager ivyManager,
        final Project project) {
      final Try<ModuleDescriptor> moduleDescriptor =
          ivyManager
              .getModuleDescriptor(module)
              .onSuccess(
                  descriptor ->
                      Arrays.stream(ModuleManager.getInstance(module.getProject()).getModules())
                          .filter(IvyIdeaFacetType::isIvyModule)
                          .filter(intellijModule -> !module.equals(intellijModule))
                          .forEach(
                              intellijModule -> {
                                for (final DependencyDescriptor ivyDependency :
                                    descriptor.getDependencies()) {
                                  ModuleId dependencyModuleId = null;
                                  if (!ivyToIntellijModuleMap.containsValue(intellijModule)) {
                                    ivyManager
                                        .getModuleDescriptor(intellijModule)
                                        .onSuccess(
                                            moduleDescriptor1 ->
                                                ivyToIntellijModuleMap.put(
                                                    moduleDescriptor1
                                                        .getModuleRevisionId()
                                                        .getModuleId(),
                                                    intellijModule));
                                  }
                                  for (final Entry<ModuleId, Module> entry :
                                      ivyToIntellijModuleMap.entrySet()) {
                                    if (intellijModule.equals(entry.getValue())) {
                                      dependencyModuleId = entry.getKey();
                                      break;
                                    }
                                  }
                                  if (ivyDependency.getDependencyId().equals(dependencyModuleId)) {
                                    LOGGER.info(
                                        "LOG00130: Recognized dependency "
                                            + ivyDependency
                                            + " as intellij module '"
                                            + intellijModule.getName()
                                            + "' in this project!");
                                    ivyToIntellijModuleMap.put(dependencyModuleId, intellijModule);
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
                if (isIntellijModule(dependency.getModuleId())) {
                  resolvedDependencies.add(
                      new InternalDependency(getIntellijModule(dependency.getModuleId())));
                } else {
                  for (final ArtifactDownloadReport artifactDownloadReport :
                      resolveReport
                          .getConfigurationReport(resolvedConfiguration)
                          .getDownloadReports(dependency)) {
                    final Artifact artifact = artifactDownloadReport.getArtifact();
                    final File artifactFile = artifactDownloadReport.getLocalFile();
                    addExternalDependency(artifact, artifactFile, resolvedConfiguration, project);
                  }

                  // If activated manually download any missing javadoc or source dependencies,
                  // in case they weren't selected by the Ivy configuration.
                  // This means that dependencies in ivy.xml don't need to explicitly include
                  // configurations
                  // for javadoc or sources, just to ensure that the plugin can see them. The
                  // plugin
                  // will
                  // get all javadocs and sources it can find for each dependency.
                  final boolean attachSources =
                      project
                          .getComponent(IvyIdeaProjectStateComponent.class)
                          .getState()
                          .isAlwaysAttachSources();
                  final boolean attachJavadocs =
                      project
                          .getComponent(IvyIdeaProjectStateComponent.class)
                          .getState()
                          .isAlwaysAttachJavadocs();
                  if (attachSources || attachJavadocs) {
                    for (final Artifact artifact :
                        resolveReport
                            .getConfigurationReport(resolvedConfiguration)
                            .getDependency(dependency)
                            .getDescriptor()
                            .getAllArtifacts()) {
                      // TODO: if sources are found, don't bother attaching javadoc?
                      // That way, IDEA will generate the javadoc and resolve links to other
                      // javadocs
                      if ((attachSources && isSource(project, artifact))
                          || (attachJavadocs && isJavadoc(project, artifact))) {
                        if (resolveReport.getArtifacts().contains(artifact)) {
                          continue; // already resolved, ignore.
                        }

                        // try to download
                        addExternalDependency(
                            artifact,
                            ivy.getResolveEngine()
                                .download(artifact, new DownloadOptions())
                                .getLocalFile(),
                            resolvedConfiguration,
                            project);
                      }
                    }
                  }
                }
              }
            });
      }
    }

    private void addExternalDependency(
        final Artifact artifact,
        final File artifactFile,
        final String resolvedConfiguration,
        final Project project) {
      ExternalDependency externalDependency = null;
      final DependencyCategory category = DependencyCategory.determineCategory(project, artifact);
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
      } else if (externalDependency.isMissing()) {
        resolveProblems.add(
            new ResolveProblem(
                artifact.getModuleRevisionId().toString(),
                "File not found: " + externalDependency.getLocalFile().getAbsolutePath()));
      } else {
        resolvedDependencies.add(externalDependency);
      }
    }

    private void registerProblems(final ConfigurationResolveReport configurationReport) {
      for (final IvyNode unresolvedDependency : configurationReport.getUnresolvedDependencies()) {
        if (isIntellijModule(unresolvedDependency.getModuleId())) {
          // centralize  this!
          resolvedDependencies.add(
              new InternalDependency(getIntellijModule(unresolvedDependency.getModuleId())));
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
