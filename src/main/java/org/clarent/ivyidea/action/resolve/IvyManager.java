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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.settings.IvyIdeaProjectState;
import org.clarent.ivyidea.util.IvyIdeaConfigUtil;
import org.clarent.ivyidea.util.IvyIdeaFacetUtil;
import org.clarent.ivyidea.util.IvyUtil;
import org.clarent.ivyidea.util.ModuleDescriptorUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Guy Mahieu
 */
class IvyManager {

  @NotNull
  private static final Logger LOGGER =
      Logger.getInstance("#org.clarent.ivyidea.action.resolve.IvyManager");

  @NotNull
  private final Map<Module, Ivy> ivyEngines = new HashMap<>();
  @NotNull
  private final Map<Module, ModuleDescriptor> moduleDescriptors = new HashMap<>();

  IvyManager() {}

  @NotNull
  private static List<String> getPropertiesFiles(
      @NotNull final IvyIdeaFacetConfiguration moduleFacetConfiguration,
      @NotNull final IvyIdeaProjectState projectState) {
    final List<String> propertiesFiles =
        new ArrayList<>(moduleFacetConfiguration.getState().getPropertiesFiles());
    if (moduleFacetConfiguration.getState().isIncludeProjectLevelPropertiesFiles()) {
      propertiesFiles.addAll(projectState.getPropertyFiles());
    }
    return propertiesFiles;
  }

  @NotNull
  private static Try<String> getSettingsFile(
      @NotNull final Module module,
      @NotNull final IvyIdeaFacetConfiguration moduleFacetConfiguration) {
    return moduleFacetConfiguration.getState().isUseProjectSettings()
        ? IvyIdeaConfigUtil.getProjectIvySettingsFile(module.getProject())
        : IvyIdeaConfigUtil.getModuleIvySettingsFile(module, moduleFacetConfiguration);
  }

  @NotNull
  Try<Ivy> getIvy(@NotNull final Module module) {
    if (ivyEngines.containsKey(module)) {
      return Try.success(ivyEngines.get(module));
    }
    return IvyUtil.newInstance(
        module,
        IvyIdeaFacetUtil.getConfiguration(module)
            .toTry(
                () ->
                    new RuntimeException(
                        "Internal error: No IvyIDEA facet configured for module "
                            + module.getName()
                            + ", but an attempt was made to use it as such."))
            .flatMapTry(
                moduleFacetConfiguration ->
                    IvyIdeaConfigUtil.createConfiguredIvySettings(
                        module,
                        getSettingsFile(module, moduleFacetConfiguration),
                        IvyIdeaConfigUtil.loadProperties(
                            module,
                            getPropertiesFiles(
                                moduleFacetConfiguration,
                                ServiceManager.getService(
                                    module.getProject(), IvyIdeaProjectState.class)
                                    .getState())))))
        .onSuccess(ivy -> ivyEngines.put(module, ivy));
  }

  @NotNull
  Try<ModuleDescriptor> getModuleDescriptor(@NotNull final Module module) {
    if (moduleDescriptors.containsKey(module)) {
      return Try.success(moduleDescriptors.get(module));
    }

    return IvyIdeaFacetUtil.getIvyFile(module)
        .flatMapTry(
            file ->
                getIvy(module)
                    .flatMapTry(ivy -> ModuleDescriptorUtil.parseDescriptor(file, Try.success(ivy)))
                    .onSuccess(moduleDescriptor -> moduleDescriptors.put(module, moduleDescriptor))
                    .onFailure(
                        throwable ->
                            LOGGER.error("LOG00020: Could not get Ivy instance", throwable))
                    .onFailure(throwable -> moduleDescriptors.put(module, null)))
        .onFailure(throwable -> moduleDescriptors.put(module, null));
  }
}
