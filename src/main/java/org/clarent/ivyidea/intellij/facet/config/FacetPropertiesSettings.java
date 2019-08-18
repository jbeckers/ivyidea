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

import com.intellij.openapi.util.JDOMExternalizable;
import java.util.ArrayList;
import java.util.List;
import org.clarent.ivyidea.config.model.PropertiesSettings;
import org.jdom.Element;

/** @author Guy Mahieu */
public class FacetPropertiesSettings extends PropertiesSettings implements JDOMExternalizable {

  private boolean includeProjectLevelPropertiesFiles = true;
  private boolean includeProjectLevelAdditionalProperties = true;

  public boolean isIncludeProjectLevelPropertiesFiles() {
    return includeProjectLevelPropertiesFiles;
  }

  public void setIncludeProjectLevelPropertiesFiles(boolean includeProjectLevelPropertiesFiles) {
    this.includeProjectLevelPropertiesFiles = includeProjectLevelPropertiesFiles;
  }

  public boolean isIncludeProjectLevelAdditionalProperties() {
    return includeProjectLevelAdditionalProperties;
  }

  public void setIncludeProjectLevelAdditionalProperties(
      boolean includeProjectLevelAdditionalProperties) {
    this.includeProjectLevelAdditionalProperties = includeProjectLevelAdditionalProperties;
  }

  @Override
  public void readExternal(Element propertiesSettingsElement) {
    final Element propertiesFilesElement = propertiesSettingsElement.getChild("propertiesFiles");
    List<String> fileNames = new ArrayList<>();
    if (propertiesFilesElement != null) {
      setIncludeProjectLevelPropertiesFiles(
          Boolean.valueOf(
              propertiesFilesElement.getAttributeValue(
                  "includeProjectLevelPropertiesFiles", Boolean.TRUE.toString())));
      @SuppressWarnings("unchecked") final List<Element> propertiesFileNames =
          propertiesFilesElement.getChildren("fileName");
      for (Element element : propertiesFileNames) {
        fileNames.add(element.getValue());
      }
    }
    setPropertyFiles(fileNames);
  }

  @Override
  public void writeExternal(Element propertiesSettingsElement) {
    final Element propertiesFilesElement = new Element("propertiesFiles");
    propertiesFilesElement.setAttribute(
        "includeProjectLevelPropertiesFiles",
        Boolean.toString(isIncludeProjectLevelPropertiesFiles()));
    propertiesSettingsElement.addContent(propertiesFilesElement);
    for (String fileName : getPropertyFiles()) {
      propertiesFilesElement.addContent(new Element("fileName").setText(fileName));
    }
  }
}
