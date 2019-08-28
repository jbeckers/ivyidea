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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import io.vavr.control.Try;
import java.io.File;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.IvyContext;
import org.apache.ivy.core.event.EventManager;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.plugins.resolver.AbstractResolver;
import org.apache.ivy.plugins.resolver.BasicResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.trigger.Trigger;
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration;
import org.jetbrains.annotations.NotNull;

/** @author Guy Mahieu */
public final class IvyUtil {

  private static final Logger LOGGER = Logger.getInstance("#org.clarent.ivyidea.util.IvyUtil");

  private IvyUtil() {
  }

  /**
   * Returnes the ivy file for the given module.
   *
   * @param module the IntelliJ module for which you want to lookup the ivy file
   * @return the File representing the ivy xml file for the given module
   * @throws RuntimeException if the given module does not have an IvyIDEA facet configured.
   */
  @NotNull
  public static Try<File> getIvyFile(final Module module) {
    final IvyIdeaFacetConfiguration configuration = IvyIdeaFacetConfiguration.getInstance(module);
    if (configuration == null) {
      return Try.failure(
          new RuntimeException(
              "Internal error: No IvyIDEA facet configured for module "
                  + module.getName()
                  + ", but an attempt was made to use it as such."));
    }

    return Try.of(configuration::getIvyFile).filter(file -> !file.isEmpty()).mapTry(File::new);
  }

  /**
   * Gives a set of configurations defined in the given ivyFileName. Will never throw an exception,
   * if something goes wrong, null is returned
   *
   * @param ivyFileName the name of the ivy file to parse
   * @param ivy the Ivy engine to use, configured with the appropriate settings
   * @return a set of configurations, null if anything went wrong parsing the ivy file
   */
  @NotNull
  public static Try<Set<Configuration>> loadConfigurations(
      @NotNull final String ivyFileName, @NotNull final Try<? extends Ivy> ivy) {
    try {
      final File file = new File(ivyFileName);
      if (file.exists() && !file.isDirectory()) {
        final Try<ModuleDescriptor> md = parseIvyFile(file, ivy);
        return md.mapTry(
            moduleDescriptor -> {
              final Set<Configuration> result =
                  new TreeSet<>((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
              result.addAll(Arrays.asList(moduleDescriptor.getConfigurations()));
              return result;
            });
      } else {
        return Try.success(Collections.emptySet());
      }
    } catch (final RuntimeException e) {
      // Not able to parse module descriptor; no problem here...
      LOGGER.info(
          "LOG00180: Error while parsing ivy file during attempt to load configurations from it: "
              + e);
      if (e.getCause() instanceof ParseException) {
        return Try.failure(e.getCause());
      }
      return Try.success(Collections.emptySet());
    }
  }

  @NotNull
  public static Try<Ivy> createConfiguredIvyEngine(
      @NotNull final Module module, @NotNull final Try<? extends IvySettings> ivySettings) {
    return ivySettings
        .map(Ivy::newInstance)
        // we should now call the Ivy#postConfigure() method, but it is private :-(
        // so we have to execute the same code ourselfs
        .onSuccess(IvyUtil::postConfigure)
        .onSuccess(ivy -> pushLogger(ivy, module.getProject()));
  }

  private static void pushLogger(@NotNull final Ivy ivy, @NotNull final Project project) {
    ivy.getLoggerEngine().pushLogger(new ConsoleViewMessageLogger(project));
  }

  private static void postConfigure(@NotNull final Ivy ivy) {
    final EventManager eventManager = ivy.getEventManager();
    final Collection<Trigger> triggers = ivy.getSettings().getTriggers();
    for (final Trigger trigger : triggers) {
      eventManager.addIvyListener(trigger, trigger.getEventFilter());
    }

    for (final DependencyResolver resolver : ivy.getSettings().getResolvers()) {
      if (resolver instanceof BasicResolver) {
        ((AbstractResolver) resolver).setEventManager(eventManager);
      }
    }
  }

  /**
   * Parses the given ivyFile into a ModuleDescriptor using the given settings.
   *
   * @param ivyFile the ivy file to parse
   * @param ivy     the Ivy engine to use, configured with the appropriate settings
   * @return the ModuleDescriptor object representing the ivy file.
   */
  public static Try<ModuleDescriptor> parseIvyFile(
      @NotNull final File ivyFile, @NotNull final Try<? extends Ivy> ivy) {
    return ivy.andThen(Ivy::pushContext)
        .andThen(() -> LOGGER.info("LOG00030: Parsing ivy file " + ivyFile.getAbsolutePath()))
        .mapTry(
            ivyEngine ->
                ModuleDescriptorParserRegistry.getInstance()
                    .parseDescriptor(ivyEngine.getSettings(), ivyFile.toURI().toURL(), false))
        .andThen(IvyContext::popContext);
  }
}
