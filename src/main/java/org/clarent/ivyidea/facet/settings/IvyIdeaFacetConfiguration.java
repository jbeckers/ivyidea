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
import com.intellij.util.xmlb.annotations.XCollection;
import com.intellij.util.xmlb.annotations.XCollection.Style;
import java.util.ArrayList;
import java.util.Collections;
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
    "unused",
    "WeakerAccess"
})
public class IvyIdeaFacetConfiguration
    implements FacetConfiguration, PersistentStateComponent<IvyIdeaFacetConfiguration> {

  @NotNull
  @Attribute
  public String ivyFile = "";
  @Attribute
  public boolean useProjectSettings = true;
  @Attribute
  public boolean useCustomIvySettings = true;
  @NotNull
  @Attribute
  public String ivySettingsFile = "";
  @Attribute
  public boolean onlyResolveSelectedConfigs = false;

  @NotNull
  @XCollection(style = Style.v2, elementName = "config", valueAttributeName = "")
  public Set<String> configsToResolve = Collections.emptySet();

  @NotNull
  @Property(surroundWithTag = false)
  public FacetPropertiesSettings propertiesSettings = new FacetPropertiesSettings();

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

  @SuppressWarnings({"WeakerAccess", "unused"})
  @Tag("propertiesSettings")
  public static class FacetPropertiesSettings {

    @NotNull
    @Property(surroundWithTag = false)
    public FacetPropertiesFiles propertiesFiles = new FacetPropertiesFiles();

    @Contract(pure = true)
    public FacetPropertiesSettings() {
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

    @SuppressWarnings({"WeakerAccess", "unused"})
    @Tag("propertiesFiles")
    public static class FacetPropertiesFiles {

      private static final long serialVersionUID = 4240068708636271273L;

      @Attribute
      public boolean includeProjectLevelPropertiesFiles = true;
      @Attribute
      public boolean includeProjectLevelAdditionalProperties = true;

      @NotNull
      @Property(surroundWithTag = false)
      @XCollection(style = Style.v2, elementName = "fileName", valueAttributeName = "")
      public List<String> propertiesFiles = new ArrayList<>();

      @Contract(pure = true)
      public FacetPropertiesFiles() {
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
