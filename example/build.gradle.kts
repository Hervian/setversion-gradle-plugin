import com.github.hervian.gradle.plugins.Modus

plugins {
    java
    id("com.github.hervian.gradle.serversion.plugin")
}

setApiVersionExtension {
    oldApi.set(file("${project.projectDir}/src/main/resources/petstore.json"))
    newApi.set(file("${project.projectDir}/src/main/resources/petstoreWithMinorChange.json"))
    modus.set(Modus.ALLOW_MINOR)
    /*versionSuffix.set(org.gradle.internal.impldep.org.joda.time.format.ISODateTimeFormat.basicDateTimeNoMillis().print(
        Instant.now()))*/
}
