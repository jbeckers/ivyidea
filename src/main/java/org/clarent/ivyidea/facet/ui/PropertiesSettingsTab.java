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

  @NotNull
  private final FacetEditorContext editorContext;

  @NotNull
  private final OrderedFileList orderedFileList = new OrderedFileList();

  @NotNull
  private JPanel pnlRoot;
  @NotNull
  private JPanel pnlPropertiesFiles;
  @NotNull
  private JPanel pnlAdditionalProperties;
  @NotNull
  private JLabel lblAdditionalPropertiesDescription;
  @NotNull
  private JLabel lblAdditionalProperties;

  private boolean alreadyOpenedBefore = false;
  private boolean modified = false;

  public PropertiesSettingsTab(@NotNull final FacetEditorContext editorContext) {
    this.editorContext = editorContext;
    pnlPropertiesFiles.setLayout(new BorderLayout());
    orderedFileList.setProject(editorContext.getProject());
    pnlPropertiesFiles.add(orderedFileList.getRootPanel(), BorderLayout.CENTER);

    pnlAdditionalProperties.setLayout(new BorderLayout());
    pnlAdditionalProperties.add(new PropertiesEditor().getRootPanel(), BorderLayout.CENTER);
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

  @NotNull
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

  @NotNull
  List<String> getFileNames() {
    return orderedFileList.getFileNames();
  }

  @Override
  public void apply() {
    ((IvyIdeaFacet) editorContext.getFacet())
        .getConfiguration()
        .getState()
        .setPropertiesFiles(orderedFileList.getFileNames());
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
            .getPropertiesFiles());
  }
}
