package com.github.hervian.gradle.plugins

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.nextMajor
import io.github.z4kn4fein.semver.nextMinor
import io.github.z4kn4fein.semver.nextPatch
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.openapitools.openapidiff.core.OpenApiCompare
import org.openapitools.openapidiff.core.model.DiffResult
import java.io.File

abstract class SetApiVersionTask : DefaultTask() {

   /* init {
        description = "Just a sample template task"

        // Don't forget to set the group here.
        // group = BasePlugin.BUILD_GROUP
    }*/

    @get:InputFile
    @get:Option(
        option = "oldApi",
        description = "The old openapi document, i.e. typically the one that is in test or production",
    )
    abstract val oldApi: RegularFileProperty // = project.objects.fileProperty()

    @get:InputFile
    @get:Option(
        option = "newApi",
        description = "The new openapi document, i.e. typically the one that contain your changes",
    )
    abstract val newApi: RegularFileProperty // = project.objects.fileProperty()

    @get:Input
    @get:Option(
        option = "versionSuffix",
        description = "A string that is suffixed to the inferred version number. Typically a timestamp or build number",
    )
    abstract val versionSuffix: Property<String> // by lazy { project.objects.property(String::class.java).convention("") }

    @get:Input
    @get:Option(
        option = "modus",
        description = "Which level of API change should be allowed, " +
            "i.e. result in a version upgrade, and which change should produce an OpenApiDiffException",
    )
    abstract val acceptableDiffLevel: Property<SemVerDif> // = project.objects.property(Modus::class.java)

    @get:OutputFile
    abstract val versionFile: RegularFileProperty

    @TaskAction
    fun nextApiVersion(): String {
        return calculateNextVersion()
    }

    fun calculateNextVersion(): String {
        validateVersionSuffix(versionSuffix)

        val oldApiFile = oldApi.get().asFile
        val newApiFile = newApi.get().asFile

        logger.lifecycle("oldApi is: ${oldApiFile.absolutePath}")
        logger.lifecycle("newApi is: ${newApiFile.absolutePath}")

        val oldVersion = project.version.toString()

        val newVersion = inferVersionByInspectingTheApisDiff(oldApiFile, newApiFile, versionSuffix.get(), acceptableDiffLevel.get())

        if (!versionFile.get().asFile.exists()) {
            versionFile.get().asFile.createNewFile()
        }
        versionFile.get().asFile.writeText("$newVersion")
        return newVersion
    }

    @TaskAction
    fun setVersion() {
        val newVersion = calculateNextVersion()
        updateVersion(newVersion)
    }

    private fun inferVersionByInspectingTheApisDiff(oldApiFile: File, newApiFile: File, versionSuffix: String, acceptedDiffLevel: SemVerDif): String {
        val diff = OpenApiCompare.fromFiles(oldApiFile, newApiFile)

        var indexOfFirstcharFromSemverBuildPart = diff.oldSpecOpenApi.info.version.indexOfFirst {
            !it.isDigit() && it != '.'
        }
        val oldApiVersionWithBuildInfoPartRemoved = if (indexOfFirstcharFromSemverBuildPart > 0) {
            diff.oldSpecOpenApi.info.version.subSequence(
                0,
                indexOfFirstcharFromSemverBuildPart,
            )
        } else {
            diff.oldSpecOpenApi.info.version
        }

        indexOfFirstcharFromSemverBuildPart = diff.oldSpecOpenApi.info.version.indexOfFirst {
            !it.isDigit() && it != '.'
        }
        val newApiVersionWithBuildInfoPartRemoved = if (indexOfFirstcharFromSemverBuildPart > 0) {
            diff.newSpecOpenApi.info.version.subSequence(
                0,
                indexOfFirstcharFromSemverBuildPart,
            )
        } else {
            diff.newSpecOpenApi.info.version
        }

        val oldApiVersion = Version.parse(oldApiVersionWithBuildInfoPartRemoved.toString())
        val newApiVersion = Version.parse(newApiVersionWithBuildInfoPartRemoved.toString())

        if (oldApiVersion.compareTo(newApiVersion) == 1) {
            throw RuntimeException(
                "The old openapi document specifies a version that is higher than the new openapi" +
                    "specification's version. This is not supported.",
            )
        }
        val versionDiff: SemVerDif = getVersionDiff(oldApiVersion, newApiVersion)

        val diffResult = diff.isChanged
        if (diffResult.weight > acceptedDiffLevel.weight && versionDiff.weight < diffResult.weight) {
            // Example: user has configured that unflagged breaking changes should cause an exception
            // That is, the plugin is configured with fx ChangeLevel.MINOR and the openapi doc's version
            // has not had it's major upgraded.
            throw OpenApiDiffException(diffResult, acceptedDiffLevel)
        }

        println("diffResult" + diffResult)
        println("diffResult.isDifferent" + diffResult.isDifferent)
        System.out.flush()

        var newVersion: Version
        when (diffResult) {
            DiffResult.NO_CHANGES -> newVersion = Version(newApiVersion.major, newApiVersion.minor, newApiVersion.patch) // newApiVersion.buildMetadata.nextPreRelease(versionSuffix)
            DiffResult.METADATA -> newVersion = Version(newApiVersion.major, newApiVersion.minor, newApiVersion.patch).nextPatch()
            DiffResult.COMPATIBLE -> newVersion = newApiVersion.nextMinor()
            DiffResult.UNKNOWN -> throw UnsupportedOperationException(
                "The openapi diff tool invoked by this plugin was " +
                    "unable to detect the type of diff",
            )
            DiffResult.INCOMPATIBLE -> newVersion = newApiVersion.nextMajor()
        }
        logger.lifecycle("openapi-diff result = $diffResult")
        val newVersionAsStringWithSuffix = newVersion.toString() + versionSuffix
        logger.lifecycle("old version number is: ${diff.oldSpecOpenApi.info.version}")
        logger.lifecycle("new version number is: $newVersionAsStringWithSuffix")
        return newVersionAsStringWithSuffix
    }

    private fun getVersionDiff(oldApiVersion: Version, newApiVersion: Version): SemVerDif {
        if (newApiVersion.major > oldApiVersion.major) {
            return SemVerDif.MAJOR
        }
        if (newApiVersion.minor > oldApiVersion.minor) {
            return SemVerDif.MINOR
        }
        if (newApiVersion.patch > oldApiVersion.patch) {
            return SemVerDif.PATCH
        }
        return SemVerDif.NONE
    }

    /*fun getChangeLevel(changedOperation: ChangedOperation): ChangeLevel {
        return ChangeLevel.NONE
    }*/

    /**
     * https://semver.org/
     */
    private fun validateVersionSuffix(versionSuffix: Property<String>) {
        assert(versionSuffix.get().startsWith("-") || versionSuffix.get().startsWith("+"))
    }

    private fun updateVersion(version: String) {
        if (hasProperty("version")) {
            setProperty("version", version)
        }
    }
}
