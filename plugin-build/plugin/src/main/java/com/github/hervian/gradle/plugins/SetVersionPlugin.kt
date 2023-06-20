package com.github.hervian.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "setApiVersionExtension"
const val TASK_NAME = "setApiVersion"

abstract class SetVersionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Add the 'template' extension object
        val extension = project.extensions.create(EXTENSION_NAME, SetApiVersionExtension::class.java, project)

        // Add a task that uses configuration from the extension object
        project.tasks.register(TASK_NAME, SetApiVersionTask::class.java) {
            it.oldApi.set(extension.oldApi)
            it.newApi.set(extension.newApi)
            it.versionSuffix.set(extension.versionSuffix)
            it.acceptableDiffLevel.set(extension.acceptableDiffLevel)
            it.versionFile.set(extension.versionFile)
        }
    }
}
