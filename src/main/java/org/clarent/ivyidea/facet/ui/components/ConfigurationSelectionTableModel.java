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

package org.clarent.ivyidea.facet.ui.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public class ConfigurationSelectionTableModel extends AbstractTableModel {

  private static final int COLUMN_SELECTION = 0;
  private static final int COLUMN_NAME = 1;
  private static final int COLUMN_DESCRIPTION = 2;

  private static final long serialVersionUID = -4485590409443411702L;

  private final List<Configuration> data;
  private final Set<Integer> selectedIndexes;
  private boolean editable = true;

  public ConfigurationSelectionTableModel() {
    this.data = Collections.emptyList();
    this.selectedIndexes = Collections.emptySet();
  }

  public ConfigurationSelectionTableModel(final Collection<Configuration> data) {
    this.data = new ArrayList<>(data);
    this.selectedIndexes = new HashSet<>();
  }

  public ConfigurationSelectionTableModel(
      final Collection<Configuration> data, final Collection<String> selectedConfigNames) {
    this.data = new ArrayList<>(data);
    this.selectedIndexes = buildSelectedIndexes(this.data, selectedConfigNames);
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  public Set<Configuration> getSelectedConfigurations() {
    final Set<Configuration> result = new HashSet<>();
    for (final Integer selectedIndex : selectedIndexes) {
      result.add(getConfigurationAt(selectedIndex));
    }
    return result;
  }

  public Configuration getConfigurationAt(final int rowIndex) {
    return data.get(rowIndex);
  }

  @Override
  public int getRowCount() {
    return data.size();
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return editable && columnIndex == 0;
  }

  @Override
  public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
    if (columnIndex == COLUMN_SELECTION && aValue instanceof Boolean) {
      final boolean checked = (Boolean) aValue;
      if (checked) {
        selectRow(rowIndex);
      } else {
        unselectRow(rowIndex);
      }
    }
  }

  private void unselectRow(final int rowIndex) {
    selectedIndexes.remove(rowIndex);
  }

  private void selectRow(final int rowIndex) {
    selectedIndexes.add(rowIndex);
  }

  @Override
  @Nullable
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final Configuration configuration = getConfigurationAt(rowIndex);
    if (columnIndex == COLUMN_SELECTION) {
      return isRowSelected(rowIndex);
    }
    if (columnIndex == COLUMN_NAME) {
      return configuration.getName();
    }
    if (columnIndex == COLUMN_DESCRIPTION) {
      return configuration.getDescription();
    }
    return null;
  }

  private boolean isRowSelected(final int rowIndex) {
    return selectedIndexes.contains(rowIndex);
  }

  private static Set<Integer> buildSelectedIndexes(
      @NotNull final List<Configuration> configurations,
      @NotNull final Collection<String> selectedConfigNames) {
    final HashSet<Integer> result = new HashSet<>();
    for (final Configuration configuration : configurations) {
      if (selectedConfigNames.contains(configuration.getName())) {
        result.add(configurations.indexOf(configuration));
      }
    }
    return result;
  }
}
