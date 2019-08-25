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

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_VERBOSE_OUTPUT;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import javax.swing.JComponent;
import org.apache.ivy.util.AbstractMessageLogger;
import org.clarent.ivyidea.IvyIdeaConstants;
import org.clarent.ivyidea.intellij.extension.IvyIdeaProjectComponent;
import org.clarent.ivyidea.intellij.extension.IvyIdeaToolWindowFactory.IvyIdeaToolWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConsoleViewMessageLogger extends AbstractMessageLogger {

  @Nullable
  private final ConsoleView consoleView;
  @NotNull
  private final IvyLogLevel threshold;

  public ConsoleViewMessageLogger(@NotNull final Project project) {
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
    threshold =
        IvyLogLevel.fromName(
            project
                .getComponent(IvyIdeaProjectComponent.class)
                .getState()
                .getIvyLogLevelThreshold());
  }

  @Override
  public void log(final String msg, final int level) {
    rawlog(msg, level);
  }

  @Override
  public void rawlog(final String msg, final int level) {
    if (consoleView != null && threshold.isMoreVerboseThan(IvyLogLevel.fromLevelCode(level))) {
      ApplicationManager.getApplication()
          .invokeLater(
              () ->
                  consoleView.print(msg + '\n', IvyLogLevel.fromLevelCode(level).getContentType()));
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
    if (consoleView != null) {
      ApplicationManager.getApplication()
          .invokeLater(() -> consoleView.print(msg + '\n', LOG_VERBOSE_OUTPUT));
    }
  }
}
