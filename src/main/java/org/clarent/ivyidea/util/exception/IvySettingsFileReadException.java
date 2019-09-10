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

/**
 * Thrown when there was a problem while reading (accessing/parsing/...) the ivy settings file for a
 * module.
 *
 * @author Guy Mahieu
 */
public class IvySettingsFileReadException extends IvyIdeaException {

  private static final long serialVersionUID = 8589432092005779379L;
  @NotNull
  private final String fileName;
  @NotNull
  private final String moduleName;

  public IvySettingsFileReadException(
      @NotNull final String fileName,
      @NotNull final String moduleName,
      @NotNull final Throwable cause) {
    super(cause);
    this.fileName = fileName;
    this.moduleName = moduleName;
  }

  @NotNull
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
    return "An error occured while reading the ivy settings for module "
        + moduleName
        + " from "
        + fileName;
  }
}
