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

package org.clarent.ivyidea.model.dependency;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ImmutableEnumChecker")
public enum DependencyCategory {
  Sources("source", "src", "sources", "srcs"),
  Javadoc("javadoc", "doc", "docs", "apidoc", "apidocs", "documentation", "documents"),
  Classes("jar", "mar", "sar", "war", "ear", "ejb", "bundle", "test-jar");

  @NotNull
  private final List<String> defaultTypes;

  DependencyCategory(final String... types) {
    this.defaultTypes = Collections.unmodifiableList(asList(types));
  }

  @Contract(pure = true)
  @NotNull
  public List<String> getDefaultTypes() {
    return defaultTypes;
  }
}
