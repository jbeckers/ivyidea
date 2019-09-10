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

import static org.clarent.ivyidea.model.dependency.DependencyCategory.Classes;
import static org.clarent.ivyidea.model.dependency.DependencyCategory.Javadoc;
import static org.clarent.ivyidea.model.dependency.DependencyCategory.Sources;

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
import org.clarent.ivyidea.settings.ui.orderedfilelist.OrderedFileList;
import org.clarent.ivyidea.util.ConsoleViewMessageLogger.IvyLogLevel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Guy Mahieu
 */
class IvyIdeaProjectConfigurable implements Configurable {

  @NotNull
  private final Supplier<IvyIdeaProjectSettingsPanel> settingsPanelSupplier;
  @Nullable
  private IvyIdeaProjectSettingsPanel settingsPanel;

  @Contract(pure = true)
  IvyIdeaProjectConfigurable(@NotNull final Project project) {
    this.settingsPanelSupplier =
        () -> {
          final IvyIdeaProjectSettingsPanel ivyIdeaProjectSettingsPanel =
              new IvyIdeaProjectSettingsPanel(project);
          ivyIdeaProjectSettingsPanel.setProject(project);
          return ivyIdeaProjectSettingsPanel;
        };
  }

  @NotNull
  @Override
  @Nls
  public String getDisplayName() {
    return IvyIdeaConstants.IVY_IDEA;
  }

  @Override
  public JComponent createComponent() {
    return getSettingsPanel().createComponent();
  }

  @NotNull
  private IvyIdeaProjectSettingsPanel getSettingsPanel() {
    if (settingsPanel == null) {
      settingsPanel = settingsPanelSupplier.get();
    }
    return settingsPanel;
  }

  @Override
  public boolean isModified() {
    return settingsPanel != null && getSettingsPanel().isModified();
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
  @SuppressWarnings("NullableProblems")
  static class IvyIdeaProjectSettingsPanel {

    @NotNull
    private final Project project;
    @NotNull
    private OrderedFileList orderedFileList;
    private boolean modified = false;
    @NotNull
    private TextFieldWithBrowseButton txtIvySettingsFile;
    @NotNull
    private JPanel projectSettingsPanel;
    @NotNull
    private JCheckBox chkValidateIvyFiles;
    @NotNull
    private JRadioButton useYourOwnIvySettingsRadioButton;
    @NotNull
    private JPanel pnlPropertiesFiles;
    @NotNull
    private ComboBox<IvyLogLevel> ivyLogLevelComboBox;
    @NotNull
    private JCheckBox includeModuleNameCheckBox;
    @NotNull
    private JCheckBox includeConfigurationNameCheckBox;
    @NotNull
    private JTextField txtClassesArtifactTypes;
    @NotNull
    private JTextField txtSourcesArtifactTypes;
    @NotNull
    private JTextField txtJavadocArtifactTypes;
    @NotNull
    private JCheckBox chkResolveTransitively;
    @NotNull
    private JCheckBox chkUseCacheOnly;
    @NotNull
    private JCheckBox chkBackground;
    @NotNull
    private JCheckBox autoAttachSources;
    @NotNull
    private JCheckBox autoAttachJavadocs;

    IvyIdeaProjectSettingsPanel(@NotNull final Project project) {
      this.project = project;
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

    @NotNull
    JComponent createComponent() {
      return projectSettingsPanel;
    }

    boolean isModified() {
      return modified;
    }

    void apply() {
      final IvyIdeaProjectState state = IvyIdeaProjectState.getInstance(project);
      state.setIvySettingsFile(txtIvySettingsFile.getText());
      state.setValidateIvyFiles(chkValidateIvyFiles.isSelected());
      state.setResolveTransitively(chkResolveTransitively.isSelected());
      state.setResolveCacheOnly(chkUseCacheOnly.isSelected());
      state.setResolveInBackground(chkBackground.isSelected());
      state.setAlwaysAttachSources(autoAttachSources.isSelected());
      state.setAlwaysAttachJavadocs(autoAttachJavadocs.isSelected());
      state.setUseCustomIvySettings(useYourOwnIvySettingsRadioButton.isSelected());
      state.setPropertyFiles(orderedFileList.getFileNames());
      state.setLibraryNameIncludesModule(includeModuleNameCheckBox.isSelected());
      state.setLibraryNameIncludesConfiguration(includeConfigurationNameCheckBox.isSelected());
      final Object selectedLogLevel = ivyLogLevelComboBox.getSelectedItem();
      state.setIvyLogLevelThreshold(
          selectedLogLevel == null ? IvyLogLevel.None.name() : selectedLogLevel.toString());
      state.setClassesTypes(txtClassesArtifactTypes.getText());
      state.setSourcesTypes(txtSourcesArtifactTypes.getText());
      state.setJavadocTypes(txtJavadocArtifactTypes.getText());
    }

    void reset() {
      final IvyIdeaProjectState state = IvyIdeaProjectState.getInstance(project);
      txtIvySettingsFile.setText(state.getIvySettingsFile());
      chkValidateIvyFiles.setSelected(state.isValidateIvyFiles());
      chkResolveTransitively.setSelected(state.isResolveTransitively());
      chkUseCacheOnly.setSelected(state.isResolveCacheOnly());
      chkBackground.setSelected(state.isResolveInBackground());
      autoAttachSources.setSelected(state.isAlwaysAttachSources());
      autoAttachJavadocs.setSelected(state.isAlwaysAttachJavadocs());
      useYourOwnIvySettingsRadioButton.setSelected(state.isUseCustomIvySettings());
      orderedFileList.setFileNames(state.getPropertyFiles());
      includeModuleNameCheckBox.setSelected(state.isLibraryNameIncludesModule());
      includeConfigurationNameCheckBox.setSelected(state.isLibraryNameIncludesConfiguration());
      ivyLogLevelComboBox.setSelectedItem(IvyLogLevel.fromName(state.getIvyLogLevelThreshold()));
      txtSourcesArtifactTypes
          .setText(state.getDependencyCategoryManager().getTypesStringForCategory(Sources));
      txtClassesArtifactTypes
          .setText(state.getDependencyCategoryManager().getTypesStringForCategory(Classes));
      txtJavadocArtifactTypes
          .setText(state.getDependencyCategoryManager().getTypesStringForCategory(Javadoc));
    }

    @SuppressWarnings("UnusedMethod")
    private void createUIComponents() {
      pnlPropertiesFiles = new JPanel(new BorderLayout());
      orderedFileList = new OrderedFileList();
      pnlPropertiesFiles.add(orderedFileList.getRootPanel(), BorderLayout.CENTER);
      ivyLogLevelComboBox = new ComboBox<>(IvyLogLevel.values());
    }

    void setProject(@NotNull final Project project) {
      orderedFileList.setProject(project);
    }
  }
}
