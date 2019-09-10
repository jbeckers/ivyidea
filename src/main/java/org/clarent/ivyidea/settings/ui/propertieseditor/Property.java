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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public class Property {

  @Nullable
  private String key;
  @Nullable
  private String value;

  @Nullable
  public String getValue() {
    return value;
  }

  public void setValue(@NotNull final String value) {
    this.value = value;
  }

  @Nullable
  String getKey() {
    return key;
  }

  void setKey(@NotNull final String key) {
    this.key = key;
  }
}
