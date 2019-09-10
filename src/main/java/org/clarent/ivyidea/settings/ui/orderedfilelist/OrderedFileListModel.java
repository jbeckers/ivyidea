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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
class OrderedFileListModel extends AbstractListModel<String> {

  private static final long serialVersionUID = 1009368465378149477L;

  @NotNull
  private final List<String> items = new ArrayList<>();

  @NotNull
  List<String> getAllItems() {
    return Collections.unmodifiableList(items);
  }

  void setItems(@Nullable final Collection<String> itemsToSet) {
    if (itemsToSet != null) {
      clear();
      items.addAll(itemsToSet);
      fireIntervalAdded(this, items.size() - itemsToSet.size(), items.size());
    }
  }

  void add(@NotNull final String item) {
    items.add(item);
    fireIntervalAdded(this, items.size(), items.size());
  }

  void removeItemAt(final int index) {
    if (index >= 0 && index < items.size()) {
      items.remove(index);
      fireIntervalRemoved(this, index, index);
    }
  }

  void moveItemUp(final int index) {
    if (index > 0 && index < items.size()) {
      final String item = items.remove(index);
      items.add(index - 1, item);
      fireContentsChanged(this, index - 1, index);
    }
  }

  void moveItemDown(final int index) {
    if (index >= 0 && index < items.size() - 1) {
      final String item = items.remove(index);
      items.add(index + 1, item);
      fireContentsChanged(this, index, index + 1);
    }
  }

  private void clear() {
    final int nrOfItemsBeforeClear = items.size();
    items.clear();
    fireContentsChanged(this, 0, nrOfItemsBeforeClear);
  }

  @Override
  public int getSize() {
    return items.size();
  }

  @NotNull
  @Override
  public String getElementAt(final int index) {
    return items.get(index);
  }
}
