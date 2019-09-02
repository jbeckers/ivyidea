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

package org.clarent.ivyidea.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import io.vavr.control.Try;
import java.util.Collection;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.AbstractResolver;
import org.apache.ivy.plugins.resolver.BasicResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.trigger.Trigger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/** @author Guy Mahieu */
public final class IvyUtil {

  @Contract(pure = true)
  private IvyUtil() {
  }

  @NotNull
  public static Try<Ivy> newInstance(
      @NotNull final Module module, @NotNull final Try<? extends IvySettings> ivySettings) {
    return ivySettings
        .map(Ivy::newInstance)
        // we should now call the Ivy#postConfigure() method, but it is private :-(
        // so we have to execute the same code ourselves
        .onSuccess(IvyUtil::postConfigure)
        .onSuccess(ivy -> pushLogger(ivy, module.getProject()));
  }

  private static void pushLogger(@NotNull final Ivy ivy, @NotNull final Project project) {
    ivy.getLoggerEngine().pushLogger(new ConsoleViewMessageLogger(project));
  }

  private static void postConfigure(@NotNull final Ivy ivy) {
    final Collection<Trigger> triggers = ivy.getSettings().getTriggers();
    for (final Trigger trigger : triggers) {
      ivy.getEventManager().addIvyListener(trigger, trigger.getEventFilter());
    }

    for (final DependencyResolver resolver : ivy.getSettings().getResolvers()) {
      if (resolver instanceof BasicResolver) {
        ((AbstractResolver) resolver).setEventManager(ivy.getEventManager());
      }
    }
  }
}
