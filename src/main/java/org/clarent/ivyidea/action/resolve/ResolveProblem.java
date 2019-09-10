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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
class ResolveProblem {

  @NotNull
  private final String targetId;
  @NotNull
  private final String message;
  @Nullable
  private final Throwable throwable;

  ResolveProblem(@NotNull final String targetId, @NotNull final String message) {
    this(targetId, message, null);
  }

  ResolveProblem(
      @NotNull final String targetId,
      @NotNull final String message,
      @Nullable final Throwable throwable) {
    this.targetId = targetId;
    this.message = message;
    this.throwable = throwable;
  }

  @NotNull
  public String getTargetId() {
    return targetId;
  }

  @NotNull
  public String getMessage() {
    return message;
  }

  @Nullable
  public Throwable getThrowable() {
    return throwable;
  }

  @NotNull
  @Override
  public String toString() {
    return targetId + ":\t" + message;
  }
}
