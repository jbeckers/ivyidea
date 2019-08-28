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
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration;
import org.jetbrains.annotations.NotNull;

/** @author Guy Mahieu */
public class IvyIdeaFacet extends Facet<IvyIdeaFacetConfiguration> {

  IvyIdeaFacet(
      @NotNull final Module module,
      @NotNull final FacetType<IvyIdeaFacet, IvyIdeaFacetConfiguration> facetType,
      @NotNull final String name,
      @NotNull final IvyIdeaFacetConfiguration configuration,
      @SuppressWarnings("rawtypes") final Facet underlyingFacet) {
    super(facetType, module, name, configuration, underlyingFacet);
  }

  @Override
  public final IvyIdeaFacet clone() {
    throw new AssertionError();
  }
}
