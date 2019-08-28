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

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.UserActivityWatcher;
import java.awt.BorderLayout;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.clarent.ivyidea.settings.IvyIdeaProjectStateComponent.State;
import org.clarent.ivyidea.settings.IvyIdeaProjectStateComponent.State.PropertiesSettings;
import org.clarent.ivyidea.settings.ui.orderedfilelist.OrderedFileList;
import org.clarent.ivyidea.util.ConsoleViewMessageLogger.IvyLogLevel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Guy Mahieu
 */
public class IvyIdeaProjectConfigurable implements Configurable {

  @NotNull
  private final Supplier<IvyIdeaProjectSettingsPanel> settingsPanelSupplier;
  @Nullable
  private IvyIdeaProjectSettingsPanel settingsPanel;

  public IvyIdeaProjectConfigurable(final Project project) {
    this.settingsPanelSupplier = () -> new IvyIdeaProjectSettingsPanel(project);
  }

  @Override
  @Nls
  public String getDisplayName() {
    return "IvyIDEA";
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
      settingsPanel = settingsPanelSupplier.get();
    }
    return settingsPanel;
  }

  @Override
  public boolean isModified() {
    return getSettingsPanel().isModified();
  }

  @Override
  public void apply() {
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

  /**
   * @author Guy Mahieu
   */
  public static class IvyIdeaProjectSettingsPanel {

    private final Project project;

    private boolean modified;

    private TextFieldWithBrowseButton txtIvySettingsFile;
    private JPanel projectSettingsPanel;
    private JCheckBox chkValidateIvyFiles;
    private JRadioButton useYourOwnIvySettingsRadioButton;
    private JPanel pnlPropertiesFiles;
    private ComboBox<IvyLogLevel> ivyLogLevelComboBox;
    private JCheckBox includeModuleNameCheckBox;
    private JCheckBox includeConfigurationNameCheckBox;
    private JTextField txtClassesArtifactTypes;
    private JTextField txtSourcesArtifactTypes;
    private JTextField txtJavadocArtifactTypes;
    private JCheckBox chkResolveTransitively;
    private JCheckBox chkUseCacheOnly;
    private JCheckBox chkBackground;
    private JCheckBox autoAttachSources;
    private JCheckBox autoAttachJavadocs;
    private State internalState;
    private OrderedFileList orderedFileList;

    IvyIdeaProjectSettingsPanel(final Project project) {
      this.project = project;
      this.internalState = project.getComponent(IvyIdeaProjectStateComponent.class).getState();

      txtIvySettingsFile.addBrowseFolderListener(
          "Select Ivy Settings File",
          null,
          project,
          new FileChooserDescriptor(true, false, false, false, false, false));

      wireActivityWatchers();
      wireIvySettingsRadioButtons();
    }

    private void wireIvySettingsRadioButtons() {
      useYourOwnIvySettingsRadioButton.addChangeListener(
          e -> txtIvySettingsFile.setEnabled(useYourOwnIvySettingsRadioButton.isSelected()));
    }

    private void wireActivityWatchers() {
      final UserActivityWatcher watcher = new UserActivityWatcher();
      watcher.addUserActivityListener(() -> modified = true);
      watcher.register(projectSettingsPanel);
    }

    JComponent createComponent() {
      return projectSettingsPanel;
    }

    boolean isModified() {
      return modified;
    }

    private List<String> getPropertiesFiles() {
      return orderedFileList.getFileNames();
    }

    private void setPropertiesFiles(final List<String> fileNames) {
      orderedFileList.setFileNames(fileNames);
    }

    void apply() {
      if (internalState == null) {
        internalState = new State();
      }
      internalState.setIvySettingsFile(txtIvySettingsFile.getText());
      internalState.setValidateIvyFiles(chkValidateIvyFiles.isSelected());
      internalState.setResolveTransitively(chkResolveTransitively.isSelected());
      internalState.setResolveCacheOnly(chkUseCacheOnly.isSelected());
      internalState.setResolveInBackground(chkBackground.isSelected());
      internalState.setAlwaysAttachSources(autoAttachSources.isSelected());
      internalState.setAlwaysAttachJavadocs(autoAttachJavadocs.isSelected());
      internalState.setUseCustomIvySettings(useYourOwnIvySettingsRadioButton.isSelected());
      final PropertiesSettings propertiesSettings = new PropertiesSettings();
      propertiesSettings.setPropertyFiles(getPropertiesFiles());
      internalState.setPropertiesSettings(propertiesSettings);
      internalState.setLibraryNameIncludesModule(includeModuleNameCheckBox.isSelected());
      internalState.setLibraryNameIncludesConfiguration(
          includeConfigurationNameCheckBox.isSelected());
      final Object selectedLogLevel = ivyLogLevelComboBox.getSelectedItem();
      internalState.setIvyLogLevelThreshold(
          selectedLogLevel == null ? IvyLogLevel.None.name() : selectedLogLevel.toString());
      internalState
          .getArtifactTypeSettings()
          .setTypesForCategory(Classes, txtClassesArtifactTypes.getText());
      internalState
          .getArtifactTypeSettings()
          .setTypesForCategory(Sources, txtSourcesArtifactTypes.getText());
      internalState
          .getArtifactTypeSettings()
          .setTypesForCategory(Javadoc, txtJavadocArtifactTypes.getText());
    }

    void reset() {
      State config = internalState;
      if (config == null) {
        config = new State();
      }
      txtIvySettingsFile.setText(config.getIvySettingsFile());
      chkValidateIvyFiles.setSelected(config.isValidateIvyFiles());
      chkResolveTransitively.setSelected(config.isResolveTransitively());
      chkUseCacheOnly.setSelected(config.isResolveCacheOnly());
      chkBackground.setSelected(config.isResolveInBackground());
      autoAttachSources.setSelected(config.isAlwaysAttachSources());
      autoAttachJavadocs.setSelected(config.isAlwaysAttachJavadocs());
      useYourOwnIvySettingsRadioButton.setSelected(config.isUseCustomIvySettings());
      setPropertiesFiles(config.getPropertiesSettings().getPropertyFiles());
      includeModuleNameCheckBox.setSelected(config.isLibraryNameIncludesModule());
      includeConfigurationNameCheckBox.setSelected(config.isLibraryNameIncludesConfiguration());
      ivyLogLevelComboBox.setSelectedItem(IvyLogLevel.fromName(config.getIvyLogLevelThreshold()));
      txtSourcesArtifactTypes.setText(
          config.getArtifactTypeSettings().getTypesStringForCategory(Sources));
      txtClassesArtifactTypes.setText(
          config.getArtifactTypeSettings().getTypesStringForCategory(Classes));
      txtJavadocArtifactTypes.setText(
          config.getArtifactTypeSettings().getTypesStringForCategory(Javadoc));
    }

    public void disposeUIResources() {
    }

    @SuppressWarnings("UnusedMethod")
    private void createUIComponents() {
      pnlPropertiesFiles = new JPanel(new BorderLayout());
      orderedFileList = new OrderedFileList(project);
      pnlPropertiesFiles.add(orderedFileList.getRootPanel(), BorderLayout.CENTER);
      ivyLogLevelComboBox = new ComboBox<>(IvyLogLevel.values());
    }
  }
}
