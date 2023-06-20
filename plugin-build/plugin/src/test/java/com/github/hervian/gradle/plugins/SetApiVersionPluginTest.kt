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
        project.pluginManager.apply("com.github.hervian.rip-version-gradle-plugin")

        assert(project.tasks.getByName("setApiVersion") is SetApiVersionTask)
    }

    @Test
    fun `extension setApiVersionConfig is created correctly`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.github.hervian.rip-version-gradle-plugin")

        assertNotNull(project.extensions.getByName("setApiVersionExtension"))
    }

    @Test
    fun `parameters are passed correctly from extension to task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.github.hervian.rip-version-gradle-plugin")
        val aFile = File(project.projectDir, ".tmp")

        val classLoader = javaClass.classLoader
        val oldApiAsFile = File(classLoader.getResource("petstore.json").file)
        val newApiAsFile = File(classLoader.getResource("petstoreWithMinorChange.json").file)

       /* val oldApiAsFile = File("${project.projectDir}/src/main/resources/petstore.json")
        val newApiAsFile = File("${project.projectDir}/src/main/resources/petstoreWithMinorChange.json")*/
        val t = (project.extensions.getByName("setApiVersionExtension") as SetApiVersionExtension).apply {
            oldApi.set(oldApiAsFile)
            newApi.set(newApiAsFile)
            acceptableDiffLevel.set(SemVerDif.MINOR)
            versionFile.set(aFile)
        }

        val task = project.tasks.getByName("setApiVersion") as SetApiVersionTask

        assertEquals(oldApiAsFile, task.oldApi.get().asFile)
        assertEquals(newApiAsFile, task.newApi.get().asFile)
        assertEquals(aFile, task.versionFile.get().asFile)
        assertEquals(SemVerDif.MINOR, task.acceptableDiffLevel.get())
    }

    @Test
    fun `nextApiVersion() on api with no change, acceptableChangeLevel = MAJOR`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstore.json").file)
        val suffix = "+18"
        val nextVersion = getNextVersion(newApiAsFile, SemVerDif.MAJOR, suffix)

        assertEquals("1.0.6$suffix", nextVersion)
    }

    @Test
    fun `nextApiVersion() on api with no change, acceptableChangeLevel = MINOR`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstore.json").file)
        val suffix = "+18"
        val nextVersion = getNextVersion(newApiAsFile, SemVerDif.MINOR, suffix)

        assertEquals("1.0.6$suffix", nextVersion)
    }

    @Test
    fun `nextApiVersion() on api with no change, acceptableChangeLevel = PATCH`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstore.json").file)
        val suffix = "+18"
        val nextVersion = getNextVersion(newApiAsFile, SemVerDif.PATCH, suffix)

        assertEquals("1.0.6$suffix", nextVersion)
    }

    @Test
    fun `nextApiVersion() on api with no change, acceptableChangeLevel = NONE`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstore.json").file)
        val suffix = "+18"
        val nextVersion = getNextVersion(newApiAsFile, SemVerDif.NONE, suffix)

        assertEquals("1.0.6$suffix", nextVersion)
    }

    @Test
    fun `nextApiVersion() on api with patch change, acceptableChangeLevel = MAJOR`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstoreWithPatchChange.json").file)
        val suffix = "+18"
        val nextVersion = getNextVersion(newApiAsFile, SemVerDif.MAJOR, suffix)

        assertEquals("1.0.7$suffix", nextVersion)
    }

    @Test
    fun `nextApiVersion() on api with patch change, acceptableChangeLevel = MINOR`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstoreWithPatchChange.json").file)
        val suffix = "+18"
        val nextVersion = getNextVersion(newApiAsFile, SemVerDif.MINOR, suffix)

        assertEquals("1.0.7$suffix", nextVersion)
    }

    @Test
    fun `nextApiVersion() on api with patch change, acceptableChangeLevel = PATCH`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstoreWithPatchChange.json").file)
        val suffix = "+18"
        val nextVersion = getNextVersion(newApiAsFile, SemVerDif.PATCH, suffix)

        assertEquals("1.0.7$suffix", nextVersion)
    }

    @Test(expected = OpenApiDiffException::class)
    fun `nextApiVersion() on api with patch change, acceptableChangeLevel = NONE`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstoreWithPatchChange.json").file)
        val suffix = "+18"
        getNextVersion(newApiAsFile, SemVerDif.NONE, suffix)

        assert(false)
    }

    @Test
    fun `nextApiVersion() on api with minor change, acceptableChangeLevel = MAJOR`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstoreWithMinorChange.json").file)

        val nextVersion = getNextVersion(newApiAsFile, SemVerDif.MAJOR)

        assert(nextVersion.startsWith("1.1.0"))
    }

    @Test
    fun `nextApiVersion() on api with minor change, acceptableChangeLevel = MINOR`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstoreWithMinorChange.json").file)
        val nextVersion = getNextVersion(newApiAsFile, SemVerDif.MINOR)

        assert(nextVersion.startsWith("1.1.0"))
    }

    @Test(expected = OpenApiDiffException::class)
    fun `nextApiVersion() on api with minor change, acceptableChangeLevel = PATCH`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstoreWithMinorChange.json").file)

        getNextVersion(newApiAsFile, SemVerDif.PATCH)

        assert(false)
    }

    @Test(expected = OpenApiDiffException::class)
    fun `nextApiVersion() on api with minor change, acceptableChangeLevel = NONE`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstoreWithMinorChange.json").file)

        getNextVersion(newApiAsFile, SemVerDif.NONE)

        assert(false)
    }

    @Test
    fun `nextApiVersion() on api with major change, acceptableChangeLevel = MAJOR`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstoreWithMajorChange.json").file)

        val nextVersion = getNextVersion(newApiAsFile, SemVerDif.MAJOR)

        assert(nextVersion.startsWith("2.0.0"))
    }

    @Test(expected = OpenApiDiffException::class)
    fun `nextApiVersion() on api with major change, acceptableChangeLevel = MINOR`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstoreWithMajorChange.json").file)
        getNextVersion(newApiAsFile, SemVerDif.MINOR)

        assert(false)
    }

    @Test(expected = OpenApiDiffException::class)
    fun `nextApiVersion() on api with major change, acceptableChangeLevel = PATCH`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstoreWithMajorChange.json").file)

        getNextVersion(newApiAsFile, SemVerDif.PATCH)

        assert(false)
    }

    @Test(expected = OpenApiDiffException::class)
    fun `nextApiVersion() on api with major change, acceptableChangeLevel = NONE`() {
        val classLoader = javaClass.classLoader
        val newApiAsFile = File(classLoader.getResource("petstoreWithMajorChange.json").file)

        getNextVersion(newApiAsFile, SemVerDif.NONE)

        assert(false)
    }

    private fun getNextVersion(newApiFile: File, level: SemVerDif): String {
        return getNextVersion(newApiFile, level, null)
    }

    private fun getNextVersion(newApiFile: File, level: SemVerDif, suffix: String?): String {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.github.hervian.rip-version-gradle-plugin")
        val aFile = File.createTempFile("version", ".txt")
        val classLoader = javaClass.classLoader
        val oldApiAsFile = File(classLoader.getResource("petstore.json").file)

        assert(oldApiAsFile.exists())
        assert(newApiFile.exists())

        (project.extensions.getByName("setApiVersionExtension") as SetApiVersionExtension).apply {
            oldApi.set(oldApiAsFile)
            newApi.set(newApiFile)
            acceptableDiffLevel.set(level)
            versionFile.set(aFile)
            if (suffix != null) {
                versionSuffix.set(suffix)
            }
        }

        val task = project.tasks.getByName("setApiVersion") as SetApiVersionTask

        return task.nextApiVersion()
    }
}
