package com.github.hervian.gradle.plugins

import org.openapitools.openapidiff.core.model.DiffResult

class OpenApiDiffException(diffResult: DiffResult, acceptedDiffLevel: SemVerDif) :
    RuntimeException(
        "The openapi documents contains diffs of a higher level than the configured maximum allowed.\n" +
            "acceptedDiffLevel=$acceptedDiffLevel\n" +
            "DiffResult from the openapi-diff tool: ${diffResult.name}",
    )
