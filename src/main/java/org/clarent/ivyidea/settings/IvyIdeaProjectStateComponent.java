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

package org.clarent.ivyidea.settings;

import static org.clarent.ivyidea.util.DependencyCategory.Classes;
import static org.clarent.ivyidea.util.DependencyCategory.Javadoc;
import static org.clarent.ivyidea.util.DependencyCategory.Sources;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.clarent.ivyidea.IvyIdeaConstants;
import org.clarent.ivyidea.settings.IvyIdeaProjectStateComponent.State;
import org.clarent.ivyidea.util.ConsoleViewMessageLogger.IvyLogLevel;
import org.clarent.ivyidea.util.DependencyCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Guy Mahieu
 */
@com.intellij.openapi.components.State(name = IvyIdeaConstants.PROJECT_STATE_NAME)
public class IvyIdeaProjectStateComponent implements PersistentStateComponent<State> {

  private final State internalState;

  public IvyIdeaProjectStateComponent(final Project project) {
    this.internalState = new State();
  }

  @Override
  @NotNull
  public State getState() {
    return internalState;
  }

  @Override
  public void loadState(@NotNull final State state) {
    XmlSerializerUtil.copyBean(state, this.internalState);
  }

  /**
   * @author Guy Mahieu
   */
  public static class State {

    @NotNull
    private Boolean useCustomIvySettings = true;
    @NotNull
    private String ivySettingsFile = "";
    @NotNull
    private Boolean validateIvyFiles = false;
    @NotNull
    private Boolean resolveTransitively = true;
    @NotNull
    private Boolean resolveCacheOnly = false;
    @NotNull
    private Boolean resolveInBackground = false;
    @NotNull
    private Boolean alwaysAttachSources = true;
    @NotNull
    private Boolean alwaysAttachJavadocs = true;
    @NotNull
    private Boolean libraryNameIncludesModule = false;
    @NotNull
    private Boolean libraryNameIncludesConfiguration = false;
    @NotNull
    private String ivyLogLevelThreshold = IvyLogLevel.None.name();
    @NotNull
    private ArtifactTypeSettings artifactTypeSettings = new ArtifactTypeSettings();
    @NotNull
    private PropertiesSettings propertiesSettings = new PropertiesSettings();

    @NotNull
    public String getIvySettingsFile() {
      return ivySettingsFile;
    }

    void setIvySettingsFile(@NotNull final String ivySettingsFile) {
      this.ivySettingsFile = ivySettingsFile;
    }

    @NotNull
    Boolean isValidateIvyFiles() {
      return validateIvyFiles;
    }

    void setValidateIvyFiles(@NotNull final Boolean validateIvyFiles) {
      this.validateIvyFiles = validateIvyFiles;
    }

    @NotNull
    Boolean isResolveTransitively() {
      return resolveTransitively;
    }

    void setResolveTransitively(@NotNull final Boolean resolveTransitively) {
      this.resolveTransitively = resolveTransitively;
    }

    @NotNull
    Boolean isResolveCacheOnly() {
      return resolveCacheOnly;
    }

    void setResolveCacheOnly(@NotNull final Boolean resolveCacheOnly) {
      this.resolveCacheOnly = resolveCacheOnly;
    }

    @NotNull
    public Boolean isResolveInBackground() {
      return resolveInBackground;
    }

    void setResolveInBackground(@NotNull final Boolean resolveInBackground) {
      this.resolveInBackground = resolveInBackground;
    }

    @NotNull
    public Boolean isAlwaysAttachSources() {
      return alwaysAttachSources;
    }

    void setAlwaysAttachSources(@NotNull final Boolean alwaysAttachSources) {
      this.alwaysAttachSources = alwaysAttachSources;
    }

    @NotNull
    public Boolean isAlwaysAttachJavadocs() {
      return alwaysAttachJavadocs;
    }

    void setAlwaysAttachJavadocs(@NotNull final Boolean alwaysAttachJavadocs) {
      this.alwaysAttachJavadocs = alwaysAttachJavadocs;
    }

    @NotNull
    public Boolean isUseCustomIvySettings() {
      return useCustomIvySettings;
    }

    void setUseCustomIvySettings(@NotNull final Boolean useCustomIvySettings) {
      this.useCustomIvySettings = useCustomIvySettings;
    }

    @NotNull
    public PropertiesSettings getPropertiesSettings() {
      return propertiesSettings;
    }

    void setPropertiesSettings(@NotNull final PropertiesSettings propertiesSettings) {
      this.propertiesSettings = propertiesSettings;
    }

    @NotNull
    public Boolean isLibraryNameIncludesModule() {
      return libraryNameIncludesModule;
    }

    void setLibraryNameIncludesModule(@NotNull final Boolean libraryNameIncludesModule) {
      this.libraryNameIncludesModule = libraryNameIncludesModule;
    }

    @NotNull
    public Boolean isLibraryNameIncludesConfiguration() {
      return libraryNameIncludesConfiguration;
    }

    void setLibraryNameIncludesConfiguration(
        @NotNull final Boolean libraryNameIncludesConfiguration) {
      this.libraryNameIncludesConfiguration = libraryNameIncludesConfiguration;
    }

    @NotNull
    public String getIvyLogLevelThreshold() {
      return ivyLogLevelThreshold;
    }

    void setIvyLogLevelThreshold(@NotNull final String ivyLogLevelThreshold) {
      this.ivyLogLevelThreshold = ivyLogLevelThreshold;
    }

    @NotNull
    public ArtifactTypeSettings getArtifactTypeSettings() {
      return artifactTypeSettings;
    }

    public void setArtifactTypeSettings(@NotNull final ArtifactTypeSettings artifactTypeSettings) {
      this.artifactTypeSettings = artifactTypeSettings;
    }

    public void updateResolveOptions(final ResolveOptions options) {
      options.setValidate(validateIvyFiles);
      options.setTransitive(resolveTransitively);
      options.setUseCacheOnly(resolveCacheOnly);
    }

    /**
     * @author Guy Mahieu
     */
    public static class PropertiesSettings {

      @NotNull
      private List<String> propertyFiles = new ArrayList<>();

      @NotNull
      public static PropertiesSettings copyDataFrom(
          @NotNull final PropertiesSettings propertiesSettings) {
        final PropertiesSettings result = new PropertiesSettings();
        result.propertyFiles = new ArrayList<>(propertiesSettings.propertyFiles);
        return result;
      }

      @NotNull
      public List<String> getPropertyFiles() {
        return propertyFiles;
      }

      void setPropertyFiles(@NotNull final List<String> propertyFiles) {
        this.propertyFiles = propertyFiles;
      }
    }

    /**
     * @author Guy Mahieu
     */
    public static class ArtifactTypeSettings
        implements PersistentStateComponent<ArtifactTypeSettings> {

      private final Map<DependencyCategory, Set<String>> typesPerCategory =
          new EnumMap<>(DependencyCategory.class);

      @Nullable
      public DependencyCategory getCategoryForType(@NotNull final String type) {
        if (isConfigurationEmpty()) {
          for (final DependencyCategory category : DependencyCategory.values()) {
            String result = "";
            final Iterable<String> artifactTypes = category.getDefaultTypes();
            if (artifactTypes != null) {
              final StringBuilder sb = new StringBuilder();
              String separator = "";
              for (final String artifactType : artifactTypes) {
                sb.append(separator).append(artifactType);
                separator = ", ";
              }
              result = sb.toString();
            }
            setTypesForCategory(category, result);
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
          @NotNull final DependencyCategory category, final String types) {
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
        if (isConfigurationEmpty()) {
          // nothing is configured for any category --> use defaults
          final Iterable<String> artifactTypes = category.getDefaultTypes();
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
        return getTypes(category);
      }

      public boolean isConfigurationEmpty() {
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
        return getTypes(Sources);
      }

      public void setSourcesTypes(final String types) {
        setTypesForCategory(Sources, types);
      }

      public String getClassesTypes() {
        return getTypes(Classes);
      }

      public void setClassesTypes(final String types) {
        setTypesForCategory(Classes, types);
      }

      public String getJavadocTypes() {
        return getTypes(Javadoc);
      }

      @NotNull
      private String getTypes(final DependencyCategory dependencyCategory) {
        final Iterable<String> artifactTypes = typesPerCategory.get(dependencyCategory);
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

      public void setJavadocTypes(final String types) {
        setTypesForCategory(Javadoc, types);
      }
    }
  }
}
