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

package org.clarent.ivyidea.intellij.extension;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import org.clarent.ivyidea.intellij.ui.IvyIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public class ToolWindowRegistrationComponent {

  private static final String COMPONENT_NAME = "IvyIDEA.ToolWindowRegistrationComponent";
  public static final String TOOLWINDOW_ID = "IvyIDEA";
  private final Project project;
  @Nullable private ConsoleView console;

  public ToolWindowRegistrationComponent(final Project project) {
    this.project = project;
  }

  public static ToolWindowRegistrationComponent getInstance(@NotNull final Project project) {
    return ServiceManager.getService(project, ToolWindowRegistrationComponent.class);
  }

  public void initComponent() {}

  public void disposeComponent() {}

  @NotNull
  public static String getComponentName() {
    return COMPONENT_NAME;
  }

  public void projectOpened() {
    registerToolWindow();
  }

  public void projectClosed() {
    unregisterToolWindow();
  }

  @Nullable
  public ConsoleView getConsole() {
    return console;
  }

  private void unregisterToolWindow() {
    if (console != null) {
      console.dispose();
    }
    console = null;

    ToolWindowManager.getInstance(project).unregisterToolWindow(TOOLWINDOW_ID);
  }

  private void registerToolWindow() {
    final ToolWindow toolWindow =
        ToolWindowManager.getInstance(project)
            .registerToolWindow(TOOLWINDOW_ID, false, ToolWindowAnchor.BOTTOM);
    toolWindow.setIcon(IvyIdeaIcons.MAIN_ICON_SMALL);
    console = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
    toolWindow
        .getContentManager()
        .addContent(
            ServiceManager.getService(ContentFactory.class)
                .createContent(console.getComponent(), "Console", true));
  }
}
