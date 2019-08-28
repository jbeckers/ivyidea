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

import com.intellij.openapi.roots.OrderRootType;
import java.io.File;
import org.apache.ivy.core.module.descriptor.Artifact;

/** @author Guy Mahieu */
public class ExternalSourceDependency extends ExternalDependency {

  public ExternalSourceDependency(
      final Artifact artifact, final File externalArtifact, final String configurationName) {
    super(artifact, externalArtifact, configurationName);
  }

  @Override
  protected String getTypeName() {
    return "sources";
  }

  @Override
  public OrderRootType getType() {
    return OrderRootType.SOURCES;
  }
}
