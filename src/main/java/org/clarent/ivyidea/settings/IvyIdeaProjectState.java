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
    "WeakerAccess",
    "NonFinalFieldReferenceInEquals",
    "ObjectInstantiationInEqualsHashCode",
    "NonFinalFieldReferencedInHashCode"
})
public class IvyIdeaProjectState implements PersistentStateComponent<IvyIdeaProjectState> {

  @Transient
  public boolean useCustomIvySettings = true;
  @NotNull
  @OptionTag
  public String ivySettingsFile = "";
  @OptionTag
  public boolean validateIvyFiles = false;
  @OptionTag
  public boolean resolveTransitively = true;
  @OptionTag
  public boolean resolveCacheOnly = false;
  @OptionTag
  public boolean resolveInBackground = false;
  @OptionTag
  public boolean alwaysAttachSources = true;
  @OptionTag
  public boolean alwaysAttachJavadocs = true;
  @OptionTag
  public boolean libraryNameIncludesModule = false;
  @OptionTag
  public boolean libraryNameIncludesConfiguration = false;
  @NotNull
  @OptionTag
  public String ivyLogLevelThreshold = IvyLogLevel.None.name();
  @NotNull
  @OptionTag
  public ArtifactTypeSettings artifactTypeSettings = new ArtifactTypeSettings();
  @NotNull
  @OptionTag
  public PropertiesSettings propertiesSettings = new PropertiesSettings();

  @Contract(pure = true)
  public IvyIdeaProjectState() {
  }

  @Contract(pure = true)
  public IvyIdeaProjectState(@NotNull final Project ignored) {
  }

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

  @SuppressWarnings("unused")
  public static class ArtifactTypeSettings {

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

    @NotNull
    @Transient
    public DependencyCategoryManager getManager() {
      return manager;
    }
  }

  public static class PropertiesSettings {

    @NotNull
    @OptionTag
    public PropertyFiles propertyFiles = new PropertyFiles();

    @Contract(pure = true)
    public PropertiesSettings() {
    }

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
  public static class PropertyFiles {

    @Property(surroundWithTag = false)
    @XCollection(style = Style.v2)
    @NotNull
    public List<String> propertyFiles = new ArrayList<>();

    @Contract(pure = true)
    public PropertyFiles() {
    }

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
