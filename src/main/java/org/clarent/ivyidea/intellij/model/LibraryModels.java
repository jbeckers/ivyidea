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

package org.clarent.ivyidea.intellij.model;

import static java.util.Arrays.asList;

import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import org.clarent.ivyidea.config.IvyIdeaConfigHelper;
import org.clarent.ivyidea.resolve.dependency.ExternalDependency;

class LibraryModels implements Closeable {

  private static final Logger LOGGER = Logger.getLogger(LibraryModels.class.getName());

  private final ConcurrentMap<String, Library.ModifiableModel> libraryModels =
      new ConcurrentHashMap<>();

  private final ModifiableRootModel intellijModule;

  LibraryModels(final ModifiableRootModel intellijModule) {
    this.intellijModule = intellijModule;
  }

  public Library.ModifiableModel getForExternalDependency(
      final ExternalDependency externalDependency) {
    final String resolvedConfiguration = externalDependency.getConfigurationName();
    return getForConfiguration(
        resolvedConfiguration == null || resolvedConfiguration.trim().isEmpty() ? "default"
            : resolvedConfiguration);
  }

  private Library.ModifiableModel getForConfiguration(final String ivyConfiguration) {
    final String libraryName =
        IvyIdeaConfigHelper.getCreatedLibraryName(intellijModule, ivyConfiguration);
    if (!libraryModels.containsKey(libraryName)) {
      final Library.ModifiableModel libraryModel =
          getIvyIdeaLibrary(intellijModule, libraryName).getModifiableModel();
      libraryModels.putIfAbsent(libraryName, libraryModel);
    }
    return libraryModels.get(libraryName);
  }

  private static Library getIvyIdeaLibrary(
      final ModifiableRootModel modifiableRootModel, final String libraryName) {
    final LibraryTable libraryTable = modifiableRootModel.getModuleLibraryTable();
    final Library library = libraryTable.getLibraryByName(libraryName);
    if (library == null) {
      LOGGER.info(
          "Internal library not found for module "
              + modifiableRootModel.getModule().getModuleFilePath()
              + ", creating with name "
              + libraryName
              + "...");
      return libraryTable.createLibrary(libraryName);
    }
    return library;
  }

  public void removeDependency(final OrderRootType type, final String dependencyUrl) {
    LOGGER.info("Removing no longer needed dependency of type " + type + ": " + dependencyUrl);
    for (final Library.ModifiableModel libraryModel : libraryModels.values()) {
      libraryModel.removeRoot(dependencyUrl, type);
    }
  }

  public List<String> getIntellijDependencyUrlsForType(final OrderRootType type) {
    final List<String> intellijDependencies = new ArrayList<>();
    for (final Library.ModifiableModel libraryModel : libraryModels.values()) {
      final String[] libraryModelUrls = libraryModel.getUrls(type);
      intellijDependencies.addAll(asList(libraryModelUrls));
    }
    return intellijDependencies;
  }

  @Override
  public void close() {
    for (final Library.ModifiableModel libraryModel : libraryModels.values()) {
      if (libraryModel.isChanged()) {
        libraryModel.commit();
      } else {
        libraryModel.dispose();
      }
    }
  }
}
