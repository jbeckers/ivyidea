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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.UserActivityWatcher;
import java.awt.BorderLayout;
import java.util.function.Supplier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.clarent.ivyidea.IvyIdeaConstants;
import org.clarent.ivyidea.settings.IvyIdeaProjectStateComponent.IvyIdeaProjectState;
import org.clarent.ivyidea.settings.IvyIdeaProjectStateComponent.IvyIdeaProjectState.PropertiesSettings;
import org.clarent.ivyidea.settings.ui.orderedfilelist.OrderedFileList;
import org.clarent.ivyidea.util.ConsoleViewMessageLogger.IvyLogLevel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;
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

  @Contract(pure = true)
  public IvyIdeaProjectConfigurable(@NotNull final Project project) {
    this.settingsPanelSupplier = () -> new IvyIdeaProjectSettingsPanel(project);
  }

  @Override
  @Nls
  public String getDisplayName() {
    return IvyIdeaConstants.IVY_IDEA;
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

  /** @author Guy Mahieu */
  public static class IvyIdeaProjectSettingsPanel {

    @NotNull
    private final OrderedFileList orderedFileList = new OrderedFileList();
    @NotNull
    private final Project project;
    private boolean modified = false;
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

    IvyIdeaProjectSettingsPanel(@NotNull final Project project) {
      this.project = project;
      orderedFileList.setProject(project);
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

    void apply() {
      final IvyIdeaProjectState state =
          ServiceManager.getService(project, IvyIdeaProjectStateComponent.class).getState();
      state.ivySettingsFile = txtIvySettingsFile.getText();
      state.validateIvyFiles = chkValidateIvyFiles.isSelected();
      state.resolveTransitively = chkResolveTransitively.isSelected();
      state.resolveCacheOnly = chkUseCacheOnly.isSelected();
      state.resolveInBackground = chkBackground.isSelected();
      state.alwaysAttachSources = autoAttachSources.isSelected();
      state.alwaysAttachJavadocs = autoAttachJavadocs.isSelected();
      state.useCustomIvySettings = useYourOwnIvySettingsRadioButton.isSelected();
      final PropertiesSettings propertiesSettings = new PropertiesSettings();
      propertiesSettings.propertyFiles = orderedFileList.getFileNames();
      state.propertiesSettings = propertiesSettings;
      state.libraryNameIncludesModule = includeModuleNameCheckBox.isSelected();
      state.libraryNameIncludesConfiguration = includeConfigurationNameCheckBox.isSelected();
      final Object selectedLogLevel = ivyLogLevelComboBox.getSelectedItem();
      state.ivyLogLevelThreshold =
          selectedLogLevel == null ? IvyLogLevel.None.name() : selectedLogLevel.toString();
      state.artifactTypeSettings.setClassesTypes(txtClassesArtifactTypes.getText());
      state.artifactTypeSettings.setSourcesTypes(txtSourcesArtifactTypes.getText());
      state.artifactTypeSettings.setJavadocTypes(txtJavadocArtifactTypes.getText());
    }

    void reset() {
      final IvyIdeaProjectState internalState =
          ServiceManager.getService(project, IvyIdeaProjectStateComponent.class).getState();
      txtIvySettingsFile.setText(internalState.ivySettingsFile);
      chkValidateIvyFiles.setSelected(internalState.validateIvyFiles);
      chkResolveTransitively.setSelected(internalState.resolveTransitively);
      chkUseCacheOnly.setSelected(internalState.resolveCacheOnly);
      chkBackground.setSelected(internalState.resolveInBackground);
      autoAttachSources.setSelected(internalState.alwaysAttachSources);
      autoAttachJavadocs.setSelected(internalState.alwaysAttachJavadocs);
      useYourOwnIvySettingsRadioButton.setSelected(internalState.useCustomIvySettings);
      orderedFileList.setFileNames(internalState.propertiesSettings.propertyFiles);
      includeModuleNameCheckBox.setSelected(internalState.libraryNameIncludesModule);
      includeConfigurationNameCheckBox.setSelected(internalState.libraryNameIncludesConfiguration);
      ivyLogLevelComboBox.setSelectedItem(IvyLogLevel.fromName(internalState.ivyLogLevelThreshold));
      txtSourcesArtifactTypes.setText(
          internalState.artifactTypeSettings.getManager().getTypesStringForCategory(Sources));
      txtClassesArtifactTypes.setText(
          internalState.artifactTypeSettings.getManager().getTypesStringForCategory(Classes));
      txtJavadocArtifactTypes.setText(
          internalState.artifactTypeSettings.getManager().getTypesStringForCategory(Javadoc));
    }

    @SuppressWarnings("UnusedMethod")
    private void createUIComponents() {
      pnlPropertiesFiles = new JPanel(new BorderLayout());
      pnlPropertiesFiles.add(orderedFileList.getRootPanel(), BorderLayout.CENTER);
      ivyLogLevelComboBox = new ComboBox<>(IvyLogLevel.values());
    }
  }
}
