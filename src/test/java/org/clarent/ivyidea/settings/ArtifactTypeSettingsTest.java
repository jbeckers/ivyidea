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

package org.clarent.ivyidea.settings;

import static org.clarent.ivyidea.model.dependency.DependencyCategory.Classes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.clarent.ivyidea.model.dependency.DependencyCategory;
import org.clarent.ivyidea.settings.IvyIdeaProjectState.ArtifactTypeSettings;
import org.junit.jupiter.api.Test;

/** @author Guy Mahieu */
class ArtifactTypeSettingsTest {

  @Test
  void testNewObjectAlwaysEmpty() {
    assertTrue(new ArtifactTypeSettings().getManager().isConfigurationEmpty());
  }

  @Test
  void testObjectWithOnlyEmptyStringsAlwaysEmpty() {
    final IvyIdeaProjectState.ArtifactTypeSettings artifactTypeSettings = new IvyIdeaProjectState.ArtifactTypeSettings();
    artifactTypeSettings.setClassesTypes("");
    assertTrue(artifactTypeSettings.getManager().isConfigurationEmpty());
  }

  @Test
  void testObjectWithDataNeverEmpty() {
    final IvyIdeaProjectState.ArtifactTypeSettings typeSettings = new IvyIdeaProjectState.ArtifactTypeSettings();
    typeSettings.getManager().setTypesForCategory(Classes, "jar");
    assertFalse(typeSettings.getManager().isConfigurationEmpty());
  }

  @Test
  void testTypeNamesAreCaseInsensitive() {
    final IvyIdeaProjectState.ArtifactTypeSettings typeSettings = new IvyIdeaProjectState.ArtifactTypeSettings();
    typeSettings.getManager().setTypesForCategory(Classes, "JAR");
    assertEquals(Classes, typeSettings.getManager().getCategoryForType("jar"));
  }

  @Test
  void testCorrectCategoryReturnedForType() {
    final IvyIdeaProjectState.ArtifactTypeSettings typeSettings = new IvyIdeaProjectState.ArtifactTypeSettings();
    typeSettings.getManager().setTypesForCategory(Classes, "jar");
    assertEquals(Classes, typeSettings.getManager().getCategoryForType("jar"));
    assertEquals(Classes, typeSettings.getManager().getCategoryForType("jar "));
    assertEquals(Classes, typeSettings.getManager().getCategoryForType(" jar"));
    assertEquals(Classes, typeSettings.getManager().getCategoryForType(" jar "));
  }

  @Test
  void testOrderOfTypesPreservedWhenSettingAndGetting() {
    final IvyIdeaProjectState.ArtifactTypeSettings typeSettings = new IvyIdeaProjectState.ArtifactTypeSettings();
    final String expected = "a, b, c, d, e, f, g";
    typeSettings.getManager().setTypesForCategory(Classes, expected);
    final String actual = typeSettings.getManager().getTypesStringForCategory(Classes);
    assertEquals(expected, actual);
  }

  @Test
  void testNullForUnknownType() {
    final IvyIdeaProjectState.ArtifactTypeSettings typeSettings = new IvyIdeaProjectState.ArtifactTypeSettings();
    typeSettings.getManager().setTypesForCategory(Classes, "jar");
    assertNull(typeSettings.getManager().getCategoryForType("foo"));
  }

  @Test
  void testSerializationGettersDoNotReturnDefaultValuesIfObjectEmpty() {
    final IvyIdeaProjectState.ArtifactTypeSettings typeSettings = new IvyIdeaProjectState.ArtifactTypeSettings();
    assertEquals("", typeSettings.getClassesTypes());
    assertEquals("", typeSettings.getSourcesTypes());
    assertEquals("", typeSettings.getJavadocTypes());
  }

  @Test
  void testGetTypeStringReturnsDefaultValuesIfObjectEmpty() {
    final IvyIdeaProjectState.ArtifactTypeSettings typeSettings = new IvyIdeaProjectState.ArtifactTypeSettings();
    for (final DependencyCategory category : DependencyCategory.values()) {
      final String typesStringForCategory = typeSettings.getManager()
          .getTypesStringForCategory(category);
      assertNotNull(typesStringForCategory);
      assertFalse(typesStringForCategory.isEmpty());
    }
  }
}
