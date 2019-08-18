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
import java.util.Collections;
import java.util.List;
import org.jdom.Element;

/** @author Guy Mahieu */
public class FacetPropertiesSettings implements JDOMExternalizable {

  private boolean includeProjectLevelPropertiesFiles = true;
  private boolean includeProjectLevelAdditionalProperties = true;
  private final List<String> propertyFiles = new ArrayList<>();

  public boolean isIncludeProjectLevelPropertiesFiles() {
    return includeProjectLevelPropertiesFiles;
  }

  public void setIncludeProjectLevelPropertiesFiles(
      final boolean includeProjectLevelPropertiesFiles) {
    this.includeProjectLevelPropertiesFiles = includeProjectLevelPropertiesFiles;
  }

  public boolean isIncludeProjectLevelAdditionalProperties() {
    return includeProjectLevelAdditionalProperties;
  }

  public void setIncludeProjectLevelAdditionalProperties(
      final boolean includeProjectLevelAdditionalProperties) {
    this.includeProjectLevelAdditionalProperties = includeProjectLevelAdditionalProperties;
  }

  public List<String> getPropertyFiles() {
    return Collections.unmodifiableList(propertyFiles);
  }

  public void setPropertyFiles(final List<String> propertyFiles) {
    this.propertyFiles.clear();
    this.propertyFiles.addAll(propertyFiles);
  }

  @Override
  public void readExternal(final Element propertiesSettingsElement) {
    final Element propertiesFilesElement = propertiesSettingsElement.getChild("propertiesFiles");
    final List<String> fileNames = new ArrayList<>();
    if (propertiesFilesElement != null) {
      includeProjectLevelPropertiesFiles =
          Boolean.parseBoolean(
              propertiesFilesElement.getAttributeValue(
                  "includeProjectLevelPropertiesFiles", Boolean.TRUE.toString()));
      for (final Element element : propertiesFilesElement.getChildren("fileName")) {
        fileNames.add(element.getValue());
      }
    }
    this.propertyFiles.clear();
    this.propertyFiles.addAll(fileNames);
  }

  @Override
  public void writeExternal(final Element propertiesSettingsElement) {
    final Element propertiesFilesElement = new Element("propertiesFiles");
    propertiesFilesElement.setAttribute(
        "includeProjectLevelPropertiesFiles", Boolean.toString(includeProjectLevelPropertiesFiles));
    propertiesSettingsElement.addContent(propertiesFilesElement);
    for (final String fileName : propertyFiles) {
      propertiesFilesElement.addContent(new Element("fileName").setText(fileName));
    }
  }
}
