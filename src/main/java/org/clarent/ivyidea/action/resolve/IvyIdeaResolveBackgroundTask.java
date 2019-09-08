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

package org.clarent.ivyidea.action.resolve;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import io.vavr.control.Option;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JComponent;
import org.clarent.ivyidea.IvyIdeaConstants;
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.model.ModifiableRootModelWrapper;
import org.clarent.ivyidea.settings.IvyIdeaProjectState;
import org.clarent.ivyidea.toolwindow.IvyIdeaToolWindowFactory.IvyIdeaToolWindow;
import org.clarent.ivyidea.util.IvyIdeaFacetUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for background tasks that trigger an ivy resolve process.
 *
 * @author Guy Mahieu
 */
public abstract class IvyIdeaResolveBackgroundTask extends Task.Backgroundable {

  @Nullable
  private final Project project;
  @Nullable
  private ProgressMonitorThread monitorThread = null;

  protected IvyIdeaResolveBackgroundTask(@NotNull final AnActionEvent event) {
    super(
        event.getProject(),
        "IvyIDEA " + event.getPresentation().getText(),
        true,
        () -> {
          final Project eventProject = event.getProject();
          return eventProject != null
              && IvyIdeaProjectState.getInstance(eventProject).isResolveInBackground();
        });
    this.project = event.getProject();
  }

  private static void clearConsole(@Nullable final Project project) {
    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              if (project != null) {
                final ToolWindow toolWindow =
                    ToolWindowManager.getInstance(project)
                        .getToolWindow(IvyIdeaConstants.TOOLWINDOW_ID);
                if (toolWindow != null) {
                  final Content content = toolWindow.getContentManager().getSelectedContent();
                  if (content != null) {
                    final JComponent component = content.getComponent();
                    if (component instanceof IvyIdeaToolWindow) {
                      ((IvyIdeaToolWindow) component).getConsoleView().clear();
                    }
                  }
                }
              }
            });
  }

  private static void reportProblems(@NotNull final IntellijDependencyResolver resolver) {
    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              final Option<IvyIdeaFacetConfiguration> ivyIdeaFacetConfiguration =
                  IvyIdeaFacetUtil.getConfiguration(resolver.getIntellijModule());
              if (ivyIdeaFacetConfiguration.isEmpty()) {
                Notifications.Bus.notify(
                    new Notification(
                        IvyIdeaConstants.NOTIFICATION_GROUP_DISPLAY_ID,
                        "Internal Error",
                        "Internal error: module "
                            + resolver.getIntellijModule().getName()
                            + " does not seem to be have an IvyIDEA facet, but was included in the resolve process anyway.",
                        NotificationType.ERROR));
              } else {
                final String configsForModule;
                if (ivyIdeaFacetConfiguration.get().getState().isOnlyResolveSelectedConfigs()) {
                  final Set<String> configs =
                      ivyIdeaFacetConfiguration.get().getState().getConfigsToResolve();
                  if (configs.isEmpty()) {
                    configsForModule = "[No configurations selected!]";
                  } else {
                    configsForModule = configs.toString();
                  }
                } else {
                  configsForModule = "[All configurations]";
                }
                if (resolver.getProblems().isEmpty()) {
                  Notifications.Bus.notify(
                      new Notification(
                          IvyIdeaConstants.NOTIFICATION_GROUP_DISPLAY_ID,
                          "Resolve Finished Successfully",
                          "No problems occurred during resolve for module '"
                              + resolver.getIntellijModule().getName()
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
                              + resolver.getIntellijModule().getName()
                              + " "
                              + configsForModule
                              + "':"
                              + resolver.getProblems().stream()
                              .map(ResolveProblem::toString)
                              .collect(Collectors.joining("n")),
                          NotificationType.WARNING));
                }
              }
            });
  }

  private static void notifyException(final Throwable e) {
    // TODO
  }

  // TODO
  //  private static void notifyException(final IvyFileReadException e) {
  //    Notifications.Bus.notify(
  //        new Notification(
  //            IvyIdeaConstants.NOTIFICATION_GROUP_DISPLAY_ID,
  //            "Could not read Ivy file",
  //            e.getMessage(),
  //            NotificationType.ERROR));
  //  }
  //
  // TODO
  //  private static void notifyException(final IvySettingsFileReadException e) {
  //    Notifications.Bus.notify(
  //        new Notification(
  //            IvyIdeaConstants.NOTIFICATION_GROUP_DISPLAY_ID,
  //            "Could not read Ivy settings file",
  //            e.getMessage(),
  //            NotificationType.ERROR));
  //  }
  //
  // TODO
  //  private static void notifyException(final IvySettingsNotFoundException e, final Project
  // project) {
  //    final Notification notification =
  //        new Notification(
  //            IvyIdeaConstants.NOTIFICATION_GROUP_DISPLAY_ID,
  //            "Could not find Ivy settings",
  //            e.getMessage(),
  //            NotificationType.ERROR);
  //    notification.addAction(
  //        NotificationAction.createSimple(
  //            "Open Ivy settings",
  //            () ->
  //                ShowSettingsUtil.getInstance()
  //                    .showSettingsDialog(project, IvyIdeaProjectConfigurable.class)));
  //    Notifications.Bus.notify(notification);
  //  }

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
          .map(
              module ->
                  ivyManager
                      .getIvy(module)
                      .onSuccess(
                          ivy -> {
                            if (monitorThread != null) {
                              monitorThread.setIvy(ivy);
                            }
                          })
                      .andThen(() -> indicator.setText2("Resolving for module " + module.getName()))
                      .mapTry(ivy -> new IntellijDependencyResolver(ivyManager, module))
                      .andThenTry(IntellijDependencyResolver::resolve)
                      .onFailure(IvyIdeaResolveBackgroundTask::notifyException))
          .forEach(
              resolver ->
                  resolver.onSuccess(
                      dependencyResolver -> {
                        if (!indicator.isCanceled()) {
                          ApplicationManager.getApplication()
                              .invokeLater(
                                  () ->
                                      WriteAction.run(
                                          () -> {
                                            try (final ModifiableRootModelWrapper wrapper =
                                                ModifiableRootModelWrapper.forModule(
                                                    dependencyResolver.getIntellijModule())) {
                                              wrapper.updateDependencies(
                                                  dependencyResolver.getDependencies());
                                            }
                                          }));
                          reportProblems(dependencyResolver);
                        }
                      }));
    } catch (final RuntimeException e) {
      if (!indicator.isCanceled()) {
        throw e;
      }
    }
  }
}
