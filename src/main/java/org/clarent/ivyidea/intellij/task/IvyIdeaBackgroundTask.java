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

package org.clarent.ivyidea.intellij.task;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.clarent.ivyidea.intellij.extension.IvyIdeaProjectComponent;
import org.jetbrains.annotations.NotNull;

/** @author Guy Mahieu */
public abstract class IvyIdeaBackgroundTask extends Task.Backgroundable {

  protected IvyIdeaBackgroundTask(@NotNull final AnActionEvent event) {
    super(
        event.getData(CommonDataKeys.PROJECT),
        "IvyIDEA " + event.getPresentation().getText(),
        true,
        () -> {
          final Project project = event.getData(CommonDataKeys.PROJECT);
          return project != null && project.getComponent(IvyIdeaProjectComponent.class).getState()
              .isResolveInBackground();
        });
  }
}
