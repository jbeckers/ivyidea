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
import java.util.stream.Collectors;
import javax.swing.table.AbstractTableModel;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
@SuppressWarnings("unused")
public class ConfigurationSelectionTableModel extends AbstractTableModel {

  private static final int COLUMN_SELECTION = 0;
  private static final int COLUMN_NAME = 1;
  private static final int COLUMN_DESCRIPTION = 2;

  private static final long serialVersionUID = -4485590409443411702L;

  @NotNull
  private final List<Configuration> configurations;
  @NotNull
  private final Set<Integer> selectedIndexes;
  private boolean editable = true;

  public ConfigurationSelectionTableModel() {
    this(Collections.emptyList(), Collections.emptySet());
  }

  public ConfigurationSelectionTableModel(@NotNull final Collection<Configuration> configurations) {
    this(configurations, new HashSet<>());
  }

  public ConfigurationSelectionTableModel(
      @NotNull final Collection<Configuration> configurations,
      @NotNull final Collection<String> selectedConfigNames) {
    this.configurations = new ArrayList<>(configurations);
    this.selectedIndexes =
        configurations.stream()
            .filter(configuration -> selectedConfigNames.contains(configuration.getName()))
            .map(this.configurations::indexOf)
            .collect(Collectors.toSet());
  }

  void setEditable(final boolean editable) {
    this.editable = editable;
  }

  @NotNull
  Set<Configuration> getSelectedConfigurations() {
    return selectedIndexes.stream().map(this::getConfigurationAt).collect(Collectors.toSet());
  }

  @NotNull
  Configuration getConfigurationAt(final int rowIndex) {
    return configurations.get(rowIndex);
  }

  @Override
  public int getRowCount() {
    return configurations.size();
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
      if ((Boolean) aValue) {
        selectedIndexes.add(rowIndex);
      } else {
        selectedIndexes.remove(rowIndex);
      }
    }
  }

  @Override
  @Nullable
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final Configuration configuration = getConfigurationAt(rowIndex);
    if (columnIndex == COLUMN_SELECTION) {
      return selectedIndexes.contains(rowIndex);
    }
    if (columnIndex == COLUMN_NAME) {
      return configuration.getName();
    }
    if (columnIndex == COLUMN_DESCRIPTION) {
      return configuration.getDescription();
    }
    return null;
  }
}
