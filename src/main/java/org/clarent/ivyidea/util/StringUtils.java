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
package org.clarent.ivyidea.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public final class StringUtils {

  private StringUtils() {
  }

  public static boolean isBlank(String s) {
    return s == null || s.trim().length() == 0;
  }

  public static boolean isNotBlank(String s) {
    return !isBlank(s);
  }

  @Contract(value = "null -> null; !null -> !null", pure = true)
  @Nullable
  public static String trim(String s) {
    return s == null ? null : s.trim();
  }
}
