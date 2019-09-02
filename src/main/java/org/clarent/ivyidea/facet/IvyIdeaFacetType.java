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

package org.clarent.ivyidea.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import javax.swing.Icon;
import org.clarent.ivyidea.IvyIdeaConstants;
import org.clarent.ivyidea.IvyIdeaIcons;
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public class IvyIdeaFacetType extends FacetType<IvyIdeaFacet, IvyIdeaFacetConfiguration> {

  public IvyIdeaFacetType() {
    super(
        IvyIdeaConstants.FACET_TYPE_ID,
        IvyIdeaConstants.FACET_STRING_ID,
        IvyIdeaConstants.IVY_IDEA);
  }

  @Override
  public IvyIdeaFacetConfiguration createDefaultConfiguration() {
    return new IvyIdeaFacetConfiguration();
  }

  @Override
  public IvyIdeaFacet createFacet(
      @NotNull final Module module,
      @NotNull final String name,
      @NotNull final IvyIdeaFacetConfiguration configuration,
      @SuppressWarnings("rawtypes") @Nullable final Facet underlyingFacet) {
    return new IvyIdeaFacet(module, this, name, configuration, underlyingFacet);
  }

  @Override
  public boolean isSuitableModuleType(@SuppressWarnings("rawtypes") final ModuleType moduleType) {
    // Allow ivy facets for all module types...
    return true;
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return IvyIdeaIcons.MAIN_ICON_SMALL;
  }
}
