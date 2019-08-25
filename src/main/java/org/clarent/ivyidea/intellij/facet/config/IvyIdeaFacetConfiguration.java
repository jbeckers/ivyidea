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

package org.clarent.ivyidea.intellij.facet.config;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.FacetManager;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.module.Module;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.clarent.ivyidea.intellij.extension.facet.IvyIdeaFacetType;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacet;
import org.clarent.ivyidea.intellij.facet.ui.BasicSettingsTab;
import org.clarent.ivyidea.intellij.facet.ui.PropertiesSettingsTab;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public class IvyIdeaFacetConfiguration implements FacetConfiguration {

  private static final Logger LOGGER = Logger.getLogger(IvyIdeaFacetConfiguration.class.getName());

  /*
      Al the fields are initialized with a default value to avoid errors when adding a new IvyIDEA facet to an
      existing module.
  */
  private String ivyFile = "";
  private boolean useProjectSettings = true;
  private boolean useCustomIvySettings = true;
  private String ivySettingsFile = "";
  private boolean onlyResolveSelectedConfigs = false;
  private Set<String> configsToResolve = Collections.emptySet();
  private FacetPropertiesSettings facetPropertiesSettings = new FacetPropertiesSettings();

  @Nullable
  public static IvyIdeaFacetConfiguration getInstance(final Module module) {
    final IvyIdeaFacet ivyIdeaFacet =
        FacetManager.getInstance(module).getFacetByType(IvyIdeaFacetType.ID);
    if (ivyIdeaFacet != null) {
      return ivyIdeaFacet.getConfiguration();
    } else {
      LOGGER.info(
          "Module " + module.getName() + " does not have the IvyIDEA facet configured; ignoring.");
      return null;
    }
  }

  @NotNull
  public String getIvyFile() {
    return ivyFile.trim();
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

  public FacetPropertiesSettings getFacetPropertiesSettings() {
    return facetPropertiesSettings;
  }

  public void setFacetPropertiesSettings(final FacetPropertiesSettings facetPropertiesSettings) {
    this.facetPropertiesSettings = facetPropertiesSettings;
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

  public Set<String> getConfigsToResolve() {
    return configsToResolve;
  }

  public void setConfigsToResolve(final Set<String> configsToResolve) {
    this.configsToResolve = configsToResolve;
  }

  public FacetPropertiesSettings getPropertiesSettings() {
    return facetPropertiesSettings;
  }

  @Override
  public FacetEditorTab[] createEditorTabs(
      final FacetEditorContext editorContext, final FacetValidatorsManager validatorsManager) {
    final PropertiesSettingsTab propertiesSettingsTab = new PropertiesSettingsTab(editorContext);
    final BasicSettingsTab basicSettingsTab =
        new BasicSettingsTab(editorContext, propertiesSettingsTab);
    return new FacetEditorTab[] {basicSettingsTab, propertiesSettingsTab};
  }

  @Override
  public void readExternal(final Element element) {
    readBasicSettings(element);
    final Element propertiesSettingsElement = element.getChild("propertiesSettings");
    if (propertiesSettingsElement != null) {
      facetPropertiesSettings.readExternal(propertiesSettingsElement);
    }
  }

  private void readBasicSettings(final Element element) {
    setIvyFile(element.getAttributeValue("ivyFile", ""));
    setUseCustomIvySettings(
        Boolean.valueOf(
            element.getAttributeValue("useCustomIvySettings", Boolean.TRUE.toString())));
    setIvySettingsFile(element.getAttributeValue("ivySettingsFile", ""));
    setOnlyResolveSelectedConfigs(
        Boolean.valueOf(
            element.getAttributeValue("onlyResolveSelectedConfigs", Boolean.FALSE.toString())));
    setUseProjectSettings(
        Boolean.valueOf(element.getAttributeValue("useProjectSettings", Boolean.TRUE.toString())));
    final Element configsToResolveElement = element.getChild("configsToResolve");
    if (configsToResolveElement != null) {
      final Set<String> configsToResolve = new TreeSet<>();
      @SuppressWarnings("unchecked") final List<Element> configElements = configsToResolveElement
          .getChildren("config");
      for (final Element configElement : configElements) {
        configsToResolve.add(configElement.getTextTrim());
      }
      setConfigsToResolve(configsToResolve);
    }
  }

  @Override
  public void writeExternal(final Element element) {
    writeBasicSettings(element);
    final Element propertiesSettingsElement = new Element("propertiesSettings");
    if (facetPropertiesSettings != null) {
      facetPropertiesSettings.writeExternal(propertiesSettingsElement);
    }
    element.addContent(propertiesSettingsElement);
  }

  private void writeBasicSettings(final Element element) {
    element.setAttribute("ivyFile", ivyFile == null ? "" : ivyFile);
    element.setAttribute("useProjectSettings", Boolean.toString(useProjectSettings));
    element.setAttribute("useCustomIvySettings", Boolean.toString(useCustomIvySettings));
    element.setAttribute("ivySettingsFile", ivySettingsFile == null ? "" : ivySettingsFile);
    element.setAttribute(
        "onlyResolveSelectedConfigs", Boolean.toString(onlyResolveSelectedConfigs));
    if (configsToResolve != null && !configsToResolve.isEmpty()) {
      final Element configsElement = new Element("configsToResolve");
      for (final String configToResolve : configsToResolve) {
        configsElement.addContent(new Element("config").setText(configToResolve));
      }
      element.addContent(configsElement);
    }
  }
}
