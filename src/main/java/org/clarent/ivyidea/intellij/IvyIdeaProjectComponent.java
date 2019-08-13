package org.clarent.ivyidea.intellij;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.clarent.ivyidea.config.model.IvyIdeaProjectSettings;
import org.jetbrains.annotations.NotNull;

public interface IvyIdeaProjectComponent {

  static IvyIdeaProjectComponent getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, IvyIdeaProjectComponent.class);
  }

  IvyIdeaProjectSettings getState();
}
