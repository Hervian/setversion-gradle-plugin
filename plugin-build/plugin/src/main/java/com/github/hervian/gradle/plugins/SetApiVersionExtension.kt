package com.github.hervian.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Optional
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
/*import org.gradle.internal.impldep.org.joda.time.Instant
import org.gradle.internal.impldep.org.joda.time.format.ISODateTimeFormat*/
import javax.inject.Inject

const val DEFAULT_OUTPUT_FILE = "version.txt"
const val DEFAULT_NEW_API_FILE = "openapi.json"
const val DEFAULT_OLD_API_FILE = "/old/openapi.json"

@Suppress("UnnecessaryAbstractClass")
abstract class SetApiVersionExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    var acceptableDiffLevel: Property<SemVerDif> = objects.property(SemVerDif::class.java).convention(SemVerDif.MINOR)

    var oldApi: RegularFileProperty = objects.fileProperty().convention(
        project.layout.buildDirectory.file(DEFAULT_OLD_API_FILE),
    )

    var newApi: RegularFileProperty = objects.fileProperty().convention(
        project.layout.buildDirectory.file(DEFAULT_NEW_API_FILE),
    )

    @Optional
    val versionSuffix: Property<String> = objects.property(String::class.java).convention(
        "+" + DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss").withZone(ZoneId.systemDefault()).format(Instant.now()),
    )

    val versionFile: RegularFileProperty = objects.fileProperty().convention(
        project.layout.buildDirectory.file(DEFAULT_OUTPUT_FILE),
    )
}
