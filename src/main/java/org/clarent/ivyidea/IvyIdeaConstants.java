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

package org.clarent.ivyidea;

import com.intellij.facet.FacetTypeId;
import org.clarent.ivyidea.facet.IvyIdeaFacet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class IvyIdeaConstants {

  @NotNull
  public static final String IVY_IDEA = "IvyIDEA";

  @NotNull
  public static final String TOOLWINDOW_ID = IVY_IDEA;

  @NotNull
  public static final String NOTIFICATION_GROUP_DISPLAY_ID = IVY_IDEA;

  @NotNull
  public static final String PROJECT_STATE_NAME = IVY_IDEA + ".ProjectSettings";

  @NotNull
  public static final String RESOLVED_LIB_NAME_ROOT = IVY_IDEA;

  @NotNull
  public static final FacetTypeId<IvyIdeaFacet> FACET_TYPE_ID = new FacetTypeId<>(IVY_IDEA);

  @NotNull
  public static final String FACET_STRING_ID = IVY_IDEA;
  @NotNull
  public static final String FACET_DETECTOR_ID = IVY_IDEA;

  @NotNull
  public static final String[] ZERO_LENGTH_STRING_ARRAY = new String[0];

  @Contract(pure = true)
  private IvyIdeaConstants() {
  }
}
