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

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.UserActivityWatcher;
import io.vavr.control.Try;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.clarent.ivyidea.facet.IvyIdeaFacet;
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.facet.ui.components.ConfigurationSelectionTable;
import org.clarent.ivyidea.facet.ui.components.ConfigurationSelectionTableModel;
import org.clarent.ivyidea.settings.IvyIdeaProjectState;
import org.clarent.ivyidea.util.IvyIdeaConfigUtil;
import org.clarent.ivyidea.util.IvyUtil;
import org.clarent.ivyidea.util.ModuleDescriptorUtil;
import org.clarent.ivyidea.util.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.util.exception.IvySettingsNotFoundException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/** @author Guy Mahieu */
public class BasicSettingsTab extends FacetEditorTab {

  @NotNull
  private static final Logger LOGGER =
      Logger.getInstance("#org.clarent.ivyidea.facet.ui.BasicSettingsTab");

  @NotNull
  private final FacetEditorContext editorContext;
  @NotNull
  private final PropertiesSettingsTab propertiesSettingsTab;
  @NotNull
  private TextFieldWithBrowseButton txtIvyFile;
  @NotNull
  private JPanel pnlRoot;
  @NotNull
  private JCheckBox chkOverrideProjectIvySettings;
  @NotNull
  private TextFieldWithBrowseButton txtIvySettingsFile;
  @NotNull
  private JCheckBox chkOnlyResolveSpecificConfigs;
  @NotNull
  private ConfigurationSelectionTable tblConfigurationSelection;
  @NotNull
  private JLabel lblIvyFileMessage;
  @NotNull
  private JRadioButton rbnUseDefaultIvySettings;
  @NotNull
  private JRadioButton rbnUseCustomIvySettings;
  private boolean modified = false;
  private boolean foundConfigsBefore = false;

  @NotNull
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

  /**
   * Gives a set of configurations defined in the given ivyFileName. Will never throw an exception,
   * if something goes wrong, null is returned
   *
   * @param ivyFileName the name of the ivy file to parse
   * @param ivy the Ivy engine to use, configured with the appropriate settings
   * @return a set of configurations, null if anything went wrong parsing the ivy file
   */
  @NotNull
  private static Try<Set<Configuration>> loadConfigurations(
      @NotNull final String ivyFileName, @NotNull final Try<? extends Ivy> ivy) {
    try {
      final File file = new File(ivyFileName);
      if (file.exists() && !file.isDirectory()) {
        final Try<ModuleDescriptor> md = ModuleDescriptorUtil.parseDescriptor(file, ivy);
        return md.mapTry(
            moduleDescriptor -> {
              final Set<Configuration> result =
                  new TreeSet<>((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
              result.addAll(Arrays.asList(moduleDescriptor.getConfigurations()));
              return result;
            });
      } else {
        return Try.success(Collections.emptySet());
      }
    } catch (final RuntimeException e) {
      // Not able to parse module descriptor; no problem here...
      LOGGER.info(
          "LOG00180: Error while parsing ivy file during attempt to load configurations from it: "
              + e);
      if (e.getCause() instanceof ParseException) {
        return Try.failure(e.getCause());
      }
      return Try.success(Collections.emptySet());
    }
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
    loadConfigurations(
        txtIvyFile.getText(),
        IvyUtil.newInstance(
            this.editorContext.getModule(),
            IvyIdeaConfigUtil.createConfiguredIvySettings(
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
                      configurations,
                      selectedConfigurations.stream()
                          .map(Configuration::getName)
                          .collect(Collectors.toSet())));
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
      return IvyIdeaConfigUtil.getProjectIvySettingsFile(editorContext.getProject());
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
          IvyIdeaProjectState.getInstance(editorContext.getProject()).getPropertyFiles());
    }
    return IvyIdeaConfigUtil.loadProperties(editorContext.getModule(), propertiesFiles);
  }

  @NotNull
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
  public void apply() {
    final IvyIdeaFacetConfiguration configuration =
        IvyIdeaFacetConfiguration.getInstance((IvyIdeaFacet) editorContext.getFacet());
    configuration.setUseProjectSettings(!chkOverrideProjectIvySettings.isSelected());
    configuration.setUseCustomIvySettings(rbnUseCustomIvySettings.isSelected());
    configuration.setIvySettingsFile(txtIvySettingsFile.getText());
    configuration.setOnlyResolveSelectedConfigs(chkOnlyResolveSpecificConfigs.isSelected());
    configuration.setConfigsToResolve(
        tblConfigurationSelection.getSelectedConfigurations().stream()
            .map(Configuration::getName)
            .collect(Collectors.toSet()));
    configuration.setIvyFile(txtIvyFile.getText());
  }

  @Override
  public void reset() {
    final IvyIdeaFacetConfiguration configuration =
        IvyIdeaFacetConfiguration.getInstance((IvyIdeaFacet) editorContext.getFacet());
    txtIvyFile.setText(configuration.getIvyFile());
    chkOverrideProjectIvySettings.setSelected(!configuration.isUseProjectSettings());
    txtIvySettingsFile.setText(configuration.getIvySettingsFile());
    chkOnlyResolveSpecificConfigs.setSelected(configuration.isOnlyResolveSelectedConfigs());
    rbnUseCustomIvySettings.setSelected(configuration.isUseCustomIvySettings());
    rbnUseDefaultIvySettings.setSelected(!configuration.isUseCustomIvySettings());

    if (configuration.getIvyFile().isEmpty()) {
      tblConfigurationSelection.setModel(new ConfigurationSelectionTableModel());
      selectedConfigurations = new HashSet<>();
      tblConfigurationSelection.setEditable(false);
    } else {
      loadConfigurations(
          configuration.getIvyFile(),
          IvyUtil.newInstance(
              this.editorContext.getModule(),
              IvyIdeaConfigUtil.createConfiguredIvySettings(
                  this.editorContext.getModule(),
                  this.getIvySettingsFileNameForCurrentSettingsInUI(),
                  getPropertiesForCurrentSettingsInUI())))
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
