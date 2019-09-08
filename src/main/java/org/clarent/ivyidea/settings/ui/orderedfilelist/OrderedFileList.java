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
import java.util.List;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public class OrderedFileList {

  @Nullable
  private Project project = null;

  @NotNull
  private JPanel pnlRoot;
  @NotNull
  private JButton btnUp;
  @NotNull
  private JButton btnRemove;
  @NotNull
  private JButton btnDown;
  @NotNull
  private JButton btnAdd;
  @NotNull
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
              public void intervalAdded(@NotNull final ListDataEvent e) {
                updateButtonStates();
              }

              @Override
              public void intervalRemoved(@NotNull final ListDataEvent e) {
                updateButtonStates();
              }

              @Override
              public void contentsChanged(@NotNull final ListDataEvent e) {
                updateButtonStates();
              }
            });
  }

  private void updateButtonStates() {
    btnRemove.setEnabled(
        lstFileNames.getModel().getSize() > 0 && lstFileNames.getSelectedIndex() > -1);
    btnUp.setEnabled(lstFileNames.getModel().getSize() > 1 && lstFileNames.getSelectedIndex() > 0);
    btnDown.setEnabled(
        lstFileNames.getModel().getSize() > 1
            && lstFileNames.getSelectedIndex() >= 0
            && lstFileNames.getSelectedIndex() < lstFileNames.getModel().getSize() - 1);
  }

  private void wireAddButton() {
    btnAdd.addActionListener(
        e -> {
          final FileChooserDescriptor fcDescriptor =
              FileChooserDescriptorFactory.createMultipleFilesNoJarsDescriptor();
          fcDescriptor.setTitle("Select Properties File(S)");
          final VirtualFile[] files = FileChooser.chooseFiles(fcDescriptor, pnlRoot, project, null);
          for (final VirtualFile file : files) {
            getFileListModel().add(file.getPresentableUrl());
            modified = true;
          }
        });
  }

  private void wireRemoveButton() {
    btnRemove.addActionListener(e -> {
      final int selectedIndex = lstFileNames.getSelectedIndex();
      getFileListModel().removeItemAt(selectedIndex);
      updateListSelection(selectedIndex);
      modified = true;
    });
  }

  private void wireMoveUpButton() {
    btnUp.addActionListener(e -> {
      final int selectedIndex = lstFileNames.getSelectedIndex();
      getFileListModel().moveItemUp(selectedIndex);
      updateListSelection(selectedIndex - 1);
      modified = true;
    });
  }

  private void wireMoveDownButton() {
    btnDown.addActionListener(e -> {
      final int selectedIndex = lstFileNames.getSelectedIndex();
      getFileListModel().moveItemDown(selectedIndex);
      updateListSelection(selectedIndex + 1);
      modified = true;
    });
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

  @NotNull
  private OrderedFileListModel getFileListModel() {
    return (OrderedFileListModel) lstFileNames.getModel();
  }

  public boolean isModified() {
    return modified;
  }

  @NotNull
  public List<String> getFileNames() {
    return getFileListModel().getAllItems();
  }

  public void setFileNames(@Nullable final Collection<String> items) {
    getFileListModel().setItems(items);
  }

  @NotNull
  public JPanel getRootPanel() {
    return pnlRoot;
  }
}
