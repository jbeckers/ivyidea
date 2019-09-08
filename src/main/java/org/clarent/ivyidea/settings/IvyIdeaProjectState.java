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
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;
import com.intellij.util.xmlb.annotations.XCollection;
import com.intellij.util.xmlb.annotations.XCollection.Style;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.clarent.ivyidea.IvyIdeaConstants;
import org.clarent.ivyidea.util.ConsoleViewMessageLogger.IvyLogLevel;
import org.clarent.ivyidea.util.DependencyCategoryManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author Guy Mahieu
 */
@State(name = IvyIdeaConstants.PROJECT_STATE_NAME)
@SuppressWarnings({
    "NonFinalFieldReferenceInEquals",
    "ObjectInstantiationInEqualsHashCode",
    "NonFinalFieldReferencedInHashCode",
    "unused",
    "AccessingNonPublicFieldOfAnotherObject"
})
public class IvyIdeaProjectState implements PersistentStateComponent<IvyIdeaProjectState> {

  @NotNull
  @OptionTag
  private final ArtifactTypeSettings artifactTypeSettings = new ArtifactTypeSettings();

  @NotNull
  @OptionTag
  private final PropertiesSettings propertiesSettings = new PropertiesSettings();

  @Transient
  private boolean useCustomIvySettings = true;
  @NotNull
  @OptionTag
  private String ivySettingsFile = "";
  @OptionTag
  private boolean validateIvyFiles = false;
  @OptionTag
  private boolean resolveTransitively = true;
  @OptionTag
  private boolean resolveCacheOnly = false;
  @OptionTag
  private boolean resolveInBackground = false;
  @OptionTag
  private boolean alwaysAttachSources = true;
  @OptionTag
  private boolean alwaysAttachJavadocs = true;
  @OptionTag
  private boolean libraryNameIncludesModule = false;
  @OptionTag
  private boolean libraryNameIncludesConfiguration = false;
  @NotNull
  @OptionTag
  private String ivyLogLevelThreshold = IvyLogLevel.None.name();

  @Contract(pure = true)
  public IvyIdeaProjectState() {}

  @Contract(pure = true)
  public IvyIdeaProjectState(@SuppressWarnings("unused") @NotNull final Project ignored) {}

  @NotNull
  public static IvyIdeaProjectState getInstance(@NotNull final Project project) {
    return ServiceManager.getService(project, IvyIdeaProjectState.class).getState();
  }

  @NotNull
  @Override
  public IvyIdeaProjectState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull final IvyIdeaProjectState state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  @Contract(value = "null -> false", pure = true)
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IvyIdeaProjectState)) {
      return false;
    }
    final IvyIdeaProjectState that = (IvyIdeaProjectState) o;
    return Objects.equals(useCustomIvySettings, that.useCustomIvySettings)
        && Objects.equals(ivySettingsFile, that.ivySettingsFile)
        && Objects.equals(validateIvyFiles, that.validateIvyFiles)
        && Objects.equals(resolveTransitively, that.resolveTransitively)
        && Objects.equals(resolveCacheOnly, that.resolveCacheOnly)
        && Objects.equals(resolveInBackground, that.resolveInBackground)
        && Objects.equals(alwaysAttachSources, that.alwaysAttachSources)
        && Objects.equals(alwaysAttachJavadocs, that.alwaysAttachJavadocs)
        && Objects.equals(libraryNameIncludesModule, that.libraryNameIncludesModule)
        && Objects.equals(libraryNameIncludesConfiguration, that.libraryNameIncludesConfiguration)
        && Objects.equals(ivyLogLevelThreshold, that.ivyLogLevelThreshold)
        && Objects.equals(artifactTypeSettings, that.artifactTypeSettings)
        && Objects.equals(propertiesSettings, that.propertiesSettings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        useCustomIvySettings,
        ivySettingsFile,
        validateIvyFiles,
        resolveTransitively,
        resolveCacheOnly,
        resolveInBackground,
        alwaysAttachSources,
        alwaysAttachJavadocs,
        libraryNameIncludesModule,
        libraryNameIncludesConfiguration,
        ivyLogLevelThreshold,
        artifactTypeSettings,
        propertiesSettings);
  }

  @NotNull
  @Transient
  public String getSourcesTypes() {
    return artifactTypeSettings.getSourcesTypes();
  }

  void setSourcesTypes(@NotNull final String types) {
    artifactTypeSettings.setSourcesTypes(types);
  }

  @NotNull
  @Transient
  public String getClassesTypes() {
    return artifactTypeSettings.getClassesTypes();
  }

  void setClassesTypes(@NotNull final String types) {
    artifactTypeSettings.setClassesTypes(types);
  }

  @NotNull
  @Transient
  public String getJavadocTypes() {
    return artifactTypeSettings.getJavadocTypes();
  }

  void setJavadocTypes(@NotNull final String types) {
    artifactTypeSettings.setJavadocTypes(types);
  }

  @NotNull
  @Transient
  public DependencyCategoryManager getDependencyCategoryManager() {
    return artifactTypeSettings.getManager();
  }

  @NotNull
  @Transient
  public List<String> getPropertyFiles() {
    return Collections.unmodifiableList(propertiesSettings.propertyFiles.propertyFiles);
  }

  void setPropertyFiles(@NotNull final Collection<String> propertyFiles) {
    propertiesSettings.propertyFiles.propertyFiles.clear();
    propertiesSettings.propertyFiles.propertyFiles.addAll(propertyFiles);
  }

  public boolean isUseCustomIvySettings() {
    return useCustomIvySettings;
  }

  void setUseCustomIvySettings(final boolean useCustomIvySettings) {
    this.useCustomIvySettings = useCustomIvySettings;
  }

  @NotNull
  public String getIvySettingsFile() {
    return ivySettingsFile;
  }

  void setIvySettingsFile(@NotNull final String ivySettingsFile) {
    this.ivySettingsFile = ivySettingsFile;
  }

  public boolean isValidateIvyFiles() {
    return validateIvyFiles;
  }

  void setValidateIvyFiles(final boolean validateIvyFiles) {
    this.validateIvyFiles = validateIvyFiles;
  }

  public boolean isResolveTransitively() {
    return resolveTransitively;
  }

  void setResolveTransitively(final boolean resolveTransitively) {
    this.resolveTransitively = resolveTransitively;
  }

  public boolean isResolveCacheOnly() {
    return resolveCacheOnly;
  }

  void setResolveCacheOnly(final boolean resolveCacheOnly) {
    this.resolveCacheOnly = resolveCacheOnly;
  }

  public boolean isResolveInBackground() {
    return resolveInBackground;
  }

  void setResolveInBackground(final boolean resolveInBackground) {
    this.resolveInBackground = resolveInBackground;
  }

  public boolean isAlwaysAttachSources() {
    return alwaysAttachSources;
  }

  void setAlwaysAttachSources(final boolean alwaysAttachSources) {
    this.alwaysAttachSources = alwaysAttachSources;
  }

  public boolean isAlwaysAttachJavadocs() {
    return alwaysAttachJavadocs;
  }

  void setAlwaysAttachJavadocs(final boolean alwaysAttachJavadocs) {
    this.alwaysAttachJavadocs = alwaysAttachJavadocs;
  }

  public boolean isLibraryNameIncludesModule() {
    return libraryNameIncludesModule;
  }

  void setLibraryNameIncludesModule(final boolean libraryNameIncludesModule) {
    this.libraryNameIncludesModule = libraryNameIncludesModule;
  }

  public boolean isLibraryNameIncludesConfiguration() {
    return libraryNameIncludesConfiguration;
  }

  void setLibraryNameIncludesConfiguration(final boolean libraryNameIncludesConfiguration) {
    this.libraryNameIncludesConfiguration = libraryNameIncludesConfiguration;
  }

  @NotNull
  public String getIvyLogLevelThreshold() {
    return ivyLogLevelThreshold;
  }

  void setIvyLogLevelThreshold(@NotNull final String ivyLogLevelThreshold) {
    this.ivyLogLevelThreshold = ivyLogLevelThreshold;
  }

  /**
   * This is a Bean with getters and setters that act on the {@link DependencyCategoryManager}. Keep
   * getters and setters public!
   */
  @SuppressWarnings("WeakerAccess")
  static final class ArtifactTypeSettings {

    @NotNull
    @Transient
    private final DependencyCategoryManager manager = new DependencyCategoryManager();

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ArtifactTypeSettings)) {
        return false;
      }
      final ArtifactTypeSettings that = (ArtifactTypeSettings) o;
      return Objects.equals(getClassesTypes(), that.getClassesTypes())
          && Objects.equals(getSourcesTypes(), that.getSourcesTypes())
          && Objects.equals(getJavadocTypes(), that.getJavadocTypes());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getClassesTypes(), getSourcesTypes(), getJavadocTypes());
    }

    @NotNull
    public String getSourcesTypes() {
      return manager.getTypes(Sources);
    }

    public void setSourcesTypes(@NotNull final String types) {
      manager.setTypesForCategory(Sources, types);
    }

    @NotNull
    public String getClassesTypes() {
      return manager.getTypes(Classes);
    }

    public void setClassesTypes(@NotNull final String types) {
      manager.setTypesForCategory(Classes, types);
    }

    @NotNull
    public String getJavadocTypes() {
      return manager.getTypes(Javadoc);
    }

    public void setJavadocTypes(@NotNull final String types) {
      manager.setTypesForCategory(Javadoc, types);
    }

    @Contract(pure = true)
    @NotNull
    @Transient
    public DependencyCategoryManager getManager() {
      return manager;
    }
  }

  private static final class PropertiesSettings {

    @NotNull
    @OptionTag
    private final PropertyFiles propertyFiles = new PropertyFiles();

    @Contract(pure = true)
    private PropertiesSettings() {}

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof PropertiesSettings)) {
        return false;
      }
      final PropertiesSettings that = (PropertiesSettings) o;
      return propertyFiles.equals(that.propertyFiles);
    }

    @Override
    public int hashCode() {
      return Objects.hash(propertyFiles);
    }
  }

  @Tag("list")
  private static final class PropertyFiles {

    @Property(surroundWithTag = false)
    @XCollection(style = Style.v2)
    @NotNull
    private final List<String> propertyFiles = new ArrayList<>();

    @Contract(pure = true)
    private PropertyFiles() {}

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof PropertyFiles)) {
        return false;
      }
      final PropertyFiles that = (PropertyFiles) o;
      return Objects.equals(propertyFiles, that.propertyFiles);
    }

    @Override
    public int hashCode() {
      return Objects.hash(propertyFiles);
    }
  }
}
