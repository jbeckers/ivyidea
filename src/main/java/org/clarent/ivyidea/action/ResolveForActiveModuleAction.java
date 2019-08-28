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
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import java.text.MessageFormat;
import java.util.stream.Stream;
import org.clarent.ivyidea.action.resolve.IvyIdeaResolveBackgroundTask;
import org.clarent.ivyidea.facet.IvyIdeaFacetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Action to resolve the dependencies for the active module.
 *
 * @author Guy Mahieu
 */
public class ResolveForActiveModuleAction extends AnAction {

  public ResolveForActiveModuleAction() {
    super("Resolve for Active Module", "Resolve dependencies for the active module", null);
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    FileDocumentManager.getInstance().saveAllDocuments();

    ProgressManager.getInstance().run(new BackgroundTask(e));
  }

  @Override
  public void update(final AnActionEvent e) {
    final Module module = e.getData(LangDataKeys.MODULE);

    if (module == null) {
      e.getPresentation().setEnabled(false);
      e.getPresentation().setVisible(false);
      e.getPresentation().setDescription(null);
    } else {

      e.getPresentation().setEnabled(IvyIdeaFacetType.isIvyModule(module));
      e.getPresentation().setVisible(IvyIdeaFacetType.isIvyModule(module));
      e.getPresentation()
          .setText(
              MessageFormat.format(
                  "Resolve for {0} module",
                  IvyIdeaFacetType.isIvyModule(module) ? module.getName() : "Active"));
    }
  }

  private static class BackgroundTask extends IvyIdeaResolveBackgroundTask {

    @Nullable
    private final Module module;

    BackgroundTask(@NotNull final AnActionEvent e) {
      super(e);
      this.module = e.getData(LangDataKeys.MODULE);
    }

    @Override
    protected Stream<Module> getModules() {
      return IvyIdeaFacetType.isIvyModule(module) ? Stream.of(module) : Stream.empty();
    }
  }
}
