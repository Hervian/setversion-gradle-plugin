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
            it.tag.set(extension.tag)
            it.message.set(extension.message)
            it.outputFile.set(extension.outputFile)
        }
    }
}
