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

package org.clarent.ivyidea.intellij.extension.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.clarent.ivyidea.config.model.IvyIdeaProjectSettings;
import org.clarent.ivyidea.intellij.extension.IvyIdeaProjectComponent;
import org.clarent.ivyidea.intellij.ui.IvyIdeaIcons;
import org.clarent.ivyidea.intellij.ui.IvyIdeaProjectSettingsPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public class IvyIdeaProjectSettingsComponent implements Configurable {

  private final Project project;

  @Nullable
  private IvyIdeaProjectSettingsPanel settingsPanel;

  public IvyIdeaProjectSettingsComponent(Project project) {
    this.project = project;
  }

  @Override
  @Nls
  public String getDisplayName() {
    return "IvyIDEA";
  }

  @Nullable
  public static Icon getIcon() {
    return IvyIdeaIcons.MAIN_ICON;
  }

  @Override
  @Nullable
  @NonNls
  public String getHelpTopic() {
    return null;
  }

  @Override
  public JComponent createComponent() {
    return getSettingsPanel().createComponent();
  }

  private IvyIdeaProjectSettingsPanel getSettingsPanel() {
    if (settingsPanel == null) {
      IvyIdeaProjectSettings state = project.getComponent(IvyIdeaProjectComponent.class).getState();
      settingsPanel = new IvyIdeaProjectSettingsPanel(project, state);
    }
    return settingsPanel;
  }

  @Override
  public boolean isModified() {
    return getSettingsPanel().isModified();
  }

  @Override
  public void apply() throws ConfigurationException {
    getSettingsPanel().apply();
  }

  @Override
  public void reset() {
    getSettingsPanel().reset();
  }

  @Override
  public void disposeUIResources() {
    settingsPanel = null;
  }
}
