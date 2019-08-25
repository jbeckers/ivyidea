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

import net.ltgt.gradle.errorprone.errorprone
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishTask

group = "org.clarent.ivyidea"
version = "1.0.14"

repositories {
    mavenCentral()
}

dependencies {
    compile("org.apache.ivy", "ivy", "2.5.0-rc1")
    runtimeOnly("org.apache.httpcomponents", "httpclient", "4.5.9") // optional httpclient for better http handling
    runtimeOnly("oro", "oro", "2.0.8") // to use optional glob matcher
    runtimeOnly("org.apache.commons", "commons-vfs2", "2.4") // optional VirtualFileSystem(VFS) support
    runtimeOnly("com.jcraft", "jsch", "0.1.55") // optional SFTP support
    runtimeOnly("com.jcraft", "jsch.agentproxy", "0.0.9") // optional SFTP support
    runtimeOnly("com.jcraft", "jsch.agentproxy.connector-factory", "0.0.9") // optional SFTP support
    runtimeOnly("com.jcraft", "jsch.agentproxy.jsch", "0.0.9") // optional SFTP support
    runtimeOnly("org.bouncycastle", "bcpg-jdk15on", "1.62") // optional
    runtimeOnly("org.bouncycastle", "bcprov-jdk15on", "1.62") // optional
    errorprone("com.google.errorprone", "error_prone_core", "2.3.3")
    errorproneJavac("com.google.errorprone", "javac", "9+181-r4173-1")
    testCompile("org.junit.jupiter", "junit-jupiter-api", "5.5.1")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.5.1")
}

plugins {
    java
    id("org.jetbrains.intellij") version "0.4.10"
    id("net.ltgt.errorprone") version "0.8.1"
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "LATEST-EAP-SNAPSHOT"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(arrayOf("-Xlint:all"))
//    options.compilerArgs.addAll(arrayOf("-Werror"))
        options.errorprone.disableWarningsInGeneratedCode.set(true)
        options.errorprone.allDisabledChecksAsWarnings.set(true)
        options.errorprone.disable("Var", "StaticOrDefaultInterfaceMethod", "MissingSummary")
    }

    getByName<PatchPluginXmlTask>("patchPluginXml") {
        pluginId("org.clarent.ivyidea")
        pluginDescription("""
        Resolves module dependencies through Ivy
        <p>Features:
            <ul>
                <li>Apache Ivy integration (up to v2.4.0); no need for external ant build scripts to be called</li>
                <li>Automatic ivy configuration of modules using facets (for modules containing an ivy.xml file)</li>
                <li>Detection of dependencies that are really other intellij modules in the same project; these are added as module references</li>
                <li>Detect source/document/jar type ivy artifacts in dependencies and add them as such to the module</li>
                <li>Creation of a module library with all resolved ivy dependencies</li>
                <li>Ivy configurations that need to be resolved can be chosen for each module</li>
                <li>Properties can be injected into the ivy resolve process</li>
            </ul>
        </p>
        """.trimIndent())
        changeNotes("""
        <strong>1.0.15</strong>
        <ul>
            <li>Changes here</li>
        </ul>
        """.trimIndent())
        version("1.0.14")
        sinceBuild("192")
    }

    getByName<PublishTask>("publishPlugin") {
        findProperty("pluginsRepoToken")?.let { token(it) }
    }
}