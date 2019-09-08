# IvyIDEA

[![Build Status](https://travis-ci.org/jbeckers/ivyidea.svg?branch=master)](https://travis-ci.org/jbeckers/ivyidea)
[![codecov](https://codecov.io/gh/jbeckers/ivyidea/branch/master/graph/badge.svg)](https://codecov.io/gh/jbeckers/ivyidea)

An [Intellij IDEA](https://www.jetbrains.com/idea/) plugin to resolve module dependencies through  [Apache Ivy](https://ant.apache.org/ivy/).

Get it from the IntelliJ Plugin Marketplace or from the [plugin page](https://plugins.jetbrains.com/plugin/3612-ivyidea).

## Usage

* Configure the ivysettings file (usually called ivyconf.xml or ivysettings.xml) in the project settings. 
* Add an IvyIDEA facet for every module you have that uses Ivy (if it was not autodetected already) and point it to the ivy.xml file for that module.
* You can now resolve your dependencies through the Tools > IvyIDEA Resolve menu or through the right click menu of the project explorer.

For a more detailed guide with screenshots, see [Getting started](getting-started).

## Features
  * Apache Ivy integration (up to v2.5.0-rc1); no need for external ant build scripts to be called
  * Detection of Intellij module dependencies; these are added as module references
  * Detect source/document/jar type artifacts of dependencies and add them as such to the module
  * Creation of module library with all resolved Ivy dependencies
  * Facets for Ivy modules containing ivy.xml files
  * Allow setting the Ivy configurations to resolve and add as a library (ide/default/provided/tools/test/...)
  * Compatibile with mac os (jdk 1.5)
  * Compatibile with IntelliJ 7 - 2019+
  * Show Ivy resolve messages in a console (basic)

#### Nice-to-haves
  * Show resolve report in Intellij (report xml transformed through the Ivy xsl)
  * Respect the order of dependencies in the ivy.xml file if feasible (this means that a module library can't always be used -> module and jar dependencies might be interleaved)
  * ~~Auto re-resolve on changed ivy.xml file~~ (is this even useful?)

#### Wild ideas for the future
  * Intellisense in Ivy files (known organisations, artifacts, ...)
  * Any remarks/suggestions of your own? [Just send an e-mail!](mailto:guy.mahieu@gmail.com)

## Getting started

### Module Settings

#### Auto detection

With a bit of luck, your ivy-enabled modules will be detected automatically by IvyIDEA. You'll see a popup stating that one or more new facets are detected on opening your project.

"ivyidea-facetpopup.png"

Note: it is possible that you missed the popup as it is only displayed briefly. In this case you can still see if a new facet was detected by the presence of a blinking gear icon in the statusbar:

"ivyidea-facetgear.png"

If you click on the 'More...' link (or on the blinking gear icon), you'll get a screen that looks like this:

"ivyidea-facetsdetected.png"

Click 'Edit Settings...' to make sure that the correct ivy.xml file is chosen for each module. This will take you to the Module Settings screen, which is described in greater detail below. _Note: there is a known issue that the Ivy file displayed here may differ from the one that is configured in the module settings, so make sure to check this if your module contains multiple ivy.xml files!_

#### Manual setup

If the autodetection did not work for some reason, or you want to changes the settings that were automatically chosen, you can manually setup an IvyIDEA facet for each module that needs it. This can be done by opening the project settings and clicking on the 'Modules' item.

You can add a facet to a module simply by clicking the [+] icon above the module list, or by right clicking on the module name as shown here:

"ivyidea-manualfacetsetup.png"

Once the facet is added to the tree, you can change its configuration by clicking on it, you will get the following screen:

"ivyidea-moduleconfig.png"

Here you need to specify the Ivy file for your module. This is the file which holds the dependency information for your module. _More information about Ivy files can be found in the_ apache Ivy quick start guide

You can also select the configuration(s) that you want to resolve in IntelliJ; the configurations table will be filled automatically when you have selected a valid ivy.xml file. _Note that if you use an include file for your configurations in your ivy.xml and you use a ${} style property inside the filename, the Ivy settings file needs to be set as well (here or in the project settings) in order to correctly lookup the available configurations. The current version of the plugin will not report this, it will simply not show any data in the table._

### Project Settings

If you do not specify an Ivy settings file with your module (meaning that you left the 'Use from project settings' checkbox checked), you obviously need to specify one at the project level. The following screenshot shows you how to do this:

"ivyidea-projectsettings.png"

You can also choose to enable or disable the validation of ivy.xml files; this can be useful if you have minor errors in ivy.xml files that you do not control but which do not cause the resolve process to fail. Disabling this will also probably be a bit faster.

### Resolving Dependencies

This can be done through the Tools menu

"ivyidea-toolsmenu.png"

but also through the right-click menu in the project view or in the editor  
_(note that the IvyIDEA menu option is not necessarily at the top of the menu)_

"ivyidea-projectrightclick.png"

It is possible to do a resolve for the current module (if you are inside the context of a module) or for all modules.

There is also an option to remove all IntelliJ module libraries that were a result of the Ivy resolve process. This does not remove any files from the filesystem, it will simply remove the IvyIDEA-resolved module library from all modules that have an IvyIDEA facet configured. It will also not remove module to module dependencies that were made by IvyIDEA!
