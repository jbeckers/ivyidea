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

package org.clarent.ivyidea.model.dependency;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import org.clarent.ivyidea.model.ModifiableRootModelWrapper;

/** @author Guy Mahieu */
public class InternalDependency implements ResolvedDependency {

  private static final Logger LOGGER =
      Logger.getInstance("#org.clarent.ivyidea.model.dependency.InternalDependency");

  private final Module module;

  public InternalDependency(final Module module) {
    this.module = module;
  }

  @Override
  public void addTo(final ModifiableRootModelWrapper modifiableRootModelWrapper) {
    if (!modifiableRootModelWrapper.alreadyHasDependencyOnModule(module)) {
      LOGGER.info(
          "LOG00150: Registering module dependency from "
              + modifiableRootModelWrapper.getModuleName()
              + " on module "
              + module.getName());
      modifiableRootModelWrapper.addModuleDependency(module);
    } else {
      LOGGER.info(
          "LOG00230: Dependency from "
              + modifiableRootModelWrapper.getModuleName()
              + " on module "
              + module.getName()
              + " was already present; not reregistring");
    }
  }
}
