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

package org.clarent.ivyidea.settings.ui.propertieseditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
class PropertiesTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 6153216757094248324L;

  @NotNull
  private final List<Property> data;

  PropertiesTableModel() {
    this.data = new ArrayList<>();
  }

  PropertiesTableModel(@NotNull final Collection<Property> data) {
    this.data = new ArrayList<>(data);
  }

  @NotNull
  public Property getPropertyAt(final int rowIndex) {
    return data.get(rowIndex);
  }

  @Override
  public int getRowCount() {
    return data.size();
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return true;
  }

  @Override
  public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
    final String value = aValue == null ? "" : aValue.toString();
    while (rowIndex > data.size()) {
      data.add(new Property());
    }
    if (columnIndex == 0) {
      data.get(rowIndex).setKey(value);
    }
    if (columnIndex == 1) {
      data.get(rowIndex).setValue(value);
    }
  }

  @Nullable
  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (columnIndex == 0) {
      return data.get(rowIndex).getKey();
    }
    if (columnIndex == 1) {
      return data.get(rowIndex).getValue();
    }
    throw new IllegalArgumentException("columnIndex is out of range:" + columnIndex);
  }
}
