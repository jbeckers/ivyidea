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

package org.clarent.ivyidea.resolve.dependency;

import com.intellij.openapi.project.Project;
import java.io.File;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.clarent.ivyidea.config.model.ArtifactTypeSettings;
import org.clarent.ivyidea.intellij.extension.IvyIdeaProjectComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
public final class ExternalDependencyFactory {

  private ExternalDependencyFactory() {
  }

  @Nullable
  public static ExternalDependency createExternalDependency(
      @NotNull final Artifact artifact,
      @Nullable final File file,
      @NotNull final Project project,
      @NotNull final String configurationName) {
    final ArtifactTypeSettings.DependencyCategory category = determineCategory(project, artifact);
    if (category != null) {
      switch (category) {
        case Classes:
          return new ExternalJarDependency(artifact, file, configurationName);
        case Sources:
          return new ExternalSourceDependency(artifact, file, configurationName);
        case Javadoc:
          return new ExternalJavaDocDependency(artifact, file, configurationName);
      }
    }
    return null;
  }

  @Nullable
  public static ArtifactTypeSettings.DependencyCategory determineCategory(
      @NotNull final Project project, @NotNull final Artifact artifact) {
    final ArtifactTypeSettings typeSettings = project.getComponent(IvyIdeaProjectComponent.class)
        .getState().getArtifactTypeSettings();
    if (typeSettings == null) {
      return null;
    }
    return typeSettings.getCategoryForType(artifact.getType());
  }
}
