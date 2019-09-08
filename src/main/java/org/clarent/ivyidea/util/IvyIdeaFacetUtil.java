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

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.File;
import org.clarent.ivyidea.IvyIdeaConstants;
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class IvyIdeaFacetUtil {

  private static final Logger LOGGER =
      Logger.getInstance("#org.clarent.ivyidea.util.IvyIdeaFacetUtil");

  @Contract(pure = true)
  private IvyIdeaFacetUtil() {
  }

  /**
   * Returnes the ivy file for the given module.
   *
   * @param module the IntelliJ module for which you want to lookup the ivy file
   * @return the File representing the ivy xml file for the given module
   * @throws RuntimeException if the given module does not have an IvyIDEA facet configured.
   */
  @NotNull
  public static Try<File> getIvyFile(@NotNull final Module module) {
    return getConfiguration(module)
        .toTry(
            () ->
                new RuntimeException(
                    "Internal error: No IvyIDEA facet configured for module "
                        + module.getName()
                        + ", but an attempt was made to use it as such."))
        .map(ivyIdeaFacetConfiguration -> ivyIdeaFacetConfiguration.getState().getIvyFile())
        .filter(file -> !file.isEmpty())
        .mapTry(File::new);
  }

  // Fixed in java 10 https://bugs.openjdk.java.net/browse/JDK-8063054
  @SuppressWarnings({"rawtypes", "RedundantSuppression"})
  @NotNull
  public static Option<IvyIdeaFacetConfiguration> getConfiguration(@NotNull final Module module) {
    return Option.of(
        FacetManager.getInstance(module).getFacetByType(IvyIdeaConstants.FACET_TYPE_ID))
        .map(Facet::getConfiguration)
        .onEmpty(
            () ->
                LOGGER.info(
                    "LOG00050: Module "
                        + module.getName()
                        + " does not have the IvyIDEA facet configured; ignoring."));
  }

  @Contract("null -> false")
  public static boolean isIvyModule(@Nullable final Module module) {
    return module != null
        && FacetManager.getInstance(module).getFacetByType(IvyIdeaConstants.FACET_TYPE_ID) != null;
  }
}
