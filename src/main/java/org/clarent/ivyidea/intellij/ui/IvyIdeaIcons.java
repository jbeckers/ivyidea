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

package org.clarent.ivyidea.intellij.ui;

import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;
import org.jetbrains.annotations.Nullable;

/**
 * Helper interface for easy access to icons used in IvyIDEA.
 *
 * @author Guy Mahieu
 */
public final class IvyIdeaIcons {

  @Nullable
  public static final Icon MAIN_ICON_SMALL = IconLoader.findIcon("/ivyidea13.png");
  @Nullable
  public static final Icon MAIN_ICON = IconLoader.findIcon("/ivyidea32.png");

  private IvyIdeaIcons() {
  }

  //    public static final Icon ERROR_ICON = IconLoader.findIcon("/compiler/error.png");

}
