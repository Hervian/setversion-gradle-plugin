package com.github.hervian.gradle.plugins

enum class SemVerDif(val weight: Int) {

    NONE(0),
    PATCH(1),
    MINOR(2),
    MAJOR(4),
}
