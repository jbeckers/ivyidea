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

import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.util.indexing.FileContent;
import java.util.Collection;
import org.clarent.ivyidea.facet.settings.IvyIdeaFacetConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Maarten Coene */
public class IvyIdeaFacetDetector
    extends FacetBasedFrameworkDetector<IvyIdeaFacet, IvyIdeaFacetConfiguration> {

  public IvyIdeaFacetDetector() {
    super("IvyIDEA");
  }

  @NotNull
  @Override
  public FacetType<IvyIdeaFacet, IvyIdeaFacetConfiguration> getFacetType() {
    return IvyIdeaFacetType.getInstance();
  }

  @Override
  @NotNull
  public FileType getFileType() {
    return FileTypeManager.getInstance().getFileTypeByExtension("xml");
  }

  @Override
  @NotNull
  public ElementPattern<FileContent> createSuitableFilePattern() {
    return FileContentPattern.fileContent().withName("ivy.xml").xmlWithRootTag("ivy-module");
  }

  @Override
  @Nullable
  protected IvyIdeaFacetConfiguration createConfiguration(final Collection<VirtualFile> files) {
    final IvyIdeaFacetConfiguration result = super.createConfiguration(files);

    if (result != null && !files.isEmpty()) {
      result.setIvyFile(files.iterator().next().getPath());
    }

    return result;
  }
}
