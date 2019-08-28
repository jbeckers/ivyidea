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

package org.clarent.ivyidea.facet.ui;

import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.UserActivityWatcher;
import io.vavr.control.Try;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.facet.ui.components.ConfigurationSelectionTable;
import org.clarent.ivyidea.facet.ui.components.ConfigurationSelectionTableModel;
import org.clarent.ivyidea.settings.IvyIdeaProjectStateComponent;
import org.clarent.ivyidea.util.IvyIdeaConfigHelper;
import org.clarent.ivyidea.util.IvyUtil;
import org.clarent.ivyidea.util.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.util.exception.IvySettingsNotFoundException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/** @author Guy Mahieu */
public class BasicSettingsTab extends FacetEditorTab {

  private static final Logger LOGGER =
      Logger.getInstance("#org.clarent.ivyidea.facet.ui.BasicSettingsTab");

  private final FacetEditorContext editorContext;
  private final PropertiesSettingsTab propertiesSettingsTab;
  private com.intellij.openapi.ui.TextFieldWithBrowseButton txtIvyFile;
  private JPanel pnlRoot;
  private JCheckBox chkOverrideProjectIvySettings;
  private TextFieldWithBrowseButton txtIvySettingsFile;
  private JCheckBox chkOnlyResolveSpecificConfigs;
  private ConfigurationSelectionTable tblConfigurationSelection;
  private JLabel lblIvyFileMessage;
  private JRadioButton rbnUseDefaultIvySettings;
  private JRadioButton rbnUseCustomIvySettings;
  private boolean modified;
  private boolean foundConfigsBefore = false;

  private Set<Configuration> selectedConfigurations = new HashSet<>();

  public BasicSettingsTab(
      @NotNull final FacetEditorContext editorContext,
      @NotNull final PropertiesSettingsTab propertiesSettingsTab) {
    this.editorContext = editorContext;
    this.propertiesSettingsTab = propertiesSettingsTab;
    this.propertiesSettingsTab.reset();

    final UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.addUserActivityListener(() -> modified = true);
    watcher.register(pnlRoot);

    txtIvyFile.addBrowseFolderListener(
        "Select Ivy File",
        "",
        editorContext.getProject(),
        new FileChooserDescriptor(true, false, false, false, false, false));
    txtIvySettingsFile.addBrowseFolderListener(
        "Select Ivy Settings File",
        "",
        editorContext.getProject(),
        new FileChooserDescriptor(true, false, false, false, false, false));

    txtIvyFile
        .getTextField()
        .getDocument()
        .addDocumentListener(
            new DocumentAdapter() {
              @Override
              public void textChanged(@NotNull final DocumentEvent e) {
                reloadIvyFile();
              }
            });
    chkOverrideProjectIvySettings.addChangeListener(e -> updateIvySettingsUIState());

    chkOnlyResolveSpecificConfigs.addChangeListener(e -> updateConfigurationsTable());

    rbnUseCustomIvySettings.addChangeListener(e -> updateIvySettingsFileTextfield());
  }

  @NotNull
  private static Set<String> getNames(@NotNull final Set<Configuration> selectedConfigurations) {
    final Set<String> result = new TreeSet<>();
    for (final Configuration selectedConfiguration : selectedConfigurations) {
      result.add(selectedConfiguration.getName());
    }
    return result;
  }

  private void updateUI() {
    updateIvySettingsFileTextfield();
    updateConfigurationsTable();
    updateIvySettingsUIState();
    reloadIvyFile();
  }

  private void updateIvySettingsFileTextfield() {
    txtIvySettingsFile.setEnabled(
        chkOverrideProjectIvySettings.isSelected() && rbnUseCustomIvySettings.isSelected());
  }

  private void updateConfigurationsTable() {
    tblConfigurationSelection.setEditable(chkOnlyResolveSpecificConfigs.isSelected());
  }

  private void updateIvySettingsUIState() {
    rbnUseCustomIvySettings.setEnabled(chkOverrideProjectIvySettings.isSelected());
    rbnUseDefaultIvySettings.setEnabled(chkOverrideProjectIvySettings.isSelected());
    updateIvySettingsFileTextfield();
  }

  @Override
  public void onTabEntering() {
    reloadIvyFile();
  }

  private void reloadIvyFile() {
    IvyUtil.loadConfigurations(
        txtIvyFile.getText(),
        IvyUtil.createConfiguredIvyEngine(
            this.editorContext.getModule(),
            IvyIdeaConfigHelper.createConfiguredIvySettings(
                this.editorContext.getModule(),
                this.getIvySettingsFileNameForCurrentSettingsInUI(),
                getPropertiesForCurrentSettingsInUI())))
        .onSuccess(
            configurations -> {
              chkOnlyResolveSpecificConfigs.setEnabled(true);
              LOGGER.info(
                  "LOG00070: Detected configs in file "
                      + txtIvyFile.getText()
                      + ": "
                      + configurations);
              tblConfigurationSelection.setModel(
                  new ConfigurationSelectionTableModel(
                      configurations, getNames(selectedConfigurations)));
              lblIvyFileMessage.setText("");
              foundConfigsBefore = true;
            })
        .onFailure(
            throwable -> {
              chkOnlyResolveSpecificConfigs.setEnabled(false);
              final File ivyFile = new File(txtIvyFile.getText());
              if (ivyFile.isDirectory() || !ivyFile.exists()) {
                lblIvyFileMessage.setText("Please enter the name of an existing ivy file.");
              } else {
                lblIvyFileMessage.setText(
                    "Warning: No configurations could be found in the given ivy file");
              }
              if (foundConfigsBefore) {
                selectedConfigurations = tblConfigurationSelection.getSelectedConfigurations();
              }
              tblConfigurationSelection.setModel(new ConfigurationSelectionTableModel());
              foundConfigsBefore = false;
              if (throwable instanceof ParseException) {
                // TODO: provide link to error display dialog with full exception
                lblIvyFileMessage.setText(
                    "Error parsing the file. If you use properties or specific ivy settings, configure those first.");
              }
              if (throwable instanceof IvySettingsNotFoundException) {
                lblIvyFileMessage.setText(
                    "Could not find the settings file. Configure the settings file here or in the project settings first.");
              }
              if (throwable instanceof IvySettingsFileReadException) {
                lblIvyFileMessage.setText(
                    "Error parsing the settings file. If you use properties, configure those first.");
              }
            });
  }

  @NotNull
  private Try<String> getIvySettingsFileNameForCurrentSettingsInUI() {
    if (chkOverrideProjectIvySettings.isSelected()) {
      if (rbnUseCustomIvySettings.isSelected()) {
        return Try.success(txtIvySettingsFile.getTextField().getText());
      } else {
        return Try.success(null);
      }
    } else {
      return IvyIdeaConfigHelper.getProjectIvySettingsFile(editorContext.getProject());
    }
  }

  @NotNull
  private Try<Properties> getPropertiesForCurrentSettingsInUI() {
    final List<String> propertiesFiles = new ArrayList<>(propertiesSettingsTab.getFileNames());
    // TODO: only include the project properties files if this option is chosen on the screen.
    //          for now this is not configurable yet - so it always is true
    final boolean includeProjectProperties = true;
    //noinspection ConstantConditions
    if (includeProjectProperties) {
      propertiesFiles.addAll(
          editorContext
              .getProject()
              .getComponent(IvyIdeaProjectStateComponent.class)
              .getState()
              .getPropertiesSettings()
              .getPropertyFiles());
    }
    return IvyIdeaConfigHelper.loadProperties(editorContext.getModule(), propertiesFiles);
  }

  @Override
  @Nls
  public String getDisplayName() {
    return "General";
  }

  @NotNull
  @Override
  public JComponent createComponent() {
    return pnlRoot;
  }

  @Override
  public boolean isModified() {
    return modified;
  }

  @Override
  public void apply() throws ConfigurationException {
    final Facet<?> facet = editorContext.getFacet();
    final IvyIdeaFacetConfiguration configuration =
        (IvyIdeaFacetConfiguration) facet.getConfiguration();
    configuration.setUseProjectSettings(!chkOverrideProjectIvySettings.isSelected());
    configuration.setUseCustomIvySettings(rbnUseCustomIvySettings.isSelected());
    configuration.setIvySettingsFile(txtIvySettingsFile.getText());
    configuration.setOnlyResolveSelectedConfigs(chkOnlyResolveSpecificConfigs.isSelected());
    configuration.setConfigsToResolve(
        getNames(tblConfigurationSelection.getSelectedConfigurations()));
    configuration.setIvyFile(txtIvyFile.getText());
  }

  @Override
  public void reset() {
    final Facet<?> facet = editorContext.getFacet();
    final IvyIdeaFacetConfiguration configuration =
        (IvyIdeaFacetConfiguration) facet.getConfiguration();
    txtIvyFile.setText(configuration.getIvyFile());
    chkOverrideProjectIvySettings.setSelected(!configuration.isUseProjectSettings());
    txtIvySettingsFile.setText(configuration.getIvySettingsFile());
    chkOnlyResolveSpecificConfigs.setSelected(configuration.isOnlyResolveSelectedConfigs());
    rbnUseCustomIvySettings.setSelected(configuration.isUseCustomIvySettings());
    rbnUseDefaultIvySettings.setSelected(!configuration.isUseCustomIvySettings());
    final Try<Set<Configuration>> allConfigurations =
        IvyUtil.loadConfigurations(
            txtIvyFile.getText(),
            IvyUtil.createConfiguredIvyEngine(
                this.editorContext.getModule(),
                IvyIdeaConfigHelper.createConfiguredIvySettings(
                    this.editorContext.getModule(),
                    this.getIvySettingsFileNameForCurrentSettingsInUI(),
                    getPropertiesForCurrentSettingsInUI())));
    if (configuration.getIvyFile().isEmpty()) {
      tblConfigurationSelection.setModel(new ConfigurationSelectionTableModel());
      selectedConfigurations = new HashSet<>();
      tblConfigurationSelection.setEditable(false);
    } else {
      allConfigurations
          .onSuccess(
              configurations ->
                  tblConfigurationSelection.setModel(
                      new ConfigurationSelectionTableModel(
                          configurations, configuration.getConfigsToResolve())))
          .onFailure(
              throwable ->
                  tblConfigurationSelection.setModel(new ConfigurationSelectionTableModel()));
      selectedConfigurations = tblConfigurationSelection.getSelectedConfigurations();
      updateConfigurationsTable();
    }
    updateUI();
  }

  @Override
  public void disposeUIResources() {}
}
