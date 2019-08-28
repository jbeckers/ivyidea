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

import static org.clarent.ivyidea.util.ConsoleViewMessageLogger.IvyLogLevel.Debug;
import static org.clarent.ivyidea.util.ConsoleViewMessageLogger.IvyLogLevel.Error;
import static org.clarent.ivyidea.util.ConsoleViewMessageLogger.IvyLogLevel.Info;
import static org.clarent.ivyidea.util.ConsoleViewMessageLogger.IvyLogLevel.None;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** @author Guy Mahieu */
class IvyLogLevelTest {

  @Test
  void test_isMoreVerboseThan() {
    assertTrue(Error.isMoreVerboseThan(None));
    assertTrue(Info.isMoreVerboseThan(None));
    assertFalse(Error.isMoreVerboseThan(Debug));
  }
}
