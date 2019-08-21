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

package org.clarent.ivyidea.config.model;

import static java.util.Arrays.asList;
import static org.clarent.ivyidea.config.model.ArtifactTypeSettings.DependencyCategory.Classes;
import static org.clarent.ivyidea.config.model.ArtifactTypeSettings.DependencyCategory.Javadoc;
import static org.clarent.ivyidea.config.model.ArtifactTypeSettings.DependencyCategory.Sources;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public class ArtifactTypeSettings implements PersistentStateComponent<ArtifactTypeSettings> {

  @SuppressWarnings("ImmutableEnumChecker")
  public enum DependencyCategory {
    Sources("source", "src", "sources", "srcs"),
    Javadoc("javadoc", "doc", "docs", "apidoc", "apidocs", "documentation", "documents"),
    Classes("jar", "mar", "sar", "war", "ear", "ejb", "bundle", "test-jar");

    private final List<String> defaultTypes;

    DependencyCategory(final String... defaultTypes) {
      this.defaultTypes = Collections.unmodifiableList(asList(defaultTypes));
    }

    public List<String> getDefaultTypes() {
      return defaultTypes;
    }
  }

  private final Map<DependencyCategory, Set<String>> typesPerCategory =
      new EnumMap<>(DependencyCategory.class);

  @Nullable
  public DependencyCategory getCategoryForType(final String type) {
    if (type == null) {
      return null;
    }
    if (isConfigurationEmpty()) {
      fillDefaults();
    }
    for (final Entry<DependencyCategory, Set<String>> entry : typesPerCategory.entrySet()) {
      final Set<String> types = entry.getValue();
      if (types != null && types.contains(type.trim().toLowerCase())) {
        return entry.getKey();
      }
    }
    return null;
  }

  private void fillDefaults() {
    for (final DependencyCategory category : DependencyCategory.values()) {
      setTypesForCategory(category, joinArtifactTypes(category.getDefaultTypes()));
    }
  }

  public void setTypesForCategory(@NotNull final DependencyCategory category, final String types) {
    if (types != null) {
      typesPerCategory.put(category, splitArtifactTypes(types));
    }
  }

  public String getTypesStringForCategory(@NotNull final DependencyCategory category) {
    if (isConfigurationEmpty()) {
      // nothing is configured for any category --> use defaults
      return joinArtifactTypes(category.getDefaultTypes());
    }
    return joinArtifactTypes(typesPerCategory.get(category));
  }

  protected boolean isConfigurationEmpty() {
    boolean configFound = false;
    for (final DependencyCategory dependencyCategory : DependencyCategory.values()) {
      final Set<String> types = typesPerCategory.get(dependencyCategory);
      configFound = types != null && !types.isEmpty();
      if (configFound) {
        break;
      }
    }
    return !configFound;
  }

  @SuppressWarnings("StringSplitter")
  private static Set<String> splitArtifactTypes(final String artifactTypesString) {
    final Set<String> result = new LinkedHashSet<>();
    if (artifactTypesString != null) {
      final String[] types = artifactTypesString.split(",");
      for (final String type : types) {
        final String typeToAdd = type.trim().toLowerCase();
        if (!typeToAdd.isEmpty()) {
          result.add(typeToAdd);
        }
      }
    }
    return result;
  }

  private static String joinArtifactTypes(final Iterable<String> artifactTypes) {
    if (artifactTypes == null) {
      return "";
    }
    final StringBuilder sb = new StringBuilder();
    String separator = "";
    for (final String artifactType : artifactTypes) {
      sb.append(separator).append(artifactType);
      separator = ", ";
    }
    return sb.toString();
  }

  @Override
  public ArtifactTypeSettings getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull final ArtifactTypeSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  // Getters and setters needed for intellij settings serialization

  public String getSourcesTypes() {
    return joinArtifactTypes(typesPerCategory.get(Sources));
  }

  public String getClassesTypes() {
    return joinArtifactTypes(typesPerCategory.get(Classes));
  }

  public String getJavadocTypes() {
    return joinArtifactTypes(typesPerCategory.get(Javadoc));
  }

  public void setSourcesTypes(final String types) {
    setTypesForCategory(Sources, types);
  }

  public void setClassesTypes(final String types) {
    setTypesForCategory(Classes, types);
  }

  public void setJavadocTypes(final String types) {
    setTypesForCategory(Javadoc, types);
  }
}
