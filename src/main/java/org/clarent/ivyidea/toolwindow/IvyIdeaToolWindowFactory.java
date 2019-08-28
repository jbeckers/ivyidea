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

package org.clarent.ivyidea.toolwindow;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

/**
 * @author Guy Mahieu
 */
public class IvyIdeaToolWindowFactory implements ToolWindowFactory {

  @Override
  public void createToolWindowContent(
      @NotNull final Project project, @NotNull final ToolWindow toolWindow) {
    final ConsoleView consoleView = ServiceManager.getService(TextConsoleBuilderFactory.class)
        .createBuilder(project)
        .getConsole();
    final Content content =
        ServiceManager.getService(ContentFactory.class)
            .createContent(new IvyIdeaToolWindow(consoleView), "Console", true);
    Disposer.register(content, consoleView);
    toolWindow.getContentManager().addContent(content);
  }

  public static final class IvyIdeaToolWindow extends JPanel {

    private static final long serialVersionUID = -3462108543241835858L;

    @NotNull
    private final ConsoleView consoleView;

    private IvyIdeaToolWindow(@NotNull final ConsoleView view) {
      this.consoleView = view;
      this.setLayout(new BorderLayout());
      this.add(view.getComponent());
    }

    @NotNull
    public ConsoleView getConsoleView() {
      return consoleView;
    }
  }
}
