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

import com.intellij.util.net.HttpConfigurable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.ivy.core.settings.TimeoutConstraint;
import org.apache.ivy.util.CopyProgressListener;
import org.apache.ivy.util.url.TimeoutConstrainedURLHandler;
import org.apache.ivy.util.url.URLHandlerDispatcher;
import org.apache.ivy.util.url.URLHandlerRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author Guy Mahieu */
@SuppressWarnings("NullableProblems")
final class IntellijProxyURLHandler implements TimeoutConstrainedURLHandler {

  @NotNull
  private final TimeoutConstrainedURLHandler delegate;

  private IntellijProxyURLHandler(@NotNull final TimeoutConstrainedURLHandler urlHandler) {
    this.delegate = urlHandler;
  }

  public static void setupHttpProxy() {
    final URLHandlerDispatcher dispatcher = new URLHandlerDispatcher();
    final TimeoutConstrainedURLHandler httpHandler =
        new IntellijProxyURLHandler(URLHandlerRegistry.getHttp());
    dispatcher.setDownloader("http", httpHandler);
    dispatcher.setDownloader("https", httpHandler);
    URLHandlerRegistry.setDefault(dispatcher);
  }

  @Override
  public boolean isReachable(@NotNull final URL url) {
    try {
      HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    } catch (final IOException e) {
      return UNAVAILABLE.isReachable();
    }
    return delegate.isReachable(url);
  }

  @Override
  public boolean isReachable(@NotNull final URL url, final int timeout) {
    try {
      HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    } catch (final IOException e) {
      return UNAVAILABLE.isReachable();
    }
    return delegate.isReachable(url, timeout);
  }

  @Override
  public boolean isReachable(
      @NotNull final URL url, @Nullable final TimeoutConstraint timeoutConstraint) {
    try {
      HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    } catch (final IOException e) {
      return UNAVAILABLE.isReachable();
    }
    return delegate.isReachable(url, timeoutConstraint);
  }

  @Override
  public long getContentLength(@NotNull final URL url) {
    try {
      HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    } catch (final IOException e) {
      return UNAVAILABLE.getContentLength();
    }
    return delegate.getContentLength(url);
  }

  @Override
  public long getContentLength(@NotNull final URL url, final int timeout) {
    try {
      HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    } catch (final IOException e) {
      return UNAVAILABLE.getContentLength();
    }
    return delegate.getContentLength(url, timeout);
  }

  @Override
  public long getContentLength(
      @NotNull final URL url, @Nullable final TimeoutConstraint timeoutConstraint) {
    try {
      HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    } catch (final IOException e) {
      return UNAVAILABLE.getContentLength();
    }
    return delegate.getContentLength(url, timeoutConstraint);
  }

  @Override
  public long getLastModified(@NotNull final URL url) {
    try {
      HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    } catch (final IOException e) {
      return UNAVAILABLE.getLastModified();
    }
    return delegate.getLastModified(url);
  }

  @Override
  public long getLastModified(@NotNull final URL url, final int timeout) {
    try {
      HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    } catch (final IOException e) {
      return UNAVAILABLE.getLastModified();
    }
    return delegate.getLastModified(url, timeout);
  }

  @Override
  public long getLastModified(
      @NotNull final URL url, @Nullable final TimeoutConstraint timeoutConstraint) {
    try {
      HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    } catch (final IOException e) {
      return UNAVAILABLE.getLastModified();
    }
    return delegate.getLastModified(url, timeoutConstraint);
  }

  @Override
  @NotNull
  public URLInfo getURLInfo(@NotNull final URL url) {
    try {
      HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    } catch (final IOException e) {
      return UNAVAILABLE;
    }
    return delegate.getURLInfo(url, null);
  }

  @Override
  @NotNull
  public URLInfo getURLInfo(@NotNull final URL url, final int timeout) {
    try {
      HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    } catch (final IOException e) {
      return UNAVAILABLE;
    }
    return delegate.getURLInfo(url, timeout);
  }

  @Override
  @NotNull
  public URLInfo getURLInfo(
      @NotNull final URL url, @Nullable final TimeoutConstraint timeoutConstraint) {
    try {
      HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    } catch (final IOException e) {
      return UNAVAILABLE;
    }
    return delegate.getURLInfo(url, timeoutConstraint);
  }

  @Override
  @NotNull
  public InputStream openStream(@NotNull final URL url) throws IOException {
    HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    return delegate.openStream(url);
  }

  @Override
  @NotNull
  public InputStream openStream(
      @NotNull final URL url, @Nullable final TimeoutConstraint timeoutConstraint)
      throws IOException {
    HttpConfigurable.getInstance().prepareURL(url.toExternalForm());
    return delegate.openStream(url, timeoutConstraint);
  }

  @Override
  public void download(
      @NotNull final URL src, @NotNull final File dest, @NotNull final CopyProgressListener l)
      throws IOException {
    HttpConfigurable.getInstance().prepareURL(src.toExternalForm());
    delegate.download(src, dest, l);
  }

  @Override
  public void download(
      @NotNull final URL src,
      @NotNull final File dest,
      @NotNull final CopyProgressListener listener,
      @Nullable final TimeoutConstraint timeoutConstraint)
      throws IOException {
    HttpConfigurable.getInstance().prepareURL(src.toExternalForm());
    delegate.download(src, dest, listener, timeoutConstraint);
  }

  @Override
  public void upload(
      @NotNull final File src, @NotNull final URL dest, @NotNull final CopyProgressListener l)
      throws IOException {
    HttpConfigurable.getInstance().prepareURL(dest.toExternalForm());
    delegate.upload(src, dest, l);
  }

  @Override
  public void upload(
      @NotNull final File src,
      @NotNull final URL dest,
      @NotNull final CopyProgressListener listener,
      @Nullable final TimeoutConstraint timeoutConstraint)
      throws IOException {
    HttpConfigurable.getInstance().prepareURL(dest.toExternalForm());
    delegate.upload(src, dest, listener, timeoutConstraint);
  }

  @Override
  public void setRequestMethod(final int requestMethod) {
    delegate.setRequestMethod(requestMethod);
  }
}
