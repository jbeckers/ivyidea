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

package org.clarent.ivyidea.exception.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.labels.LinkLabel;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Standard dialog for showing exception feedback to users from IvyIDEA. Allows you to specify a
 * hyperlink so the user do a single click to navigate to the UI where the problem can be resolved.
 *
 * @author Guy Mahieu
 */
public final class IvyIdeaExceptionDialog<T> extends DialogWrapper {

  private JPanel rootPanel;
  private JTextArea txtMessage;
  private LinkLabel<T> lblLink;

  /** Shows a standard IvyIDEA exception dialog without a hyperlink. */
  public static void showModalDialog(final String title, final Throwable exception, final Project project) {
    showModalDialog(title, exception, project, null);
  }

  /**
   * Shows a standard IvyIDEA exception dialog with a hyperlink that takes its parameters from the
   * given linkBehavior.
   */
  public static <T> void showModalDialog(
      final String title, final Throwable exception, final Project project, final LinkBehavior<T> linkBehavior) {
    final IvyIdeaExceptionDialog<T> dlg = new IvyIdeaExceptionDialog<>(project);
    dlg.setTitle(title);
    dlg.setMessageFromThrowable(exception);
    dlg.setLinkBehavior(linkBehavior);
    dlg.show();
  }

  public IvyIdeaExceptionDialog(final Project project) {
    super(project, false);

    setButtonsAlignment(SwingConstants.CENTER);

    // By default we do not show a link
    lblLink.setVisible(false);

    // You have to call this or nothing is shown!
    init();
  }

  public JPanel getRootPanel() {
    return rootPanel;
  }

  public void setMessage(@NotNull final String message) {
    txtMessage.setText(message);
    // scroll to top
    txtMessage.setSelectionStart(0);
    txtMessage.setSelectionEnd(0);
  }

  public void setMessageFromThrowable(@NotNull final Throwable exception) {
    String message = exception.getMessage() + '\n';
    Throwable cause = exception.getCause();
    final int maxDepth = 20;
    int currDepth = 0;
    while (currDepth++ <= maxDepth && cause != null) {
      message += "\nCaused by: " + cause.getMessage();
      cause = cause.getCause();
    }
    if (cause != null) {
      message += "\nMore causes skipped.";
    }
    setMessage(message);
  }

  public void setLinkBehavior(@Nullable final LinkBehavior<T> linkBehavior) {
    if (linkBehavior == null
        || linkBehavior.getLinkText() == null
        || linkBehavior.getLinkText().trim().isEmpty()) {
      lblLink.setText("");
      lblLink.setListener(null, null);
      lblLink.setVisible(false);
    } else {
      lblLink.setText(linkBehavior.getLinkText());
      lblLink.setListener(linkBehavior.getLinkListener(), linkBehavior.getData());
      lblLink.setVisible(true);
    }
  }

  @Override
  protected JComponent createCenterPanel() {
    return rootPanel;
  }

  @NotNull
  @Override
  protected Action[] createActions() {
    return new Action[] {getOKAction()};
  }
}
