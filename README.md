# kotlin-gradle-plugin-template 🐘

A Gradle plugin to upgrade the gradle version based on diffs between your project's current openapi.json spec and
whatever is in test/QA/prod etc.

That is: The plugin takes the old openapi.json and the new openapi.json as input.
Intended usage is to add a call the setApiVersion task in your pipeline such as to get automatic versioning for
your openapi documented REST app.

Be aware that this plugin assumes that you produce your openapi document in a way where the version number is copied
from the gradle project's version. That is, the setApiVersion task updates the version number defined in the
gradle.proerties file.

Available tasks:
* nextApiVersion: outputs the inferred next api version
* setApiVersion: like nextApiVersion but with the added feature that the version number in the gradle.properties is updated.

## How to use 👣

`gradlew setApiVersion`
Be aware that the task needs 2 files: the "old" api and the new api.

How to get the "old" api depends on your setup. If your build produces a binary that incldues the openapi doc you can
probably download the binary using maven or gradle commands and get the doc from the artifact.
If your openapi doc is only available runtime you can make a curl GET request to download the document.
The same kind of goes for the new api:
If you are using springdoc that works by runtime scanning you must add a plugin like the openapi-gradle-plugin
which build time starts up a server and then downloads the openapi doc such as to make it available for downstream
tooling like present plugin.

Please be aware that the plugin's task `setApiversion` updates the version number assumed to be in the
gradle.properties file. I.e. that goal will only work if the version number is defined in the gradle.properties file
and not in the build.gradle.kts file. Also, since the version number gets updates you could consider git commiting and
pushing the changed source code. Alternatively your build script just produces a binary/docker image from the modified
code and don't care about the version number change being added to source version control. This is no problem for the
functioning of this plugin as it focuses on the version numbers in the openapi docs.

`nextApiVersion` task: See this Q&A for how to access the returned version number: https://stackoverflow.com/questions/59024354/custom-gradle-plugin-access-return-value-from-method

## Features 🎨


## Composite Build 📦

This template is using a [Gradle composite build](https://docs.gradle.org/current/userguide/composite_builds.html) to build, test and publish the plugin. This means that you don't need to run Gradle twice to test the changes on your Gradle plugin (no more `publishToMavenLocal` tricks or so).

The included build is inside the [plugin-build](plugin-build) folder.

### `preMerge` task

A `preMerge` task on the top level build is already provided in the template. This allows you to run all the `check` tasks both in the top level and in the included build.

You can easily invoke it with:

```
./gradlew preMerge
```

If you need to invoke a task inside the included build with:

```
./gradlew -p plugin-build <task-name>
```


### Dependency substitution

Please note that the project relies on module name/group in order for [dependency substitution](https://docs.gradle.org/current/userguide/resolution_rules.html#sec:dependency_substitution_rules) to work properly. If you change only the plugin ID everything will work as expected. If you change module name/group, things might break and you probably have to specify a [substitution rule](https://docs.gradle.org/current/userguide/resolution_rules.html#sub:project_to_module_substitution).


## Publishing 🚀

This template is ready to let you publish to [Gradle Portal](https://plugins.gradle.org/).

The [![Publish Plugin to Portal](https://github.com/cortinico/kotlin-gradle-plugin-template/workflows/Publish%20Plugin%20to%20Portal/badge.svg?branch=1.0.0)](https://github.com/cortinico/kotlin-gradle-plugin-template/actions?query=workflow%3A%22Publish+Plugin+to+Portal%22) Github Action will take care of the publishing whenever you **push a tag**.

Please note that you need to configure two secrets: `GRADLE_PUBLISH_KEY` and `GRADLE_PUBLISH_SECRET` with the credetials you can get from your profile on the Gradle Portal.

## 100% Kotlin 🅺

This template is designed to use Kotlin everywhere. The build files are written using [**Gradle Kotlin DSL**](https://docs.gradle.org/current/userguide/kotlin_dsl.html) as well as the [Plugin DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block) to setup the build.

Dependencies are centralized inside the [libs.versions.toml](gradle/libs.versions.toml).

Moreover, a minimalistic Gradle Plugin is already provided in Kotlin to let you easily start developing your own around it.

## Static Analysis 🔍

This template is using [**ktlint**](https://github.com/pinterest/ktlint) with the [ktlint-gradle](https://github.com/jlleitschuh/ktlint-gradle) plugin to format your code. To reformat all the source code as well as the buildscript you can run the `ktlintFormat` gradle task.

This template is also using [**detekt**](https://github.com/arturbosch/detekt) to analyze the source code, with the configuration that is stored in the [detekt.yml](config/detekt/detekt.yml) file (the file has been generated with the `detektGenerateConfig` task).

## CI ⚙️

This template is using [**GitHub Actions**](https://github.com/cortinico/kotlin-android-template/actions) as CI. You don't need to setup any external service and you should have a running CI once you start using this template.

There are currently the following workflows available:
- [Validate Gradle Wrapper](.github/workflows/gradle-wrapper-validation.yml) - Will check that the gradle wrapper has a valid checksum
- [Pre Merge Checks](.github/workflows/pre-merge.yaml) - Will run the `preMerge` tasks as well as trying to run the Gradle plugin.
- [Publish to Plugin Portal](.github/workflows/publish-plugin.yaml) - Will run the `publishPlugin` task when pushing a new tag.

## Contributing 🤝

Feel free to open a issue or submit a pull request for any bugs/improvements.

## License 📄

This template is licensed under the MIT License - see the [License](License) file for details.
Please note that the generated template is offering to start with a MIT license but you can change it to whatever you wish, as long as you attribute under the MIT terms that you're using the template.
