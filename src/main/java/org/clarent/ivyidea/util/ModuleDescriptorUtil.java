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

import com.intellij.openapi.diagnostic.Logger;
import io.vavr.control.Try;
import java.io.File;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.IvyContext;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ModuleDescriptorUtil {

  private static final Logger LOGGER = Logger
      .getInstance("#org.clarent.ivyidea.util.ModuleDescriptorUtil");

  @Contract(pure = true)
  private ModuleDescriptorUtil() {
  }

  /**
   * Parses the given ivyFile into a ModuleDescriptor using the given settings.
   *
   * @param ivyFile the ivy file to parse
   * @param ivy     the Ivy engine to use, configured with the appropriate settings
   * @return the ModuleDescriptor object representing the ivy file.
   */
  public static Try<ModuleDescriptor> parseDescriptor(
      @NotNull final File ivyFile, @NotNull final Try<? extends Ivy> ivy) {
    return ivy.andThen(Ivy::pushContext)
        .andThen(() -> LOGGER.info("LOG00030: Parsing ivy file " + ivyFile.getAbsolutePath()))
        .mapTry(
            ivyEngine ->
                ModuleDescriptorParserRegistry.getInstance()
                    .parseDescriptor(ivyEngine.getSettings(), ivyFile.toURI().toURL(), false))
        .andThen(IvyContext::popContext);
  }
}
