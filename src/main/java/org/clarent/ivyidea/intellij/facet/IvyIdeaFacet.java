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

package org.clarent.ivyidea.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.module.Module;
import org.clarent.ivyidea.intellij.facet.config.IvyIdeaFacetConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public class IvyIdeaFacet extends Facet<IvyIdeaFacetConfiguration> {

  @Nullable
  public static IvyIdeaFacet getInstance(@NotNull final Module module) {
    return FacetManager.getInstance(module).getFacetByType(IvyIdeaFacetType.ID);
  }

  public IvyIdeaFacet(
      @NotNull Module module,
      @NotNull FacetType<IvyIdeaFacet, IvyIdeaFacetConfiguration> facetType,
      String name,
      @NotNull IvyIdeaFacetConfiguration configuration,
      @SuppressWarnings("rawtypes") Facet underlyingFacet) {
    super(facetType, module, name, configuration, underlyingFacet);
  }

  public IvyIdeaFacet(@NotNull Module module) {
    this(
        module, FacetTypeRegistry.getInstance().findFacetType(IvyIdeaFacetType.ID),
        "IvyIdeaFacet",
        new IvyIdeaFacetConfiguration(),
        null);
  }
}
