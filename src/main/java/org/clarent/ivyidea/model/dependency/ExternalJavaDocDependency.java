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

import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import java.io.File;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public class ExternalJavaDocDependency extends ExternalDependency {

  public ExternalJavaDocDependency(
      @NotNull final Artifact artifact, @Nullable final File externalArtifact,
      @NotNull final String configurationName) {
    super(artifact, externalArtifact, configurationName);
  }

  @NotNull
  @Override
  protected String getTypeName() {
    return "javadoc";
  }

  @NotNull
  @Override
  public OrderRootType getType() {
    return JavadocOrderRootType.getInstance();
  }
}
