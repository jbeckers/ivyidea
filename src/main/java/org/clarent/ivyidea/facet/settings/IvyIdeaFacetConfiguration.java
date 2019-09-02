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
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.XCollection;
import com.intellij.util.xmlb.annotations.XCollection.Style;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.clarent.ivyidea.facet.ui.BasicSettingsTab;
import org.clarent.ivyidea.facet.ui.PropertiesSettingsTab;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/** @author Guy Mahieu */
@SuppressWarnings({
    "NonFinalFieldReferenceInEquals",
    "ObjectInstantiationInEqualsHashCode",
    "NonFinalFieldReferencedInHashCode"
})
public class IvyIdeaFacetConfiguration
    implements FacetConfiguration, PersistentStateComponent<IvyIdeaFacetConfiguration.State> {

  @NotNull
  private State state = new State();

  @Override
  public FacetEditorTab[] createEditorTabs(
      final FacetEditorContext editorContext, final FacetValidatorsManager validatorsManager) {
    final PropertiesSettingsTab propertiesSettingsTab = new PropertiesSettingsTab(editorContext);
    return new FacetEditorTab[]{
        new BasicSettingsTab(editorContext, propertiesSettingsTab), propertiesSettingsTab
    };
  }

  @NotNull
  @Override
  public State getState() {
    return state;
  }

  @Override
  public void loadState(@NotNull final State state) {
    this.state = state;
  }

  @SuppressWarnings({"unused", "WeakerAccess"})
  public static class State {

    @NotNull
    @Attribute
    public String ivyFile;

    @NotNull
    @Attribute
    public Boolean useProjectSettings;

    @NotNull
    @Attribute
    public Boolean useCustomIvySettings;

    @NotNull
    @Attribute
    public String ivySettingsFile;

    @NotNull
    @Attribute
    public Boolean onlyResolveSelectedConfigs;

    @NotNull
    @XCollection(style = Style.v2, elementName = "config", valueAttributeName = "")
    public Set<String> configsToResolve;

    @NotNull
    public FacetPropertiesSettings propertiesSettings;

    public State() {
      ivyFile = "";
      useProjectSettings = true;
      useCustomIvySettings = true;
      ivySettingsFile = "";
      onlyResolveSelectedConfigs = false;
      configsToResolve = Collections.emptySet();
      propertiesSettings = new FacetPropertiesSettings();
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof State)) {
        return false;
      }
      final State state = (State) o;
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
    public static class FacetPropertiesSettings {

      @NotNull
      @XCollection(style = Style.v2, elementName = "fileName", valueAttributeName = "")
      public FacetPropertiesFilesList propertiesFiles;

      @Contract(pure = true)
      public FacetPropertiesSettings() {
        propertiesFiles = new FacetPropertiesFilesList();
      }

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

      @SuppressWarnings({"WeakerAccess", "unused", "ClassExtendsConcreteCollection"})
      public static class FacetPropertiesFilesList extends ArrayList<String> {

        private static final long serialVersionUID = 4240068708636271273L;

        @NotNull
        @Attribute
        public Boolean includeProjectLevelPropertiesFiles;

        @NotNull
        @Attribute
        public Boolean includeProjectLevelAdditionalProperties;

        @Contract(pure = true)
        public FacetPropertiesFilesList() {
          includeProjectLevelPropertiesFiles = true;
          includeProjectLevelAdditionalProperties = true;
        }

        public FacetPropertiesFilesList(final Collection<String> items) {
          super(items);
          includeProjectLevelPropertiesFiles = true;
          includeProjectLevelAdditionalProperties = true;
        }

        @Override
        public boolean equals(final Object o) {
          if (this == o) {
            return true;
          }
          if (!(o instanceof FacetPropertiesFilesList)) {
            return false;
          }
          if (!super.equals(o)) {
            return false;
          }
          final FacetPropertiesFilesList strings = (FacetPropertiesFilesList) o;
          return Objects.equals(
              includeProjectLevelPropertiesFiles, strings.includeProjectLevelPropertiesFiles)
              && Objects.equals(
              includeProjectLevelAdditionalProperties,
              strings.includeProjectLevelAdditionalProperties);
        }

        @Override
        public int hashCode() {
          return Objects.hash(
              super.hashCode(),
              includeProjectLevelPropertiesFiles,
              includeProjectLevelAdditionalProperties);
        }

        @SuppressWarnings("unchecked")
        @Override
        public final FacetPropertiesFilesList clone() {
          final Object clone = super.clone();
          if (clone instanceof Collection<?>) {
            return new FacetPropertiesFilesList((Collection<String>) clone);
          } else {
            throw new InternalError("ArrayList.clone() went wrong!");
          }
        }
      }
    }
  }
}
