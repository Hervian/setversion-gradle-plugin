import com.github.hervian.gradle.plugins.SemVerDif

plugins {
    java
    id("com.github.hervian.gradle.serversion.plugin")
}

setApiVersionExtension {
    oldApi.set(file("${project.projectDir}/src/main/resources/petstore.json"))
    newApi.set(file("${project.projectDir}/src/main/resources/petstoreWithMinorChange.json"))
    acceptableDiffLevel.set(SemVerDif.MINOR)
    /*versionSuffix.set(org.gradle.internal.impldep.org.joda.time.format.ISODateTimeFormat.basicDateTimeNoMillis().print(
        Instant.now()))*/
}
