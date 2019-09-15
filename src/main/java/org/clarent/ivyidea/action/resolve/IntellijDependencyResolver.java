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
import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.control.Either;
import io.vavr.control.Try;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.clarent.ivyidea.IvyIdeaConstants;
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration;
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
  private final Map<ModuleId, Module> ivyToIntellijModuleMap = new LinkedHashMap<>();
  @NotNull
  private final Map<Module, ModuleId> intellijToIvyModuleMap = new LinkedHashMap<>();

  private static boolean isSource(
      @NotNull final Project project, @NotNull final Artifact artifact) {
    return DependencyCategory.Sources
        == IvyIdeaProjectState.getInstance(project)
        .getDependencyCategoryManager()
        .getCategoryForType(artifact.getType())
        .getOrNull();
  }

  private static boolean isJavadoc(
      @NotNull final Project project, @NotNull final Artifact artifact) {
    return DependencyCategory.Javadoc
        == IvyIdeaProjectState.getInstance(project)
        .getDependencyCategoryManager()
        .getCategoryForType(artifact.getType())
        .getOrNull();
  }

  @NotNull
  private static Stream<Module> getOtherIntelliJModules(@NotNull final Module module) {
    return Arrays.stream(ModuleManager.getInstance(module.getProject()).getModules())
        .filter(IvyIdeaFacetUtil::isIvyModule)
        .filter(intellijModule -> !module.equals(intellijModule));
  }

  private static void setConfigsToResolve(
      @NotNull final Module module, @NotNull final ResolveOptions options) {
    IvyIdeaFacetUtil.getConfiguration(module)
        .map(IvyIdeaFacetConfiguration::getState)
        .filter(
            moduleConfiguration ->
                moduleConfiguration.isOnlyResolveSelectedConfigs()
                    && !moduleConfiguration.getConfigsToResolve().isEmpty())
        .map(IvyIdeaFacetConfiguration::getConfigsToResolve)
        .forEach(
            configsToResolve ->
                options.setConfs(
                    configsToResolve.toArray(IvyIdeaConstants.ZERO_LENGTH_STRING_ARRAY)));
  }

  @NotNull
  private static ResolveOptions getResolveOptions(@NotNull final Module module) {
    final IvyIdeaProjectState state = IvyIdeaProjectState.getInstance(module.getProject());
    final ResolveOptions options = new ResolveOptions();
    options.setValidate(state.isValidateIvyFiles());
    options.setTransitive(state.isResolveTransitively());
    options.setUseCacheOnly(state.isResolveCacheOnly());
    setConfigsToResolve(module, options);
    return options;
  }

  @NotNull
  private static Either<ResolveProblem, ExternalDependency> processArtifact(
      @NotNull final Artifact artifact,
      @Nullable final File artifactFile,
      @NotNull final String resolvedConfiguration,
      @NotNull final IvyIdeaProjectState projectState) {
    return projectState
        .getDependencyCategoryManager()
        .getCategoryForType(artifact.getType())
        .map(
            dependencyCategory -> {
              switch (dependencyCategory) {
                case Classes:
                  return new ExternalJarDependency(artifact, artifactFile, resolvedConfiguration);
                case Sources:
                  return new ExternalSourceDependency(
                      artifact, artifactFile, resolvedConfiguration);
                case Javadoc:
                  return new ExternalJavaDocDependency(
                      artifact, artifactFile, resolvedConfiguration);
              }
              throw new IllegalStateException("Unknown DependencyCategory!");
            })
        .<Either<ResolveProblem, ExternalDependency>>map(
            externalDependency -> {
              final File localFile = externalDependency.getLocalFile();
              if (localFile != null && !new File(localFile.getAbsolutePath()).exists()) {
                return Either.left(
                    new ResolveProblem(
                        artifact.getModuleRevisionId().toString(),
                        "File not found: " + localFile.getAbsolutePath()));
              } else {
                return Either.right(externalDependency);
              }
            })
        .getOrElse(
            () -> {
              LOGGER.warn(
                  "LOG00110: Artifact of unrecognized type "
                      + artifact.getType()
                      + " found, *not* adding as a dependency.");
              return Either.left(
                  new ResolveProblem(
                      artifact.getModuleRevisionId().toString(),
                      "Unrecognized artifact type: "
                          + artifact.getType()
                          + ", will not add this as a dependency in IntelliJ.",
                      null));
            });
  }

  @NotNull
  private static URL getIvySource(@NotNull final File ivyFile) throws MalformedURLException {
    return ivyFile.toURI().toURL();
  }

  @NotNull
  public Try<Tuple3<Module, List<ResolvedDependency>, List<ResolveProblem>>> resolve(
      @NotNull final Module module, @NotNull final IvyManager ivyManager) {
    final Try<String> ivyFileName = IvyIdeaFacetUtil.getIvyFile(module);
    if (ivyFileName.isFailure()) {
      return Try.failure(ivyFileName.getCause());
    }
    final Try<File> ivyFile = ivyFileName.mapTry(File::new);
    if (ivyFile.isFailure()) {
      return Try.failure(
          new IvyFileReadException(ivyFileName.get(), module.getName(), ivyFile.getCause()));
    }

    ivyManager
        .getModuleDescriptor(module)
        .map(ModuleDescriptor::getDependencies)
        .forEach(moduleDependencies -> cacheModules(module, moduleDependencies, ivyManager));

    return ivyManager
        .getIvy(module)
        .mapTry(
            ivy ->
                Tuple.of(ivy, ivy.resolve(getIvySource(ivyFile.get()), getResolveOptions(module))))
        .mapTry(
            tuple -> {
              @NotNull final List<ResolveProblem> resolveProblems = new ArrayList<>();
              @NotNull final List<ResolvedDependency> resolvedDependencies = new ArrayList<>();
              for (final String resolvedConfiguration : tuple._2().getConfigurations()) {
                Arrays.stream(
                    tuple
                        ._2()
                        .getConfigurationReport(resolvedConfiguration)
                        .getUnresolvedDependencies())
                    .forEach(
                        unresolvedDependency -> {
                          if (ivyToIntellijModuleMap.containsKey(
                              unresolvedDependency.getModuleId())) {
                            // centralize  this!
                            resolvedDependencies.add(
                                new InternalDependency(
                                    ivyToIntellijModuleMap.get(
                                        unresolvedDependency.getModuleId())));
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
                        });

                for (final ModuleRevisionId dependency :
                    tuple
                        ._2()
                        .getConfigurationReport(resolvedConfiguration)
                        .getModuleRevisionIds()) {
                  if (ivyToIntellijModuleMap.containsKey(dependency.getModuleId())) {
                    resolvedDependencies.add(
                        new InternalDependency(
                            ivyToIntellijModuleMap.get(dependency.getModuleId())));
                  } else {
                    final IvyIdeaProjectState projectState =
                        IvyIdeaProjectState.getInstance(module.getProject());
                    Arrays.stream(
                        tuple
                            ._2()
                            .getConfigurationReport(resolvedConfiguration)
                            .getDownloadReports(dependency))
                        .forEach(
                            artifactDownloadReport ->
                                processArtifact(
                                    artifactDownloadReport.getArtifact(),
                                    artifactDownloadReport.getLocalFile(),
                                    resolvedConfiguration,
                                    projectState)
                                    .bimap(resolveProblems::add, resolvedDependencies::add));

                    /* If activated manually download any missing javadoc or source dependencies,
                     * in case they weren't selected by the Ivy configuration.
                     * This means that dependencies in ivy.xml don't need to explicitly include
                     * configurations for javadoc or sources, just to ensure that the plugin can
                     * see them.
                     * The plugin will get all javadocs and sources it can find for each dependency.
                     */
                    if (projectState.isAlwaysAttachSources()
                        || projectState.isAlwaysAttachJavadocs()) {
                      Arrays.stream(
                          tuple
                              ._2()
                              .getConfigurationReport(resolvedConfiguration)
                              .getDependency(dependency)
                              .getDescriptor()
                              .getAllArtifacts())
                          .filter(
                              artifact ->
                                  ((projectState.isAlwaysAttachSources()
                                      && isSource(module.getProject(), artifact))
                                      || (projectState.isAlwaysAttachJavadocs()
                                      && isJavadoc(module.getProject(), artifact)))
                                      && !tuple._2().getArtifacts().contains(artifact))
                          .forEach(
                              artifact -> {
                                /*
                                 * TODO: if sources are found, don't bother attaching javadoc?
                                 *       That way, IDEA will generate the javadoc and resolve links
                                 *       to other javadocs
                                 */
                                processArtifact(
                                    artifact,
                                    tuple
                                        ._1()
                                        .getResolveEngine()
                                        .download(artifact, new DownloadOptions())
                                        .getLocalFile(),
                                    resolvedConfiguration,
                                    IvyIdeaProjectState.getInstance(module.getProject()))
                                    .bimap(resolveProblems::add, resolvedDependencies::add);
                              });
                    }
                  }
                }
              }
              return Tuple.of(module, resolvedDependencies, resolveProblems);
            });
  }

  private void cacheModules(
      @NotNull final Module module,
      @NotNull final DependencyDescriptor[] ivyDependencies,
      @NotNull final IvyManager ivyManager) {
    // Loop over all other modules in the IntelliJ project
    getOtherIntelliJModules(module)
        .forEach(
            otherIntelliJModule -> {
              // If we haven't seen this other module yet, find its ivy id and cache it
              if (!intellijToIvyModuleMap.containsKey(otherIntelliJModule)) {
                ivyManager
                    .getModuleDescriptor(otherIntelliJModule)
                    .map(
                        otherModuleDescriptor ->
                            otherModuleDescriptor.getModuleRevisionId().getModuleId())
                    .onSuccess(
                        otherModuleId -> {
                          intellijToIvyModuleMap.put(otherIntelliJModule, otherModuleId);
                          ivyToIntellijModuleMap.put(otherModuleId, otherIntelliJModule);
                        });
              }
              // Now
              maybeUpdateCacheWithIvyDependency(otherIntelliJModule, ivyDependencies);
            });
  }

  private void maybeUpdateCacheWithIvyDependency(
      @NotNull final Module otherIntelliJModule,
      @NotNull final DependencyDescriptor[] ivyDependencies) {
    // Now loop over the ivy dependencies of the given module
    boolean keepLooking = true;
    for (int i = 0; i < ivyDependencies.length && keepLooking; i++) {
      final DependencyDescriptor ivyDependency = ivyDependencies[i];
      // Look at the cache to see if we can find the ivy dependency yet
      final ModuleId dependencyModuleId =
          intellijToIvyModuleMap.getOrDefault(otherIntelliJModule, null);
      if (dependencyModuleId != null
          && ivyDependency.getDependencyId().equals(dependencyModuleId)) {
        // Success, let's update our cache.
        LOGGER.info(
            "LOG00130: Recognized dependency "
                + ivyDependency
                + " as intellij module '"
                + otherIntelliJModule.getName()
                + "' in this project!");
        final ModuleId moduleId = ivyDependency.getDependencyId();
        ivyToIntellijModuleMap.put(moduleId, otherIntelliJModule);
        intellijToIvyModuleMap.put(otherIntelliJModule, moduleId);
        keepLooking = false;
      }
    }
  }
}
