package org.clarent.ivyidea.intellij;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface ToolWindowRegistrationComponent {

  String COMPONENT_NAME = "IvyIDEA.ToolWindowRegistrationComponent";

  String TOOLWINDOW_ID = "IvyIDEA";

  static ToolWindowRegistrationComponent getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, ToolWindowRegistrationComponent.class);
  }

  ConsoleView getConsole();
}
