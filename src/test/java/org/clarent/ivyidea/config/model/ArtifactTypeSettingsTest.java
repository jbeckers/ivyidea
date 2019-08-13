/*
 * Copyright 2010 Guy Mahieu
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

package org.clarent.ivyidea.config.model;

import org.junit.jupiter.api.Test;

import static org.clarent.ivyidea.config.model.ArtifactTypeSettings.DependencyCategory.Classes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Guy Mahieu
 */
class ArtifactTypeSettingsTest {

    @Test
    void testNewObjectAlwaysEmpty() {
        assertTrue(new ArtifactTypeSettings().isConfigurationEmpty());
    }

    @Test
    void testObjectWithOnlyEmptyStringsAlwaysEmpty() {
        final ArtifactTypeSettings artifactTypeSettings = new ArtifactTypeSettings();
        artifactTypeSettings.setClassesTypes("");
        assertTrue(artifactTypeSettings.isConfigurationEmpty());
    }

    @Test
    void testObjectWithDataNeverEmpty() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        typeSettings.setTypesForCategory(Classes, "jar");
        assertFalse(typeSettings.isConfigurationEmpty());
    }

    @Test
    void testTypeNamesAreCaseInsensitive() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        typeSettings.setTypesForCategory(Classes, "JAR");
        assertEquals(Classes, typeSettings.getCategoryForType("jar"));
    }

    @Test
    void testCorrectCategoryReturnedForType() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        typeSettings.setTypesForCategory(Classes, "jar");
        assertEquals(Classes, typeSettings.getCategoryForType("jar"));
        assertEquals(Classes, typeSettings.getCategoryForType("jar "));
        assertEquals(Classes, typeSettings.getCategoryForType(" jar"));
        assertEquals(Classes, typeSettings.getCategoryForType(" jar "));
    }
    @Test
    void testOrderOfTypesPreservedWhenSettingAndGetting() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        final String expected = "a, b, c, d, e, f, g";
        typeSettings.setTypesForCategory(Classes, expected);
        final String actual = typeSettings.getTypesStringForCategory(Classes);
        assertEquals(expected, actual);
    }

    @Test
    void testNullForUnknownType() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        typeSettings.setTypesForCategory(Classes, "jar");
        assertNull(typeSettings.getCategoryForType("foo"));
    }

    @Test
    void testSerializationGettersDoNotReturnDefaultValuesIfObjectEmpty() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        assertEquals("", typeSettings.getClassesTypes());
        assertEquals("", typeSettings.getSourcesTypes());
        assertEquals("", typeSettings.getJavadocTypes());
    }

    @Test
    void testGetTypeStringReturnsDefaultValuesIfObjectEmpty() {
        final ArtifactTypeSettings typeSettings = new ArtifactTypeSettings();
        for (ArtifactTypeSettings.DependencyCategory category : ArtifactTypeSettings.DependencyCategory.values()) {
            final String typesStringForCategory = typeSettings.getTypesStringForCategory(category);
            assertNotNull(typesStringForCategory);
            assertTrue(typesStringForCategory.length() > 0);
        }
    }

}
