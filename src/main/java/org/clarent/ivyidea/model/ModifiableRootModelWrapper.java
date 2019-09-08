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

package org.clarent.ivyidea.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.Library.ModifiableModel;
import com.intellij.openapi.roots.libraries.LibraryTable;
import java.io.Closeable;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.clarent.ivyidea.IvyIdeaConstants;
import org.clarent.ivyidea.model.dependency.ExternalDependency;
import org.clarent.ivyidea.model.dependency.ResolvedDependency;
import org.clarent.ivyidea.settings.IvyIdeaProjectState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ModifiableRootModelWrapper implements Closeable {

  @NotNull
  private static final Logger LOGGER =
      Logger.getInstance("#org.clarent.ivyidea.model.ModifiableRootModelWrapper");

  @NotNull
  private final ModifiableRootModel intellijModule;

  @NotNull
  private final ConcurrentMap<String, ModifiableModel> libraryModels = new ConcurrentHashMap<>();

  private ModifiableRootModelWrapper(@NotNull final ModifiableRootModel model) {
    this.intellijModule = model;
  }

  @NotNull
  @Contract("_ -> new")
  public static ModifiableRootModelWrapper forModule(@NotNull final Module module) {
    ModifiableRootModel modifiableModel = null;
    try {
      modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
      return new ModifiableRootModelWrapper(modifiableModel);
    } catch (final RuntimeException e) {
      if (modifiableModel != null) {
        modifiableModel.dispose();
      }
      throw e;
    }
  }

  @NotNull
  private static String getCreatedLibraryName(
      @NotNull final ModifiableRootModel model, final String configName) {
    final IvyIdeaProjectState state = IvyIdeaProjectState.getInstance(model.getProject());
    String libraryName = IvyIdeaConstants.RESOLVED_LIB_NAME_ROOT;
    if (state.isLibraryNameIncludesModule()) {
      libraryName += "-" + model.getModule().getName();
    }
    if (state.isLibraryNameIncludesConfiguration()) {
      libraryName += "-" + configName;
    }
    return libraryName;
  }

  public void updateDependencies(
      @NotNull final Iterable<? extends ResolvedDependency> resolvedDependencies) {
    for (final ResolvedDependency resolvedDependency : resolvedDependencies) {
      resolvedDependency.addTo(this);
    }
    for (final OrderRootType type : OrderRootType.getAllTypes()) {
      libraryModels.values().stream()
          .flatMap(libraryModel -> Arrays.stream(libraryModel.getUrls(type)))
          .filter(
              intellijDependency -> {
                for (final ResolvedDependency ivyDependency : resolvedDependencies) {
                  // TODO: We don't touch module to module dependencies here because we currently
                  //       can't determine if they were added by IvyIDEA or by the user
                  if (ivyDependency instanceof ExternalDependency
                      && ((ExternalDependency) ivyDependency)
                      .isSameDependency(intellijDependency)) {
                    return false;
                  }
                }
                return true;
              })
          .forEach(
              dependencyUrl -> {
                LOGGER.info(
                    "LOG00090: Removing no longer needed dependency of type "
                        + type
                        + ": "
                        + dependencyUrl);
                libraryModels
                    .values()
                    .forEach(modifiableModel -> modifiableModel.removeRoot(dependencyUrl, type));
              });
    }

    // remove resolved libraries that are no longer used
    final Collection<String> librariesInUse =
        StreamSupport.stream(resolvedDependencies.spliterator(), false)
            .filter(dependency -> dependency instanceof ExternalDependency)
            .map(ExternalDependency.class::cast)
            .map(
                dependency ->
                    getCreatedLibraryName(intellijModule, dependency.getConfigurationName()))
            .collect(Collectors.toSet());

    Arrays.stream(intellijModule.getModuleLibraryTable().getLibraries())
        .filter(
            library -> {
              final String libraryName = library.getName();
              return libraryName != null
                  && libraryName.startsWith(IvyIdeaConstants.RESOLVED_LIB_NAME_ROOT)
                  && !librariesInUse.contains(libraryName);
            })
        .forEach(library -> intellijModule.getModuleLibraryTable().removeLibrary(library));
  }

  @Override
  public void close() {
    for (final ModifiableModel model : libraryModels.values()) {
      if (model.isChanged()) {
        model.commit();
      } else {
        model.dispose();
      }
    }
    if (intellijModule.isChanged()) {
      intellijModule.commit();
    } else {
      intellijModule.dispose();
    }
  }

  public String getModuleName() {
    return intellijModule.getModule().getName();
  }

  public void addModuleDependency(@NotNull final Module module) {
    intellijModule.addModuleOrderEntry(module);
  }

  public void addExternalDependency(@NotNull final ExternalDependency externalDependency) {
    final String urlForLibraryRoot = externalDependency.getUrlForLibraryRoot();
    if (urlForLibraryRoot != null) {
      getModelForExternalDependency(externalDependency)
          .addRoot(urlForLibraryRoot, externalDependency.getType());
    }
  }

  public boolean alreadyHasDependencyOnModule(@NotNull final Module module) {
    return Arrays.stream(intellijModule.getModuleDependencies())
        .map(Module::getName)
        .anyMatch(dependency -> dependency.equals(module.getName()));
  }

  public boolean alreadyHasDependencyOnLibrary(
      @NotNull final ExternalDependency externalDependency) {
    return Arrays.stream(
        getModelForExternalDependency(externalDependency).getUrls(externalDependency.getType()))
        .anyMatch(externalDependency::isSameDependency);
  }

  @NotNull
  private ModifiableModel getModelForExternalDependency(
      @NotNull final ExternalDependency externalDependency) {
    final String resolvedConfiguration = externalDependency.getConfigurationName();
    final String libraryName =
        getCreatedLibraryName(
            intellijModule,
            resolvedConfiguration.trim().isEmpty() ? "default" : resolvedConfiguration);

    if (libraryModels.containsKey(libraryName)) {
      return libraryModels.get(libraryName);
    }

    final LibraryTable libraryTable = intellijModule.getModuleLibraryTable();
    final Library library = libraryTable.getLibraryByName(libraryName);
    if (library == null) {
      LOGGER.info(
          "LOG00170: Internal library not found for module "
              + intellijModule.getModule().getModuleFilePath()
              + ", creating with name "
              + libraryName
              + "...");
      libraryModels.putIfAbsent(
          libraryName, libraryTable.createLibrary(libraryName).getModifiableModel());
    } else {
      libraryModels.putIfAbsent(libraryName, library.getModifiableModel());
    }
    return libraryModels.get(libraryName);
  }
}
