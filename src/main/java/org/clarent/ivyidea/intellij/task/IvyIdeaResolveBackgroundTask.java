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

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JComponent;
import org.clarent.ivyidea.IvyIdeaConstants;
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.extension.IvyIdeaToolWindowFactory.IvyIdeaToolWindow;
import org.clarent.ivyidea.intellij.extension.facet.IvyIdeaFacetType;
import org.clarent.ivyidea.intellij.extension.settings.IvyIdeaProjectSettingsComponent;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.intellij.model.IntellijModuleWrapper;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.IntellijDependencyResolver;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;
import org.clarent.ivyidea.resolve.problem.ResolveProblem;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for background tasks that trigger an ivy resolve process.
 *
 * @author Guy Mahieu
 */
public abstract class IvyIdeaResolveBackgroundTask extends IvyIdeaBackgroundTask {

  private final Project project;
  private ProgressMonitorThread monitorThread = null;

  protected IvyIdeaResolveBackgroundTask(final AnActionEvent event) {
    super(event);
    this.project = event.getProject();
  }

  private static void clearConsole(final Project project) {
    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              final ToolWindow toolWindow =
                  ToolWindowManager.getInstance(project)
                      .getToolWindow(IvyIdeaConstants.TOOLWINDOW_ID);
              if (toolWindow != null) {
                final Content content = toolWindow.getContentManager().getSelectedContent();
                if (content != null) {
                  JComponent component = content.getComponent();
                  if (component instanceof IvyIdeaToolWindow) {
                    ((IvyIdeaToolWindow) component).getConsoleView().clear();
                  }
                }
              }
            });
  }

  private static void updateIntellijModel(
      final Module module, final Collection<ResolvedDependency> dependencies) {
    ApplicationManager.getApplication()
        .invokeLater(
            () ->
                WriteAction.run(
                    () -> {
                      try (final IntellijModuleWrapper wrapper =
                          IntellijModuleWrapper.forModule(module)) {
                        wrapper.updateDependencies(dependencies);
                      }
                    }));
  }

  private static void reportProblems(
      final Module module, final Collection<? extends ResolveProblem> problems) {
    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              final IvyIdeaFacetConfiguration ivyIdeaFacetConfiguration =
                  IvyIdeaFacetConfiguration.getInstance(module);
              if (ivyIdeaFacetConfiguration == null) {
                Notifications.Bus.notify(
                    new Notification(
                        IvyIdeaConstants.NOTIFICATION_GROUP_DISPLAY_ID,
                        "Internal Error",
                        "Internal error: module "
                            + module.getName()
                            + " does not seem to be have an IvyIDEA facet, but was included in the resolve process anyway.",
                        NotificationType.ERROR));
              } else {
                final String configsForModule;
                if (ivyIdeaFacetConfiguration.isOnlyResolveSelectedConfigs()) {
                  final Set<String> configs = ivyIdeaFacetConfiguration.getConfigsToResolve();
                  if (configs == null || configs.isEmpty()) {
                    configsForModule = "[No configurations selected!]";
                  } else {
                    configsForModule = configs.toString();
                  }
                } else {
                  configsForModule = "[All configurations]";
                }
                if (problems.isEmpty()) {
                  Notifications.Bus.notify(
                      new Notification(
                          IvyIdeaConstants.NOTIFICATION_GROUP_DISPLAY_ID,
                          "Resolve Finished Successfully",
                          "No problems occurred during resolve for module '"
                              + module.getName()
                              + "' "
                              + configsForModule
                              + ".",
                          NotificationType.INFORMATION));
                } else {
                  Notifications.Bus.notify(
                      new Notification(
                          IvyIdeaConstants.NOTIFICATION_GROUP_DISPLAY_ID,
                          "Resolve Failed",
                          "Problems occurred for module '"
                              + module.getName()
                              + " "
                              + configsForModule
                              + "':"
                              + problems.stream()
                              .map(ResolveProblem::toString)
                              .collect(Collectors.joining("n")),
                          NotificationType.WARNING));
                }
              }
            });
  }

  private static void notifyIvyFileRead(final IvyFileReadException e) {
    Notifications.Bus.notify(
        new Notification(
            IvyIdeaConstants.NOTIFICATION_GROUP_DISPLAY_ID, "Could not read Ivy file",
            e.getMessage(), NotificationType.ERROR));
  }

  private static void notifyIvySettingsFileRead(final IvySettingsFileReadException e) {
    Notifications.Bus.notify(
        new Notification(
            IvyIdeaConstants.NOTIFICATION_GROUP_DISPLAY_ID, "Could not read Ivy settings file",
            e.getMessage(), NotificationType.ERROR));
  }

  private static void notifyIvySettingsNotFound(
      final Project project, final IvySettingsNotFoundException e) {
    final Notification notification =
        new Notification(
            IvyIdeaConstants.NOTIFICATION_GROUP_DISPLAY_ID, "Could not find Ivy settings",
            e.getMessage(), NotificationType.ERROR);
    notification.addAction(
        NotificationAction.createSimple(
            "Open Ivy settings",
            () ->
                ShowSettingsUtil.getInstance()
                    .showSettingsDialog(project, IvyIdeaProjectSettingsComponent.class)));
    Notifications.Bus.notify(notification);
  }

  protected abstract Stream<Module> getModules();

  @Override
  public final void run(@NotNull final ProgressIndicator indicator) {
    monitorThread = new ProgressMonitorThread(indicator, Thread.currentThread());
    monitorThread.start();

    try {
      // Intercept URL requests and force the intellij proxy to be used
      //
      // TODO: This does not seem to work...
      /*
          IntellijProxyURLHandler.setupHttpProxy();
      */
      // Start the actual resolve process
      clearConsole(project);

      final IvyManager ivyManager = new IvyManager();

      getModules()
          .filter(IvyIdeaFacetType::isIvyModule)
          .map(
              module -> {
                try {
                  monitorThread.setIvy(ivyManager.getIvy(module));
                } catch (final IvySettingsNotFoundException e) {
                  notifyIvySettingsNotFound(project, e);
                } catch (final IvySettingsFileReadException e) {
                  notifyIvySettingsFileRead(e);
                }
                indicator.setText2("Resolving for module " + module.getName());
                final IntellijDependencyResolver resolver =
                    new IntellijDependencyResolver(ivyManager);
                try {
                  resolver.resolve(module);
                } catch (final IvySettingsNotFoundException e) {
                  notifyIvySettingsNotFound(project, e);
                } catch (final IvySettingsFileReadException e) {
                  notifyIvySettingsFileRead(e);
                } catch (final IvyFileReadException e) {
                  notifyIvyFileRead(e);
                }
                return resolver;
              })
          .forEach(
              resolver -> {
                if (!indicator.isCanceled()) {
                  updateIntellijModel(resolver.getModule(), resolver.getDependencies());
                  reportProblems(resolver.getModule(), resolver.getProblems());
                }
              });
    } catch (final RuntimeException e) {
      if (!indicator.isCanceled()) {
        throw e;
      }
    }
  }
}
