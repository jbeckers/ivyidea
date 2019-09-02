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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.util.net.HttpConfigurable;
import io.vavr.control.Try;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.ivy.core.settings.IvySettings;
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.settings.IvyIdeaProjectStateComponent;
import org.clarent.ivyidea.settings.IvyIdeaProjectStateComponent.IvyIdeaProjectState;
import org.clarent.ivyidea.util.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.util.exception.IvySettingsNotFoundException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Handles retrieval of settings from the configuration.
 *
 * @author Guy Mahieu
 */
public final class IvyIdeaConfigUtil {

  @Contract(pure = true)
  private IvyIdeaConfigUtil() {
  }

  @NotNull
  public static Try<String> getModuleIvySettingsFile(
      @NotNull final Module module, @NotNull final IvyIdeaFacetConfiguration moduleConfiguration) {
    if (moduleConfiguration.getState().useCustomIvySettings) {
      final String ivySettingsFile = moduleConfiguration.getState().ivySettingsFile.trim();
      if (ivySettingsFile.isEmpty()) {
        return Try.failure(
            new IvySettingsNotFoundException(
                "No ivy settings file given in the settings of module " + module.getName(),
                IvySettingsNotFoundException.ConfigLocation.Module,
                module.getName()));
      } else {
        if (!ivySettingsFile.startsWith("http://")
            && !ivySettingsFile.startsWith("https://")
            && !ivySettingsFile.startsWith("file://")) {
          final File result = new File(ivySettingsFile);
          if (!result.exists()) {
            return Try.failure(
                new IvySettingsNotFoundException(
                    "The ivy settings file given in the module settings for module "
                        + module.getName()
                        + " does not exist: "
                        + result.getAbsolutePath(),
                    IvySettingsNotFoundException.ConfigLocation.Module,
                    module.getName()));
          }
          if (result.isDirectory()) {
            return Try.failure(
                new IvySettingsNotFoundException(
                    "The ivy settings file given in the module settings for module "
                        + module.getName()
                        + " is a directory: "
                        + result.getAbsolutePath(),
                    IvySettingsNotFoundException.ConfigLocation.Module,
                    module.getName()));
          }
        }
        return Try.success(ivySettingsFile);
      }
    } else {
      return Try.success(null); // use ivy standard
    }
  }

  @NotNull
  public static Try<String> getProjectIvySettingsFile(@NotNull final Project project) {
    final IvyIdeaProjectState state =
        ServiceManager.getService(project, IvyIdeaProjectStateComponent.class).getState();
    if (state.useCustomIvySettings) {
      final String settingsFile = state.ivySettingsFile.trim();
      if (settingsFile.isEmpty()) {
        return Try.failure(
            new IvySettingsNotFoundException(
                "No ivy settings file specified in the project settings.",
                IvySettingsNotFoundException.ConfigLocation.Project,
                project.getName()));
      } else {
        if (!settingsFile.startsWith("http://")
            && !settingsFile.startsWith("https://")
            && !settingsFile.startsWith("file://")) {
          final File result = new File(settingsFile);
          if (!result.exists()) {
            return Try.failure(
                new IvySettingsNotFoundException(
                    "The ivy settings file given in the project settings does not exist: "
                        + result.getAbsolutePath(),
                    IvySettingsNotFoundException.ConfigLocation.Project,
                    project.getName()));
          }
          if (result.isDirectory()) {
            return Try.failure(
                new IvySettingsNotFoundException(
                    "The ivy settings file given in the project settings is a directory: "
                        + result.getAbsolutePath(),
                    IvySettingsNotFoundException.ConfigLocation.Project,
                    project.getName()));
          }
        }
        return Try.success(settingsFile);
      }
    } else {
      return Try.success(null); // use ivy standard
    }
  }

  @NotNull
  public static Try<Properties> loadProperties(
      @NotNull final Module module, @NotNull final List<String> propertiesFiles) {
    // Go over the files in reverse order --> files listed first should have priority and loading
    // properties
    // overwrited previously loaded ones.
    final Properties properties = new Properties();
    final List<String> result1 = new ArrayList<>(propertiesFiles); // avoid changing the input
    Collections.reverse(result1);
    for (final String propertiesFile : result1) {
      if (propertiesFile != null) {
        final File result = new File(propertiesFile);
        if (!result.exists()) {
          return Try.failure(
              new IvySettingsNotFoundException(
                  "The ivy properties file given in the module settings for module "
                      + module.getName()
                      + " does not exist: "
                      + result.getAbsolutePath(),
                  IvySettingsNotFoundException.ConfigLocation.Module,
                  module.getName()));
        }
        try {
          properties.load(new FileInputStream(result));
        } catch (final IOException e) {
          return Try.failure(
              new IvySettingsFileReadException(result.getAbsolutePath(), module.getName(), e));
        }
      }
    }
    return Try.success(properties);
  }

  @NotNull
  public static Try<IvySettings> createConfiguredIvySettings(
      final Module module,
      @NotNull final Try<String> settingsFile,
      @NotNull final Try<? extends Properties> properties) {
    final IvySettings ivySettings = new IvySettings();
    // inject our properties; they may be needed to parse the settings file
    // By default, we use the module root as basedir (can be overridden by properties injected
    // below)
    final File moduleFileFolder = new File(module.getModuleFilePath()).getParentFile();
    if (moduleFileFolder != null) {
      ivySettings.setBaseDir(moduleFileFolder.getAbsoluteFile());
    }
    properties.onSuccess(
        props -> {
          @SuppressWarnings("unchecked") final Enumeration<String> propertyNames = (Enumeration<String>) props
              .propertyNames();
          while (propertyNames.hasMoreElements()) {
            final String propertyName = propertyNames.nextElement();
            ivySettings.setVariable(propertyName, props.getProperty(propertyName));
          }
        });

    if (settingsFile.isSuccess()) {
      try {
        if (settingsFile.get() == null || settingsFile.get().trim().isEmpty()) {
          ivySettings.loadDefault();
        } else {
          if (settingsFile.get().startsWith("http://")
              || settingsFile.get().startsWith("https://")) {
            HttpConfigurable.getInstance().prepareURL(settingsFile.get());
            ivySettings.load(new URL(settingsFile.get()));
          } else if (settingsFile.get().startsWith("file://")) {
            ivySettings.load(new URL(settingsFile.get()));
          } else {
            ivySettings.load(new File(settingsFile.get()));
          }
        }
      } catch (final ParseException | IOException e) {
        return Try.failure(
            new IvySettingsFileReadException(settingsFile.get(), module.getName(), e));
      }
    }

    // re-inject our properties; they may overwrite some properties loaded by the settings file
    properties.onSuccess(
        props -> {
          for (final Map.Entry<Object, Object> entry : props.entrySet()) {
            final String key = (String) entry.getKey();
            final String value = (String) entry.getValue();

            // we first clear the property to avoid possible cyclic-variable errors (cfr issue 95)
            ivySettings.setVariable(key, null);
            ivySettings.setVariable(key, value);
          }
        });

    return Try.success(ivySettings);
  }
}
