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

package org.clarent.ivyidea.logging;

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_DEBUG_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.LOG_ERROR_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.LOG_INFO_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.LOG_VERBOSE_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.LOG_WARNING_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT;

import com.intellij.execution.ui.ConsoleViewContentType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.ivy.util.Message;

@SuppressWarnings("ImmutableEnumChecker")
public enum IvyLogLevel {
  None(Integer.MIN_VALUE, SYSTEM_OUTPUT),
  Error(Message.MSG_ERR, LOG_ERROR_OUTPUT),
  Warning(Message.MSG_WARN, LOG_WARNING_OUTPUT),
  Info(Message.MSG_INFO, LOG_INFO_OUTPUT),
  Verbose(Message.MSG_VERBOSE, LOG_VERBOSE_OUTPUT),
  Debug(Message.MSG_DEBUG, LOG_DEBUG_OUTPUT),
  All(Integer.MAX_VALUE, SYSTEM_OUTPUT);

  private static final Map<Integer, IvyLogLevel> loglevels;

  static {
    final Map<Integer, IvyLogLevel> temp = new HashMap<>();
    for (final IvyLogLevel ivyLogLevel : values()) {
      temp.put(ivyLogLevel.levelCode, ivyLogLevel);
    }
    loglevels = Collections.unmodifiableMap(temp);
  }

  private final int levelCode;
  private final ConsoleViewContentType contentType;

  public static IvyLogLevel fromLevelCode(final int levelCode) {
    final IvyLogLevel level = loglevels.get(levelCode);
    return level == null ? None : level;
  }

  public static IvyLogLevel fromName(final String name) {
    try {
      return valueOf(name);
    } catch (final IllegalArgumentException e) {
      return None;
    }
  }

  IvyLogLevel(final int levelCode, final ConsoleViewContentType contentType) {
    this.levelCode = levelCode;
    this.contentType = contentType;
  }

  public boolean isMoreVerboseThan(final IvyLogLevel otherLevel) {
    return otherLevel.levelCode <= levelCode;
  }

  public ConsoleViewContentType getContentType() {
    return contentType;
  }
}
