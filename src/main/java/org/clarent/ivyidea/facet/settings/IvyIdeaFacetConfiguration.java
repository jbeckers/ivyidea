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

package org.clarent.ivyidea.facet.settings;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;
import com.intellij.util.xmlb.annotations.XCollection;
import com.intellij.util.xmlb.annotations.XCollection.Style;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.clarent.ivyidea.facet.IvyIdeaFacet;
import org.clarent.ivyidea.facet.ui.BasicSettingsTab;
import org.clarent.ivyidea.facet.ui.PropertiesSettingsTab;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/** @author Guy Mahieu */
@SuppressWarnings({
    "NonFinalFieldReferenceInEquals",
    "ObjectInstantiationInEqualsHashCode",
    "NonFinalFieldReferencedInHashCode",
    "unused"
})
public class IvyIdeaFacetConfiguration
    implements FacetConfiguration, PersistentStateComponent<IvyIdeaFacetConfiguration> {

  @NotNull
  @Property(surroundWithTag = false)
  private final FacetPropertiesSettings propertiesSettings = new FacetPropertiesSettings();

  @NotNull
  @XCollection(style = Style.v2, elementName = "config", valueAttributeName = "")
  private final Set<String> configsToResolve = new HashSet<>();

  @NotNull
  @Attribute
  private String ivyFile = "";
  @Attribute
  private boolean useProjectSettings = true;
  @Attribute
  private boolean useCustomIvySettings = true;
  @NotNull
  @Attribute
  private String ivySettingsFile = "";
  @Attribute
  private boolean onlyResolveSelectedConfigs = false;

  public IvyIdeaFacetConfiguration() {
  }

  @NotNull
  public static IvyIdeaFacetConfiguration getInstance(@NotNull final IvyIdeaFacet facet) {
    return facet.getConfiguration().getState();
  }

  @NotNull
  @Override
  @SuppressWarnings("NullableProblems")
  public FacetEditorTab[] createEditorTabs(
      @NotNull final FacetEditorContext editorContext,
      @NotNull final FacetValidatorsManager validatorsManager) {
    final PropertiesSettingsTab propertiesSettingsTab = new PropertiesSettingsTab(editorContext);
    return new FacetEditorTab[]{
        new BasicSettingsTab(editorContext, propertiesSettingsTab), propertiesSettingsTab
    };
  }

  @NotNull
  @Override
  public IvyIdeaFacetConfiguration getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull final IvyIdeaFacetConfiguration state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IvyIdeaFacetConfiguration)) {
      return false;
    }
    final IvyIdeaFacetConfiguration state = (IvyIdeaFacetConfiguration) o;
    return Objects.equals(ivyFile, state.ivyFile)
        && Objects.equals(useProjectSettings, state.useProjectSettings)
        && Objects.equals(useCustomIvySettings, state.useCustomIvySettings)
        && Objects.equals(ivySettingsFile, state.ivySettingsFile)
        && Objects.equals(onlyResolveSelectedConfigs, state.onlyResolveSelectedConfigs)
        && Objects.equals(configsToResolve, state.configsToResolve)
        && Objects.equals(propertiesSettings, state.propertiesSettings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        ivyFile,
        useProjectSettings,
        useCustomIvySettings,
        ivySettingsFile,
        onlyResolveSelectedConfigs,
        configsToResolve,
        propertiesSettings);
  }

  @Transient
  public boolean isIncludeProjectLevelPropertiesFiles() {
    return propertiesSettings.propertiesFiles.includeProjectLevelPropertiesFiles;
  }

  public void setIncludeProjectLevelPropertiesFiles(
      final boolean includeProjectLevelPropertiesFiles) {
    propertiesSettings.propertiesFiles.includeProjectLevelPropertiesFiles =
        includeProjectLevelPropertiesFiles;
  }

  @Transient
  public boolean isIncludeProjectLevelAdditionalProperties() {
    return propertiesSettings.propertiesFiles.includeProjectLevelAdditionalProperties;
  }

  public void setIncludeProjectLevelAdditionalProperties(
      final boolean includeProjectLevelAdditionalProperties) {
    propertiesSettings.propertiesFiles.includeProjectLevelAdditionalProperties =
        includeProjectLevelAdditionalProperties;
  }

  @NotNull
  @Transient
  public List<String> getPropertiesFiles() {
    return Collections.unmodifiableList(propertiesSettings.propertiesFiles.propertiesFiles);
  }

  public void setPropertiesFiles(@NotNull final Collection<String> propertiesFiles) {
    propertiesSettings.propertiesFiles.propertiesFiles.clear();
    propertiesSettings.propertiesFiles.propertiesFiles.addAll(propertiesFiles);
  }

  @NotNull
  public String getIvyFile() {
    return ivyFile;
  }

  public void setIvyFile(@NotNull final String ivyFile) {
    this.ivyFile = ivyFile;
  }

  public boolean isUseProjectSettings() {
    return useProjectSettings;
  }

  public void setUseProjectSettings(final boolean useProjectSettings) {
    this.useProjectSettings = useProjectSettings;
  }

  public boolean isUseCustomIvySettings() {
    return useCustomIvySettings;
  }

  public void setUseCustomIvySettings(final boolean useCustomIvySettings) {
    this.useCustomIvySettings = useCustomIvySettings;
  }

  @NotNull
  public String getIvySettingsFile() {
    return ivySettingsFile;
  }

  public void setIvySettingsFile(@NotNull final String ivySettingsFile) {
    this.ivySettingsFile = ivySettingsFile;
  }

  public boolean isOnlyResolveSelectedConfigs() {
    return onlyResolveSelectedConfigs;
  }

  public void setOnlyResolveSelectedConfigs(final boolean onlyResolveSelectedConfigs) {
    this.onlyResolveSelectedConfigs = onlyResolveSelectedConfigs;
  }

  @NotNull
  public Set<String> getConfigsToResolve() {
    return Collections.unmodifiableSet(configsToResolve);
  }

  public void setConfigsToResolve(@NotNull final Collection<String> configsToResolve) {
    this.configsToResolve.clear();
    this.configsToResolve.addAll(configsToResolve);
  }

  @Tag("propertiesSettings")
  private static final class FacetPropertiesSettings {

    @NotNull
    @Property(surroundWithTag = false)
    final FacetPropertiesFiles propertiesFiles = new FacetPropertiesFiles();

    @Contract(pure = true)
    private FacetPropertiesSettings() {
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof FacetPropertiesSettings)) {
        return false;
      }
      final FacetPropertiesSettings that = (FacetPropertiesSettings) o;
      return Objects.equals(propertiesFiles, that.propertiesFiles);
    }

    @Override
    public int hashCode() {
      return Objects.hash(propertiesFiles);
    }

    @Tag("propertiesFiles")
    private static final class FacetPropertiesFiles {

      @NotNull
      @Property(surroundWithTag = false)
      @XCollection(style = Style.v2, elementName = "fileName", valueAttributeName = "")
      final List<String> propertiesFiles = new ArrayList<>();

      @Attribute
      boolean includeProjectLevelPropertiesFiles = true;
      @Attribute
      boolean includeProjectLevelAdditionalProperties = true;

      @Contract(pure = true)
      private FacetPropertiesFiles() {
      }

      @Contract(value = "null -> false", pure = true)
      @Override
      public boolean equals(final Object o) {
        if (this == o) {
          return true;
        }
        if (!(o instanceof FacetPropertiesFiles)) {
          return false;
        }
        if (!super.equals(o)) {
          return false;
        }
        final FacetPropertiesFiles facetPropertiesFiles = (FacetPropertiesFiles) o;
        return Objects.equals(
            includeProjectLevelPropertiesFiles,
            facetPropertiesFiles.includeProjectLevelPropertiesFiles)
            && Objects.equals(
            includeProjectLevelAdditionalProperties,
            facetPropertiesFiles.includeProjectLevelAdditionalProperties)
            && Objects.equals(this.propertiesFiles, facetPropertiesFiles.propertiesFiles);
      }

      @Override
      public int hashCode() {
        return Objects.hash(
            super.hashCode(),
            includeProjectLevelPropertiesFiles,
            includeProjectLevelAdditionalProperties,
            propertiesFiles);
      }
    }
  }
}
