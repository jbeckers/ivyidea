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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.libraries.Library;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import org.clarent.ivyidea.IvyIdeaConstants;
import org.clarent.ivyidea.facet.IvyIdeaFacetType;
import org.clarent.ivyidea.settings.IvyIdeaProjectStateComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Action to remove all module libraries that match the name of the IvyIDEA-resolved module.
 *
 * @author Guy Mahieu
 */
public class RemoveAllIvyIdeaModuleLibrariesAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    ProgressManager.getInstance().run(new BackgroundTask(e));
  }

  private static class BackgroundTask extends Task.Backgroundable {

    @Nullable
    private final Project project;

    BackgroundTask(final AnActionEvent event) {
      super(
          event.getProject(),
          "IvyIDEA " + event.getPresentation().getText(),
          true,
          () -> {
            final Project eventProject = event.getProject();
            return eventProject != null
                && eventProject
                .getComponent(IvyIdeaProjectStateComponent.class)
                .getState()
                .isResolveInBackground();
          });
      this.project = event.getProject();
    }

    @NotNull
    private static Stream<Module> getModules(@Nullable final Project project) {
      return project == null
          ? Stream.empty()
          : Arrays.stream(ModuleManager.getInstance(project).getModules()).filter(Objects::nonNull);
    }

    @NotNull
    private static Stream<Library> getLibraries(final ModifiableRootModel model) {
      return Arrays.stream(model.getModuleLibraryTable().getLibraries()).filter(Objects::nonNull);
    }

    private static boolean isIvyResolvedLibrary(@NotNull final Library library) {
      final String libraryName = library.getName();
      return libraryName != null && libraryName.startsWith(IvyIdeaConstants.RESOLVED_LIB_NAME_ROOT);
    }

    @Override
    public void run(@NotNull final ProgressIndicator indicator) {
      indicator.setIndeterminate(false);

      getModules(project)
          .filter(IvyIdeaFacetType::isIvyModule)
          .forEach(
              module -> {
                indicator.setText2("Removing for module " + module.getName());
                ModuleRootModificationUtil.updateModel(
                    module,
                    model ->
                        getLibraries(model)
                            .filter(BackgroundTask::isIvyResolvedLibrary)
                            .forEach(
                                library -> model.getModuleLibraryTable().removeLibrary(library)));
              });
    }
  }
}
