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

package org.clarent.ivyidea.ivy;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.event.EventManager;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.plugins.resolver.AbstractResolver;
import org.apache.ivy.plugins.resolver.BasicResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.trigger.Trigger;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.logging.ConsoleViewMessageLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public final class IvyUtil {

  private static final Logger LOGGER = Logger.getLogger(IvyUtil.class.getName());

  private IvyUtil() {
  }

  /**
   * Returnes the ivy file for the given module.
   *
   * @param module the IntelliJ module for which you want to lookup the ivy file
   * @return the File representing the ivy xml file for the given module
   * @throws RuntimeException if the given module does not have an IvyIDEA facet configured.
   */
  @Nullable
  public static File getIvyFile(final Module module) {
    final IvyIdeaFacetConfiguration configuration = IvyIdeaFacetConfiguration.getInstance(module);
    if (configuration == null) {
      throw new RuntimeException(
          "Internal error: No IvyIDEA facet configured for module "
              + module.getName()
              + ", but an attempt was made to use it as such.");
    }

    final String ivyFile = configuration.getIvyFile();
    return ivyFile.isEmpty() ? null : new File(ivyFile);

  }

  /**
   * Parses the given ivyFile into a ModuleDescriptor using the given settings.
   *
   * @param ivyFile the ivy file to parse
   * @param ivy the Ivy engine to use, configured with the appropriate settings
   * @return the ModuleDescriptor object representing the ivy file.
   */
  public static ModuleDescriptor parseIvyFile(@NotNull final File ivyFile, @NotNull final Ivy ivy) {
    LOGGER.info("Parsing ivy file " + ivyFile.getAbsolutePath());

    ModuleDescriptor moduleDescriptor;
    try {
      ivy.pushContext();
      moduleDescriptor =
          ModuleDescriptorParserRegistry.getInstance()
              .parseDescriptor(ivy.getSettings(), ivyFile.toURI().toURL(), false);
    } catch (final ParseException | IOException e) {
      throw new RuntimeException(e);
    } finally {
      ivy.popContext();
    }

    return moduleDescriptor;
  }

  /**
   * Gives a set of configurations defined in the given ivyFileName. Will never throw an exception,
   * if something goes wrong, null is returned
   *
   * @param ivyFileName the name of the ivy file to parse
   * @param ivy the Ivy engine to use, configured with the appropriate settings
   * @return a set of configurations, null if anything went wrong parsing the ivy file
   * @throws java.text.ParseException if there was an error parsing the ivy file; if the file does
   *     not exist or is a directory, no exception will be thrown
   */
  @Nullable
  public static Set<Configuration> loadConfigurations(@NotNull final String ivyFileName, @NotNull final Ivy ivy)
      throws ParseException {
    try {
      final File file = new File(ivyFileName);
      if (file.exists() && !file.isDirectory()) {
        final ModuleDescriptor md = parseIvyFile(file, ivy);
        final Set<Configuration> result =
            new TreeSet<>(
                (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        result.addAll(Arrays.asList(md.getConfigurations()));
        return result;
      } else {
        return null;
      }
    } catch (final RuntimeException e) {
      // Not able to parse module descriptor; no problem here...
      LOGGER.info(
          "Error while parsing ivy file during attempt to load configurations from it: " + e);
      if (e.getCause() instanceof ParseException) {
        throw (ParseException) e.getCause();
      }
      return null;
    }
  }

  public static Ivy createConfiguredIvyEngine(final Module module, final IvySettings ivySettings) {
    final Ivy ivy = Ivy.newInstance(ivySettings);

    // we should now call the Ivy#postConfigure() method, but it is private :-(
    // so we have to execute the same code ourselfs
    postConfigure(ivy);

    registerConsoleLogger(ivy, module.getProject());
    return ivy;
  }

  private static void postConfigure(final Ivy ivy) {
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

  private static void registerConsoleLogger(final Ivy ivy, final Project project) {
    ivy.getLoggerEngine()
        .pushLogger(new ConsoleViewMessageLogger(project, IntellijUtils.getConsoleView(project)));
  }
}
