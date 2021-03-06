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

import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.table.JBTable;
import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Table to allow the user to configure the configurations that need to be resolved from within
 * IntelliJ IDEA.
 *
 * @author Guy Mahieu
 */
public class ConfigurationSelectionTable extends JBTable {

  private static final long serialVersionUID = 7190498625135720513L;

  private boolean editable = true;

  public ConfigurationSelectionTable() {
    super(new ConfigurationSelectionTableModel());
    initComponents();
  }

  @Override
  public void setModel(@NotNull final TableModel dataModel) {
    super.setModel(dataModel);
    ((ConfigurationSelectionTableModel) dataModel).setEditable(editable);
    initComponents();
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
    ((ConfigurationSelectionTableModel) dataModel).setEditable(editable);

    initComponents();
    revalidate();
    repaint();
  }

  @NotNull
  public Set<Configuration> getSelectedConfigurations() {
    return ((ConfigurationSelectionTableModel) getModel()).getSelectedConfigurations();
  }

  private void initComponents() {
    setRowSelectionAllowed(false);
    setColumnSelectionAllowed(false);

    getColumnModel().getColumn(0).setPreferredWidth(30);
    getColumnModel().getColumn(0).setMaxWidth(30);
    getColumnModel().getColumn(1).setPreferredWidth(120);
    getColumnModel().getColumn(2).setPreferredWidth(400);

    getColumnModel().getColumn(0).setHeaderValue("");
    getColumnModel().getColumn(1).setHeaderValue("Name");
    getColumnModel().getColumn(2).setHeaderValue("Description");

    // Render checkbox disabled if table is disabled
    getColumnModel().getColumn(0).setCellRenderer(new BooleanTableCellRenderer());
    getColumnModel().getColumn(0).setCellEditor(new BooleanTableCellEditor());

    // Register custom renderer to draw deprecated configs in 'strikethrough'
    getColumnModel().getColumn(1).setCellRenderer(new TableCellRenderer());

    // Render description disabled if table is disabled
    getColumnModel().getColumn(2).setCellRenderer(new BooleanTableCellRenderer());
  }

  private final class BooleanTableCellRenderer extends com.intellij.ui.BooleanTableCellRenderer {

    private static final long serialVersionUID = -408023647541058532L;

    @NotNull
    @Override
    public Component getTableCellRendererComponent(
        final JTable table,
        final Object value,
        final boolean isSelected,
        final boolean hasFocus,
        final int row,
        final int column) {
      final Component rendererComponent =
          super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      rendererComponent.setEnabled(editable);
      return rendererComponent;
    }
  }

  private final class TableCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1661620308397003067L;
    @Nullable
    private Font regularFont;
    @Nullable
    private Font strikethroughFont;

    @NotNull
    @Override
    public Component getTableCellRendererComponent(
        final JTable table,
        final Object value,
        final boolean isSelected,
        final boolean hasFocus,
        final int row,
        final int column) {
      final Component rendererComponent =
          super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if (regularFont == null) {
        regularFont = rendererComponent.getFont();
      }
      final Configuration configuration =
          ((ConfigurationSelectionTableModel) table.getModel()).getConfigurationAt(row);
      if (configuration.getDeprecated() != null) {
        if (strikethroughFont == null && regularFont != null) {
          final Map<TextAttribute, Object> attribs = new LinkedHashMap<>();
          attribs.put(TextAttribute.STRIKETHROUGH, Boolean.TRUE);
          strikethroughFont = regularFont.deriveFont(attribs);
        }
        setToolTipText("Depracated: " + configuration.getDeprecated());
        rendererComponent.setFont(strikethroughFont);
      } else {
        setToolTipText(null);
        rendererComponent.setFont(regularFont);
      }
      rendererComponent.setEnabled(editable);
      return rendererComponent;
    }
  }
}
