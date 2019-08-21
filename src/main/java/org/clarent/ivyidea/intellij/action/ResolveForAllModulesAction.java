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

package org.clarent.ivyidea.intellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import java.util.ArrayList;
import java.util.Collection;
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.task.IvyIdeaResolveBackgroundTask;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.IntellijDependencyResolver;
import org.jetbrains.annotations.NotNull;

/**
 * Action to resolve the dependencies for all modules that have an IvyIDEA facet configured.
 *
 * @author Guy Mahieu
 */
public class ResolveForAllModulesAction extends AbstractResolveAction {

  @Override
  public void actionPerformed(final AnActionEvent e) {
    FileDocumentManager.getInstance().saveAllDocuments();

    final Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
    ProgressManager.getInstance()
        .run(
            new IvyIdeaResolveBackgroundTask(project, e) {
              @Override
              public void doResolve(@NotNull final ProgressIndicator indicator)
                  throws IvySettingsNotFoundException, IvyFileReadException,
                      IvySettingsFileReadException {
                clearConsole(myProject);

                final IvyManager ivyManager = new IvyManager();

                final Collection<IntellijDependencyResolver> resolvers =
                    new ArrayList<>();
                for (final Module module : IntellijUtils.getAllModulesWithIvyIdeaFacet(project)) {
                  getProgressMonitorThread().setIvy(ivyManager.getIvy(module));
                  indicator.setText2("Resolving for module " + module.getName());
                  final IntellijDependencyResolver resolver =
                      new IntellijDependencyResolver(ivyManager);
                  resolver.resolve(module);
                  resolvers.add(resolver);

                  if (indicator.isCanceled()) {
                    return;
                  }
                }

                for (final IntellijDependencyResolver resolver : resolvers) {
                  final Module module = resolver.getModule();
                  updateIntellijModel(module, resolver.getDependencies());
                  reportProblems(module, resolver.getProblems());
                }
              }
            });
  }
}
