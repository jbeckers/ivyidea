# Changelog

## [Unreleased]

## [1.0.14]
- Fixed IllegalArgumentException in ShowSettingsUtilImpl
 
  When trying to resolve dependencies without an Ivy settings file, an IllegalArgumentException was thrown when clicking on the 'Open Project Settings' link

## [1.0.13]
- This release fixes some IntelliJ 2016.1 compatibility issues.

## [1.0.12]
_This version was never officially released_

## [1.0.11]
- IvyIDEA 1.0.11 has been released. The most important change is the upgrade to Apache Ivy 2.4.0.

## [1.0.10]
- This release fixes a compatibility problem with IntelliJ IDEA 13.1.

## [1.0.9]
- This release contains Apache Ivy 2.4.0-RC1 and has improved source/javadoc handling.

## [1.0.8]
- This release fixes a compatibility problem with IntelliJ IDEA 13.0.2 EAP (build 133.609).

## [1.0.7]
- This is a bugfix release and is also compatible with IntelliJ 13.

## [1.0.6]
- This is a bugfix release with improved compatibility with IntelliJ IDEA 13 beta1.

## [1.0.5]
- This release contains background resolving, bugfixes and IntelliJ 13 EAP compatibility.

## [1.0.4]
- Updated to Apache Ivy 2.3.0
- Added extra resolve options

## [1.0.3]
This release contains Apache Ivy 2.3.0-RC2.

## [1.0.2]
- Upgraded internal Apache Ivy to 2.3.0-rc1 (including dependencies)
- Fixed issue when loading properties files containing cyclic properties

## [1.0.1]
Bugfix: it was not possible to use the default Ivy settings

## [1.0]
- Added support for 'mar' artifact types (Axis module archives)
- Fixed compatibility issues with IntelliJ 11
- Fixed problem on Windows when the case of the ivy-cache path didn't match the case on disk

## [0.9]
- Upgraded internal Apache Ivy to 2.2.0 (including dependencies)
- Support for using ${} style properties in ivy and ivysettings files
- Improved lookup method for artifacts; now useOrigin="true" will also be supported.
- Resolved config names are now listed in the IvyIDEA console
- Resolved library names can now contain the module and or configuration name (with help from wajiii).
- Configurable log level for ivy logging (with help from wajiii)
- Dependencies are now added to the module library with a relative path
- The types used for classes/sources/javadoc artifacts is now configurable
- Several small fixes and improvements

## [0.9-beta7]

## [0.9-beta6]
This release fixes some bugs introduced in beta4.

## [0.9-beta5]

## [0.9-beta4]
- This release adds some new features:
  - The module library created by IvyIDEA can now contain the module and configuration names (based on a patch submitted by wajiii)
  - The raw ivy output can now be logged to the IvyIDEA console for better analysis of problems (config UI based on a patch submitted by wajiii)
  - Dependencies are now added to the module library with relative path names.
  - Several small fixes and improvements.

- Known issue: exception when there are unresolved dependencies in some cases, compatibility with IntelliJ 8 broken - (will be fixed).

## [0.9-beta3]

## [0.9-beta2]
- This release does not introduce much new functionality but should resolve a few annoying issues from 0.9-beta1

## [0.9-beta1]
- This release adds some new features:
  - Upgraded internal ivy to 2.0.0 (including dependencies)
  - Support for using ${} style properties in ivy and ivysettings files
  - Improved lookup method for artifacts; now useOrigin="true" will also be supported
  - Resolved config names are now listed in the IvyIDEA console
  - Several small fixes and improvements

## [0.8]
- Upgraded internal ivy to 2.0.0rc2
- Improved exception handling

## [0.7-alpha]
- IvyIDEA is now compatible with IntelliJ 8.0 (and will run on previous versions as well)
- Switched to JDK 1.5 so the plugin will also run on mac os
- Made looking up intellij module dependencies more lenient; now the revision is ignored when identifying dependencies as existing intellij modules rather than jars

## [0.6-alpha]
- Removed toolwindow logging again as a quickfix - it blocked UI on larger projects.

## [0.5-alpha]
- The ivy configurations that need to be resolved are now configurable. They can be selected in the module configuration.
- Dependencies that are in the module library from a previous resolve, but that are no longer valid are now removed during the resolve process.
- Resolve process is now done in a background task to avoid blocking the UI.
- Added a first simple toolwindow with the logging from the ivy resolve process.

## [0.4-alpha]
- Added option in project settings to allow resolving without validating ivy.xml files

## [0.3-alpha]
- Fixed issues with non-ivy modules mixed with ivy modules in one project
- Added menu item to allow removing all resolved libraries in the project

## [0.2-alpha]
- Fixed an annoying bug causing exceptions when dependencies were evicted

## [0.1-alpha]

## [0.0-alpha-first-functional-codebase]

## [initial-code-commit]

[unreleased]: https://github.com/jbeckers/ivyidea/compare/1.0.14...HEAD
[1.0.14]: https://github.com/jbeckers/ivyidea/compare/1.0.13...1.0.14
[1.0.13]: https://github.com/jbeckers/ivyidea/compare/252deba...1.0.13
[1.0.12]: https://github.com/jbeckers/ivyidea/compare/085bf8b...252deba
[1.0.11]: https://github.com/jbeckers/ivyidea/compare/c0f4541...085bf8b
[1.0.10]: https://github.com/jbeckers/ivyidea/compare/e2a6e89...c0f4541
[1.0.9]: https://github.com/jbeckers/ivyidea/compare/0fa0254...e2a6e89
[1.0.8]: https://github.com/jbeckers/ivyidea/compare/4aeb599b...0fa0254
[1.0.7]: https://github.com/jbeckers/ivyidea/compare/95b912c...4aeb599b
[1.0.6]: https://github.com/jbeckers/ivyidea/compare/2b6c61e...95b912c
[1.0.5]: https://github.com/jbeckers/ivyidea/compare/7db8c19...2b6c61e
[1.0.4]: https://github.com/jbeckers/ivyidea/compare/1.0.3...7db8c19
[1.0.3]: https://github.com/jbeckers/ivyidea/compare/1.0.2...1.0.3
[1.0.2]: https://github.com/jbeckers/ivyidea/compare/1.0.1...1.0.2
[1.0.1]: https://github.com/jbeckers/ivyidea/compare/1.0...1.0.1
[1.0]: https://github.com/jbeckers/ivyidea/compare/480ae92...1.0
[0.9]: https://github.com/jbeckers/ivyidea/compare/495f4a7...480ae92
[0.9-beta7]: https://github.com/jbeckers/ivyidea/compare/0.9-beta6...495f4a7
[0.9-beta6]: https://github.com/jbeckers/ivyidea/compare/3c7d75b...0.9-beta6
[0.9-beta5]: https://github.com/jbeckers/ivyidea/compare/977d92d...3c7d75b
[0.9-beta4]: https://github.com/jbeckers/ivyidea/compare/4fa6b6c...977d92d
[0.9-beta3]: https://github.com/jbeckers/ivyidea/compare/0.9-beta2...4fa6b6c
[0.9-beta2]: https://github.com/jbeckers/ivyidea/compare/0.9-beta1...0.9-beta2
[0.9-beta1]: https://github.com/jbeckers/ivyidea/compare/0.8...0.9-beta1
[0.8]: https://github.com/jbeckers/ivyidea/compare/0.7-alpha...0.8
[0.7-alpha]: https://github.com/jbeckers/ivyidea/compare/0.6-alpha...0.7-alpha
[0.6-alpha]: https://github.com/jbeckers/ivyidea/compare/0.5-alpha...0.6-alpha
[0.5-alpha]: https://github.com/jbeckers/ivyidea/compare/0.4-alpha...0.5-alpha
[0.4-alpha]: https://github.com/jbeckers/ivyidea/compare/0.3-alpha...0.4-alpha
[0.3-alpha]: https://github.com/jbeckers/ivyidea/compare/0.2-alpha...0.3-alpha
[0.2-alpha]: https://github.com/jbeckers/ivyidea/compare/0.1-alpha...0.2-alpha
[0.1-alpha]: https://github.com/jbeckers/ivyidea/compare/0.0-alpha-first-functional-codebase...0.1-alpha
[0.0-alpha-first-functional-codebase]: https://github.com/jbeckers/ivyidea/compare/3d3b378...e6f90e5
[initial-code-commit]: https://github.com/jbeckers/ivyidea/commit/3d3b378a7f53cf31291e7b81560a3e4c18132c37