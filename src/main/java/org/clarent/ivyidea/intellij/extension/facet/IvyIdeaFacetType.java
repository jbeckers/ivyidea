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

package org.clarent.ivyidea.intellij.extension.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Collection;
import org.clarent.ivyidea.intellij.facet.IvyIdeaFacet;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.clarent.ivyidea.intellij.ui.IvyIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public class IvyIdeaFacetType extends FacetType<IvyIdeaFacet, IvyIdeaFacetConfiguration> {

  public static final FacetTypeId<IvyIdeaFacet> ID = new FacetTypeId<>("IvyIDEA");

  public static IvyIdeaFacetType getInstance() {
    return findInstance(IvyIdeaFacetType.class);
  }

  public IvyIdeaFacetType() {
    super(ID, "IvyIDEA", "IvyIDEA");
  }

  @Override
  public IvyIdeaFacetConfiguration createDefaultConfiguration() {
    return new IvyIdeaFacetConfiguration();
  }

  @Override
  public IvyIdeaFacet createFacet(
      @NotNull final Module module,
      final String name,
      @NotNull final IvyIdeaFacetConfiguration configuration,
      @SuppressWarnings("rawtypes") @Nullable final Facet underlyingFacet) {
    return new IvyIdeaFacet(module, this, name, configuration, underlyingFacet);
  }

  @Override
  public boolean isSuitableModuleType(@SuppressWarnings("rawtypes") final ModuleType moduleType) {
    // Allow ivy facets for all module types...
    return true;
    // return
    // IntellijCompatibilityService.getCompatibilityMethods().getJavaModuleType().equals(moduleType);
  }

  @Nullable
  @Override
  public javax.swing.Icon getIcon() {
    return IvyIdeaIcons.MAIN_ICON_SMALL;
  }

  protected IvyIdeaFacetConfiguration configureDetectedFacet(
      final VirtualFile ivyFile, final Collection<? extends IvyIdeaFacetConfiguration> existingFacetConfigurations) {
    if (existingFacetConfigurations.isEmpty()) {
      final IvyIdeaFacetConfiguration defaultConfiguration = createDefaultConfiguration();
      defaultConfiguration.setIvyFile(ivyFile.getPath());
      return defaultConfiguration;
    } else {
      // TODO: only use file that is the closest to the iml file!
      //              http://code.google.com/p/ivyidea/issues/detail?id=1
      return existingFacetConfigurations.iterator().next();
    }
  }
}
