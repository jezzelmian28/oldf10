package io.github.detekt.tooling.dsl

import io.github.detekt.tooling.api.spec.ProjectSpec
import java.nio.file.Path

@ProcessingModelDsl
class ProjectSpecBuilder : Builder<ProjectSpec>, ProjectSpec {

    override var reportBasePath: Path? = null
    override var inputPaths: Collection<Path> = emptyList()
    override var excludes: Collection<String> = emptyList()
    override var includes: Collection<String> = emptyList()

    override fun build(): ProjectSpec = ProjectModel(reportBasePath, inputPaths, excludes, includes)
}

internal data class ProjectModel(
    override val reportBasePath: Path?,
    override val inputPaths: Collection<Path>,
    override val excludes: Collection<String>,
    override val includes: Collection<String>
) : ProjectSpec
