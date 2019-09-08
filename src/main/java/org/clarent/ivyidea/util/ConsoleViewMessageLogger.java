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

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_DEBUG_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.LOG_ERROR_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.LOG_INFO_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.LOG_VERBOSE_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.LOG_WARNING_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import org.apache.ivy.util.AbstractMessageLogger;
import org.apache.ivy.util.Message;
import org.clarent.ivyidea.IvyIdeaConstants;
import org.clarent.ivyidea.settings.IvyIdeaProjectState;
import org.clarent.ivyidea.toolwindow.IvyIdeaToolWindowFactory.IvyIdeaToolWindow;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ImmutableEnumChecker")
public class ConsoleViewMessageLogger extends AbstractMessageLogger {

  @Nullable
  private final ConsoleView consoleView;
  @NotNull
  private final IvyLogLevel threshold;

  ConsoleViewMessageLogger(@NotNull final Project project) {
    final Content toolWindowContent =
        ToolWindowManager.getInstance(project)
            .getToolWindow(IvyIdeaConstants.TOOLWINDOW_ID)
            .getContentManager()
            .getSelectedContent();
    if (toolWindowContent != null) {
      final JComponent toolWindowComponent = toolWindowContent.getComponent();
      if (toolWindowComponent instanceof IvyIdeaToolWindow) {
        consoleView = ((IvyIdeaToolWindow) toolWindowComponent).getConsoleView();
      } else {
        consoleView = null;
      }
    } else {
      consoleView = null;
    }
    threshold = IvyLogLevel.fromName(
        IvyIdeaProjectState.getInstance(project).getIvyLogLevelThreshold());
  }

  @Override
  public void log(final String msg, final int level) {
    rawlog(msg, level);
  }

  @Override
  public void rawlog(final String msg, final int level) {
    if (threshold.isMoreVerboseThan(IvyLogLevel.fromLevelCode(level))) {
      ApplicationManager.getApplication()
          .invokeLater(
              () -> {
                if (consoleView != null) {
                  consoleView.print(msg + '\n', IvyLogLevel.fromLevelCode(level).getContentType());
                }
              });
    }
  }

  @Override
  protected void doProgress() {
    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              if (consoleView != null) {
                consoleView.print(".", LOG_VERBOSE_OUTPUT);
              }
            });
  }

  @Override
  protected void doEndProgress(final String msg) {

    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              if (consoleView != null) {
                consoleView.print(msg + '\n', LOG_VERBOSE_OUTPUT);
              }
            });
  }

  public enum IvyLogLevel implements Comparable<IvyLogLevel> {
    None(Integer.MIN_VALUE, SYSTEM_OUTPUT),
    Error(Message.MSG_ERR, LOG_ERROR_OUTPUT),
    Warning(Message.MSG_WARN, LOG_WARNING_OUTPUT),
    Info(Message.MSG_INFO, LOG_INFO_OUTPUT),
    Verbose(Message.MSG_VERBOSE, LOG_VERBOSE_OUTPUT),
    Debug(Message.MSG_DEBUG, LOG_DEBUG_OUTPUT),
    All(Integer.MAX_VALUE, SYSTEM_OUTPUT);

    private static final Map<Integer, IvyLogLevel> loglevels =
        Arrays.stream(IvyLogLevel.values())
            .collect(Collectors.toMap(IvyLogLevel::getLevelCode, ivyLogLevel -> ivyLogLevel));

    private final int levelCode;
    @NotNull
    private final ConsoleViewContentType contentType;

    IvyLogLevel(final int levelCode, @NotNull final ConsoleViewContentType contentType) {
      this.levelCode = levelCode;
      this.contentType = contentType;
    }

    public static IvyLogLevel fromLevelCode(final int levelCode) {
      return loglevels.getOrDefault(levelCode, None);
    }

    public static IvyLogLevel fromName(final String name) {
      try {
        return IvyLogLevel.valueOf(name);
      } catch (final IllegalArgumentException e) {
        return None;
      }
    }

    @Contract(pure = true)
    public int getLevelCode() {
      return levelCode;
    }

    @NotNull
    @Contract(pure = true)
    public ConsoleViewContentType getContentType() {
      return contentType;
    }

    @Contract(pure = true)
    public boolean isMoreVerboseThan(@NotNull final IvyLogLevel otherLevel) {
      return this.compareTo(otherLevel) >= 0;
    }
  }
}
