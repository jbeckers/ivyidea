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

package org.clarent.ivyidea.action.resolve;

import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.ivy.Ivy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Background thread that monitors the ProgressIndicator.
 *
 * @author Maarten Coene
 */
class ProgressMonitorThread extends Thread {

  @NotNull
  private final ProgressIndicator indicator;
  @NotNull
  private final Thread resolveThread;
  @Nullable
  private Ivy ivy = null;

  ProgressMonitorThread(
      @NotNull final ProgressIndicator indicator, @NotNull final Thread resolveThread) {
    super("ProgressIndicator Monitor");
    this.indicator = indicator;
    this.resolveThread = resolveThread;
  }

  public void setIvy(@NotNull final Ivy ivy) {
    this.ivy = ivy;
  }

  @Override
  public void run() {
    while (indicator.isRunning()) {
      if (ivy != null && indicator.isCanceled()) {
        ivy.interrupt(resolveThread);
        return;
      }
      try {
        Thread.sleep(500L);
      } catch (final InterruptedException ignored) {
      }
    }
  }
}
