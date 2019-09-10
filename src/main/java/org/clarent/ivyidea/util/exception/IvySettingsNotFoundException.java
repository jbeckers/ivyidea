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
 * Typically thrown when no ivy settings file can be found during the resolve process.
 *
 * @author Guy Mahieu
 */
public class IvySettingsNotFoundException extends IvyIdeaException {

  private static final long serialVersionUID = 4606478597395336177L;

  @NotNull
  private final ConfigLocation configLocation;
  @NotNull
  private final String configName;

  public IvySettingsNotFoundException(
      @NotNull final String message,
      @NotNull final ConfigLocation configLocation,
      @NotNull final String configName) {
    super(message);
    this.configLocation = configLocation;
    this.configName = configName;
  }

  @NotNull
  public ConfigLocation getConfigLocation() {
    return configLocation;
  }

  @NotNull
  public String getConfigName() {
    return configName;
  }

  public enum ConfigLocation {
    Project,
    Module
  }
}
