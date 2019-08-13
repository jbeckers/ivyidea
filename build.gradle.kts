import net.ltgt.gradle.errorprone.errorprone
import org.jetbrains.intellij.tasks.PatchPluginXmlTask

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
    errorprone ("com.google.errorprone", "error_prone_core", "2.3.3")
    errorproneJavac("com.google.errorprone", "javac", "9+181-r4173-1")
    testCompile("org.junit.jupiter", "junit-jupiter-api", "5.5.1")
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

tasks.withType<JavaCompile>().configureEach {
    options.errorprone.disableWarningsInGeneratedCode.set(true)
}

tasks.getByName<PatchPluginXmlTask>("patchPluginXml") {
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
        <strong>1.0.14</strong>
        <ul>
            <li>When trying to resolve dependencies without an Ivy settings file, an IllegalArgumentException 	    was thrown when clicking on the 'Open Project Settings' link</li>
        </ul>
        <strong>1.0.13</strong>
        <ul>
            <li>The IvyIDEA settings window was no longer visible in IntelliJ 2016.1</li>
        </ul>
        <strong>1.0.12</strong>
        <ul>
            <li>The configurations to resolve are now stored alphabetically in the .iml file</li>
            <li>Modified files are now saved before starting to resolve the dependencies</li>
        </ul>
        <strong>1.0.11</strong>
        <ul>
            <li>Fixed compatibility issue with IntelliJ 11</li>
            <li>Upgraded internal Apache Ivy to 2.4.0 (including dependencies)</li>
        </ul>
        <strong>1.0.10</strong>
        <ul>
            <li>Fixed compatibility issue with IntelliJ 13.1</li>
        </ul>
        <strong>1.0.9</strong>
        <ul>
            <li>Added an extra configuration option to always attach the source/javadoc artifacts, even when
                they aren't selected by an Ivy configuration.</li>
            <li>Fixed compatibility issue with IntelliJ 13.1</li>
            <li>Upgraded internal Apache Ivy to 2.4.0-rc1 (including dependencies)</li>
        </ul>
        <strong>1.0.8</strong>
        <ul>
            <li>Fixed compatibility issue with IntelliJ 13.0.2 EAP (build 133.609)</li>
        </ul>
        <strong>1.0.7</strong>
        <ul>
            <li>Fixed compatibility issue with IntelliJ 13</li>
            <li>Fixed issue with resolving dependencies of a single module if another module had an incorrect ivy file</li>
        </ul>
        <strong>1.0.6</strong>
        <ul>
            <li>Fixed issue with the 'Only resolve specific ivy configurations'-option on the IvyIDEA facet page</li>
            <li>Fixed compatibility issue with IntelliJ 13 beta1 (build 132.947)</li>
        </ul>
        <strong>1.0.5</strong>
        <ul>
            <li>Improved resolving dependencies in the background</li>
            <li>Added an extra configuration option to always resolve in the background</li>
            <li>Resolving dependencies can now be canceled</li>
            <li>Fixed problem parsing ivy.xml files extending others</li>
            <li>Fixed compatibility issue with IntelliJ 13 EAP (build 130.1619)</li>
        </ul>
        <strong>1.0.4</strong>
        <ul>
            <li>Upgraded internal Apache Ivy to 2.3.0 (including dependencies)</li>
            <li>Added some extra resolve options to the project settings</li>
        </ul>
        <strong>1.0.3</strong>
        <ul>
            <li>Upgraded internal Apache Ivy to 2.3.0-rc2 (including dependencies)</li>
        </ul>
        <strong>1.0.2</strong>
        <ul>
            <li>Upgraded internal Apache Ivy to 2.3.0-rc1 (including dependencies)</li>
            <li>Fixed issue when loading properties files containing cyclic properties</li>
        </ul>
        <strong>1.0.1</strong>
        <ul>
            <li>Bugfix: it was not possible to use the default Ivy settings</li>
        </ul>
        <strong>1.0</strong>
        <ul>
            <li>Added support for 'mar' artifact types (Axis module archives)</li>
            <li>Fixed compatibility issues with IntelliJ 11</li>
            <li>Fixed problem on Windows when the case of the ivy-cache path didn't match the case on disk</li>
        </ul>
        <strong>0.9</strong>
        <ul>
            <li>Upgraded internal Apache Ivy to 2.2.0 (including dependencies)</li>
            <li>Support for using ${'$'}{} style properties in ivy and ivysettings files</li>
            <li>Improved lookup method for artifacts; now useOrigin="true" will also be supported.</li>
            <li>Resolved config names are now listed in the IvyIDEA console</li>
            <li>Resolved library names can now contain the module and or configuration name (with help from wajiii).</li>
            <li>Configurable log level for ivy logging (with help from wajiii)</li>
            <li>Dependencies are now added to the module library with a relative path</li>
            <li>The types used for classes/sources/javadoc artifacts is now configurable</li>
            <li>Several small fixes and improvements</li>
        </ul>
        <strong>0.8</strong>
        <ul>
            <li>Upgraded internal ivy to 2.0.0rc2</li>
            <li>Improved exception handling</li>
        </ul>
        <strong>0.7-alpha</strong>
        <ul>
            <li>IvyIDEA is now compatible with IntelliJ 8.0 (and will run on previous versions as well)</li>
            <li>Switched to JDK 1.5 so the plugin will also run on mac os</li>
            <li>Made looking up intellij module dependencies more lenient; now the revision is ignored when
                identifying dependencies as existing intellij modules rather than jars</li>
        </ul>
        """.trimIndent())
    version("1.0.14")
    sinceBuild("192")
}