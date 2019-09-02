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
import com.intellij.ui.UserActivityWatcher;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.clarent.ivyidea.facet.IvyIdeaFacet;
import org.clarent.ivyidea.settings.ui.orderedfilelist.OrderedFileList;
import org.clarent.ivyidea.settings.ui.propertieseditor.PropertiesEditor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/** @author Guy Mahieu */
public class PropertiesSettingsTab extends FacetEditorTab {

  private final FacetEditorContext editorContext;

  @NotNull
  private final OrderedFileList orderedFileList = new OrderedFileList();

  private JPanel pnlRoot;
  private JPanel pnlPropertiesFiles;
  private JPanel pnlAdditionalProperties;
  private JLabel lblAdditionalPropertiesDescription;
  private JLabel lblAdditionalProperties;

  private boolean alreadyOpenedBefore = false;
  private boolean modified = false;

  public PropertiesSettingsTab(final FacetEditorContext editorContext) {
    this.editorContext = editorContext;
    orderedFileList.setProject(editorContext.getProject());

    /* No additional properties support yet in this release */
    pnlAdditionalProperties.setVisible(false);
    lblAdditionalProperties.setVisible(false);
    lblAdditionalPropertiesDescription.setVisible(false);
    /* -- */

    wireActivityWatcher();
  }

  private void wireActivityWatcher() {
    final UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.addUserActivityListener(() -> modified = true);
    watcher.register(pnlRoot);
  }

  @Override
  @Nls
  public String getDisplayName() {
    return "Properties (optional)";
  }

  @NotNull
  @Override
  public JComponent createComponent() {
    return pnlRoot;
  }

  @Override
  public boolean isModified() {
    return modified || orderedFileList.isModified();
  }

  public boolean isAlreadyOpenedBefore() {
    return alreadyOpenedBefore;
  }

  List<String> getFileNames() {
    return orderedFileList.getFileNames();
  }

  @Override
  public void apply() {
    ((IvyIdeaFacet) editorContext.getFacet())
        .getConfiguration()
        .getState()
        .propertiesSettings
        .propertiesFiles =
        orderedFileList.getFileNames();
  }

  @Override
  public void onTabEntering() {
    super.onTabEntering();
    alreadyOpenedBefore = true;
  }

  @Override
  public void reset() {
    orderedFileList.setFileNames(
        ((IvyIdeaFacet) editorContext.getFacet())
            .getConfiguration()
            .getState()
            .propertiesSettings
            .propertiesFiles);
  }

  @SuppressWarnings("UnusedMethod")
  private void createUIComponents() {
    pnlPropertiesFiles = new JPanel(new BorderLayout());
    pnlPropertiesFiles.add(orderedFileList.getRootPanel(), BorderLayout.CENTER);

    pnlAdditionalProperties = new JPanel(new BorderLayout());
    pnlAdditionalProperties.add(new PropertiesEditor().getRootPanel(), BorderLayout.CENTER);
  }
}
