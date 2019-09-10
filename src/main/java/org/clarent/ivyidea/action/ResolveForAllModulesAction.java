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

package org.clarent.ivyidea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import org.clarent.ivyidea.action.resolve.IvyIdeaResolveBackgroundTask;
import org.clarent.ivyidea.util.IvyIdeaFacetUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Action to resolve the dependencies for all modules that have an IvyIDEA facet configured.
 *
 * @author Guy Mahieu
 */
public class ResolveForAllModulesAction extends AnAction {

  public ResolveForAllModulesAction() {
    super("Resolve for All Modules", "Resolve dependencies for all modules", null);
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    FileDocumentManager.getInstance().saveAllDocuments();

    ProgressManager.getInstance().run(new BackgroundTask(e));
  }

  private static class BackgroundTask extends IvyIdeaResolveBackgroundTask {

    @NotNull
    static final Module[] ZERO_LENGTH_MODULES = new Module[0];

    @Nullable
    private final Project project;

    BackgroundTask(@NotNull final AnActionEvent e) {
      super(e);
      project = e.getProject();
    }

    @Override
    @NotNull
    protected Stream<Module> getModules() {
      return Arrays.stream(
          project == null
              ? ZERO_LENGTH_MODULES
              : ModuleManager.getInstance(project).getModules())
          .filter(Objects::nonNull)
          .filter(IvyIdeaFacetUtil::isIvyModule);
    }
  }
}
