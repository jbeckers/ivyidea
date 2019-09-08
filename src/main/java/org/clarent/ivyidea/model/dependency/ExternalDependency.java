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
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.util.PathUtil;
import java.io.File;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.clarent.ivyidea.model.ModifiableRootModelWrapper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a dependency to an external artifact somewhere on the filesystem.
 *
 * @author Guy Mahieu
 */
public abstract class ExternalDependency implements ResolvedDependency {

  @NotNull
  private static final Logger LOGGER =
      Logger.getInstance("#org.clarent.ivyidea.model.dependency.ExternalDependency");

  @NotNull
  private final Artifact artifact;
  @NotNull
  private final String configurationName;
  @Nullable
  private final File localFile;

  @Contract(pure = true)
  ExternalDependency(
      @NotNull final Artifact artifact,
      @Nullable final File localFile,
      @NotNull final String configurationName) {
    this.artifact = artifact;
    this.localFile = localFile;
    this.configurationName = configurationName;
  }

  @Nullable
  public File getLocalFile() {
    return localFile;
  }

  @Nullable
  public String getUrlForLibraryRoot() {
    return localFile == null ? null : VfsUtil.getUrlForLibraryRoot(localFile);
  }

  @NotNull
  public String getConfigurationName() {
    return configurationName;
  }

  @Override
  public void addTo(@NotNull final ModifiableRootModelWrapper modifiableRootModelWrapper) {
    if (localFile == null) {
      LOGGER.warn(
          "LOG00240: Not registering external "
              + getTypeName()
              + " dependency for module "
              + artifact.getModuleRevisionId()
              + " as the file does not seem to exist.");
      return;
    }
    final String artifactPath = localFile.getAbsolutePath();
    if (!new File(localFile.getAbsolutePath()).exists()) {
      LOGGER.warn(
          "LOG00210: Not registering external "
              + getTypeName()
              + " file dependency as the file does not seem to exist: "
              + artifactPath);
      return;
    }
    if (modifiableRootModelWrapper.alreadyHasDependencyOnLibrary(this)) {
      LOGGER.info(
          "LOG00200: Not re-registering external "
              + getTypeName()
              + " file dependency "
              + artifactPath
              + " as it is already present.");
      return;
    }
    LOGGER.info(
        "LOG00220: Registering external " + getTypeName() + " file dependency: " + artifactPath);
    modifiableRootModelWrapper.addExternalDependency(this);
  }

  public boolean isSameDependency(@NotNull final String url) {
    return localFile != null
        && FileUtil.filesEqual(localFile, new File(PathUtil.toPresentableUrl(url)));
  }

  @NotNull
  public abstract OrderRootType getType();

  @NotNull
  protected abstract String getTypeName();
}
