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

package org.clarent.ivyidea.settings.ui.orderedfilelist;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.UserActivityWatcher;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration.State.FacetPropertiesSettings.FacetPropertiesFilesList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public class OrderedFileList {

  @Nullable
  private Project project = null;

  private JPanel pnlRoot;
  private JButton btnUp;
  private JButton btnRemove;
  private JButton btnDown;
  private JButton btnAdd;
  private JList<String> lstFileNames;
  private boolean modified = false;

  public OrderedFileList() {

    wireFileList();
    wireAddButton();
    wireRemoveButton();
    wireMoveUpButton();
    wireMoveDownButton();

    updateButtonStates();

    installActivityListener();
  }

  public void setProject(@NotNull final Project project) {
    this.project = project;
  }

  private void installActivityListener() {
    final UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.addUserActivityListener(() -> modified = true);
    watcher.register(pnlRoot);
  }

  private void wireFileList() {
    lstFileNames.setModel(new OrderedFileListModel());
    // TODO: implement multi select
    lstFileNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    lstFileNames.getSelectionModel().addListSelectionListener(e -> updateButtonStates());
    lstFileNames
        .getModel()
        .addListDataListener(
            new ListDataListener() {
              @Override
              public void intervalAdded(final ListDataEvent e) {
                updateButtonStates();
              }

              @Override
              public void intervalRemoved(final ListDataEvent e) {
                updateButtonStates();
              }

              @Override
              public void contentsChanged(final ListDataEvent e) {
                updateButtonStates();
              }
            });
  }

  private void updateButtonStates() {
    updateRemoveButtonState();
    updateMoveUpButtonState();
    updateMoveDownButtonState();
  }

  private void updateRemoveButtonState() {
    btnRemove.setEnabled(isRemoveAllowed());
  }

  private void updateMoveUpButtonState() {
    btnUp.setEnabled(isMoveUpAllowed());
  }

  private void updateMoveDownButtonState() {
    btnDown.setEnabled(isMoveDownAllowed());
  }

  private boolean isRemoveAllowed() {
    return lstFileNames.getModel().getSize() > 0 && lstFileNames.getSelectedIndex() > -1;
  }

  private boolean isMoveUpAllowed() {
    final int size = lstFileNames.getModel().getSize();
    return size > 1 && lstFileNames.getSelectedIndex() > 0;
  }

  private boolean isMoveDownAllowed() {
    final int size = lstFileNames.getModel().getSize();
    final int selectedIndex = lstFileNames.getSelectedIndex();
    return size > 1 && selectedIndex >= 0 && selectedIndex < size - 1;
  }

  private void wireAddButton() {
    btnAdd.addActionListener(
        e -> {
          final FileChooserDescriptor fcDescriptor =
              FileChooserDescriptorFactory.createMultipleFilesNoJarsDescriptor();
          fcDescriptor.setTitle("Select Properties File(S)");
          final VirtualFile[] files = FileChooser.chooseFiles(fcDescriptor, pnlRoot, project, null);
          for (final VirtualFile file : files) {
            addFilenameToList(file.getPresentableUrl());
          }
        });
  }

  private void wireRemoveButton() {
    btnRemove.addActionListener(e -> removeSelectedItemFromList());
  }

  private void wireMoveUpButton() {
    btnUp.addActionListener(e -> moveSelectedItemUp());
  }

  private void wireMoveDownButton() {
    btnDown.addActionListener(e -> moveSelectedItemDown());
  }

  private void addFilenameToList(final String fileName) {
    getFileListModel().add(fileName);
    modified = true;
  }

  private void removeSelectedItemFromList() {
    final int selectedIndex = lstFileNames.getSelectedIndex();
    getFileListModel().removeItemAt(selectedIndex);
    updateListSelection(selectedIndex);
    modified = true;
  }

  private void moveSelectedItemUp() {
    final int selectedIndex = lstFileNames.getSelectedIndex();
    getFileListModel().moveItemUp(selectedIndex);
    updateListSelection(selectedIndex - 1);
    modified = true;
  }

  private void moveSelectedItemDown() {
    final int selectedIndex = lstFileNames.getSelectedIndex();
    getFileListModel().moveItemDown(selectedIndex);
    updateListSelection(selectedIndex + 1);
    modified = true;
  }

  private void updateListSelection(final int indexToSelect) {
    if (indexToSelect >= 0) {
      if (indexToSelect < getFileListModel().getSize()) {
        lstFileNames.getSelectionModel().setSelectionInterval(indexToSelect, indexToSelect);
      } else {
        lstFileNames
            .getSelectionModel()
            .setSelectionInterval(
                getFileListModel().getSize() - 1, getFileListModel().getSize() - 1);
      }
    }
  }

  private OrderedFileListModel getFileListModel() {
    return (OrderedFileListModel) lstFileNames.getModel();
  }

  public boolean isModified() {
    return modified;
  }

  public FacetPropertiesFilesList getFileNames() {
    return getFileListModel().getAllItems();
  }

  public void setFileNames(final Collection<String> items) {
    getFileListModel().setItems(items);
  }

  public JPanel getRootPanel() {
    return pnlRoot;
  }
}
