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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.settings.IvyIdeaProjectStateComponent;
import org.clarent.ivyidea.util.IvyIdeaConfigHelper;
import org.clarent.ivyidea.util.IvyUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Guy Mahieu
 */
class IvyManager {

  private static final Logger LOGGER =
      Logger.getInstance("#org.clarent.ivyidea.action.resolve.IvyManager");

  private final Map<Module, Ivy> configuredIvyInstances = new HashMap<>();
  private final Map<Module, ModuleDescriptor> moduleDescriptors = new HashMap<>();

  IvyManager() {
  }

  @NotNull
  private static Try<IvySettings> createConfiguredIvySettings(@NotNull final Module module) {
    final Try<String> setingsFile;
    final IvyIdeaFacetConfiguration moduleConfiguration =
        IvyIdeaFacetConfiguration.getInstance(module);
    if (moduleConfiguration == null) {
      return Try.failure(
          new RuntimeException(
              "Internal error: No IvyIDEA facet configured for module "
                  + module.getName()
                  + ", but an attempt was made to use it as such."));
    }
    if (moduleConfiguration.isUseProjectSettings()) {
      setingsFile = IvyIdeaConfigHelper.getProjectIvySettingsFile(module.getProject());
    } else {
      setingsFile = IvyIdeaConfigHelper.getModuleIvySettingsFile(module, moduleConfiguration);
    }
    final List<String> propertiesFiles =
        new ArrayList<>(moduleConfiguration.getPropertiesSettings().getPropertyFiles());
    if (moduleConfiguration.getPropertiesSettings().isIncludeProjectLevelPropertiesFiles()) {
      propertiesFiles.addAll(
          module
              .getProject()
              .getComponent(IvyIdeaProjectStateComponent.class)
              .getState()
              .getPropertiesSettings()
              .getPropertyFiles());
    }
    return IvyIdeaConfigHelper.createConfiguredIvySettings(
        module, setingsFile, IvyIdeaConfigHelper.loadProperties(module, propertiesFiles));
  }

  @NotNull
  Try<Ivy> getIvy(@NotNull final Module module) {
    if (configuredIvyInstances.containsKey(module)) {
      return Try.success(configuredIvyInstances.get(module));
    }
    return IvyUtil.createConfiguredIvyEngine(module, createConfiguredIvySettings(module))
        .onSuccess(ivy -> configuredIvyInstances.put(module, ivy));
  }

  @NotNull
  Try<ModuleDescriptor> getModuleDescriptor(@NotNull final Module module) {
    if (moduleDescriptors.containsKey(module)) {
      return Try.success(moduleDescriptors.get(module));
    }

    return IvyUtil.getIvyFile(module)
        .flatMapTry(
            file ->
                getIvy(module)
                    .flatMapTry(ivy -> IvyUtil.parseIvyFile(file, Try.success(ivy)))
                    .onSuccess(moduleDescriptor -> moduleDescriptors.put(module, moduleDescriptor))
                    .onFailure(
                        throwable ->
                            LOGGER.error("LOG00020: Could not get Ivy instance", throwable))
                    .onFailure(throwable -> moduleDescriptors.put(module, null)))
        .onFailure(throwable -> moduleDescriptors.put(module, null));
  }
}
