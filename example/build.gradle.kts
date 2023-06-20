import com.github.hervian.gradle.plugins.SemVerDif

plugins {
    java
    id("com.github.hervian.rip-version-gradle-plugin")
}

setApiVersionExtension {
    oldApi.set(file("${project.projectDir}/src/main/resources/petstore.json"))
    newApi.set(file("${project.projectDir}/src/main/resources/petstoreWithMinorChange.json"))
    acceptableDiffLevel.set(SemVerDif.MINOR)
}
