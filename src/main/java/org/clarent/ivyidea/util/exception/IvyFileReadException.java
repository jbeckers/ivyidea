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

package org.clarent.ivyidea.util.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown when there was a problem while reading (accessing/parsing/...) the ivy xml file for a
 * module.
 *
 * @author Guy Mahieu
 */
@SuppressWarnings("unused")
public class IvyFileReadException extends IvyIdeaException {

  private static final long serialVersionUID = 2569486467585035102L;

  @Nullable
  private final String fileName;
  @NotNull
  private final String moduleName;

  public IvyFileReadException(
      @Nullable final String fileName,
      @NotNull final String moduleName,
      @Nullable final Throwable cause) {
    super(cause);
    this.fileName = fileName;
    this.moduleName = moduleName;
  }

  @Nullable
  public String getFileName() {
    return fileName;
  }

  @NotNull
  public String getModuleName() {
    return moduleName;
  }

  @NotNull
  @Override
  public String getMessage() {
    if (fileName == null) {
      return "No ivy file specified for module " + moduleName;
    } else {
      return "Exception while reading ivy file " + fileName + " for module " + moduleName;
    }
  }
}
