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

package org.clarent.ivyidea.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DependencyCategoryManager {

  private final Map<DependencyCategory, Set<String>> typesPerCategory =
      new EnumMap<>(DependencyCategory.class);

  @NotNull
  private static String getDefaultTypes(@NotNull final DependencyCategory category) {
    return String.join(", ", category.getDefaultTypes());
  }

  @Nullable
  public DependencyCategory getCategoryForType(@NotNull final String type) {
    if (isConfigurationEmpty()) {
      for (final DependencyCategory category : DependencyCategory.values()) {
        setTypesForCategory(category, getDefaultTypes(category));
      }
    }
    for (final Entry<DependencyCategory, Set<String>> entry : typesPerCategory.entrySet()) {
      final Set<String> types = entry.getValue();
      if (types != null && types.contains(type.trim().toLowerCase())) {
        return entry.getKey();
      }
    }
    return null;
  }

  @SuppressWarnings("StringSplitter")
  public void setTypesForCategory(
      @NotNull final DependencyCategory category, @Nullable final String types) {
    if (types != null) {
      final Set<String> result = new LinkedHashSet<>();
      for (final String type : types.split(",")) {
        final String typeToAdd = type.trim().toLowerCase();
        if (!typeToAdd.isEmpty()) {
          result.add(typeToAdd);
        }
      }
      typesPerCategory.put(category, result);
    }
  }

  public String getTypesStringForCategory(@NotNull final DependencyCategory category) {
    return isConfigurationEmpty() ? getDefaultTypes(category) : getTypes(category);
  }

  public boolean isConfigurationEmpty() {
    return Arrays.stream(DependencyCategory.values())
        .allMatch(
            dependencyCategory ->
                typesPerCategory
                    .getOrDefault(dependencyCategory, Collections.emptySet())
                    .isEmpty());
  }

  @NotNull
  public String getTypes(@NotNull final DependencyCategory dependencyCategory) {
    return String.join(
        ", ", typesPerCategory.getOrDefault(dependencyCategory, Collections.emptySet()));
  }
}
