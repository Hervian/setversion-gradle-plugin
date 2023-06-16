package com.github.hervian.gradle.plugins

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class SetApiVersionPluginTest {

    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.github.hervian.gradle.serversion.plugin")

        assert(project.tasks.getByName("setApiVersion") is SetApiVersionTask)
    }

    @Test
    fun `extension setApiVersionConfig is created correctly`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.github.hervian.gradle.serversion.plugin")

        assertNotNull(project.extensions.getByName("setApiVersionExtension"))
    }

    @Test
    fun `parameters are passed correctly from extension to task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.github.hervian.gradle.serversion.plugin")
        val aFile = File(project.projectDir, ".tmp")
        (project.extensions.getByName("setApiVersionExtension") as SetApiVersionExtension).apply {
            tag.set("a-sample-tag")
            message.set("just-a-message")
            outputFile.set(aFile)
        }

        val task = project.tasks.getByName("setApiVersion") as SetApiVersionTask

        assertEquals("a-sample-tag", task.tag.get())
        assertEquals("just-a-message", task.message.get())
        assertEquals(aFile, task.outputFile.get().asFile)
    }
}
