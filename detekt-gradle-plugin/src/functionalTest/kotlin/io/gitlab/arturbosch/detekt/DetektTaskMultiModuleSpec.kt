package io.gitlab.arturbosch.detekt

import io.gitlab.arturbosch.detekt.testkit.DslGradleRunner
import io.gitlab.arturbosch.detekt.testkit.DslTestBuilder
import io.gitlab.arturbosch.detekt.testkit.ProjectLayout
import io.gitlab.arturbosch.detekt.testkit.reIndent
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class DetektTaskMultiModuleSpec {

    @Test
    @DisplayName(
        "it is applied with defaults to all subprojects individually without " +
            "sources in root project using the subprojects block"
    )
    fun applyToSubprojectsWithoutSources() {
        val projectLayout = ProjectLayout(0).apply {
            addSubmodule("child1", 2)
            addSubmodule("child2", 4)
        }

        val builder = DslTestBuilder.kotlin()

        val mainBuildFileContent: String = """
            ${builder.gradlePlugins.reIndent()}
            allprojects {
                ${builder.gradleRepositories.reIndent(1)}
            }
            subprojects {
                ${builder.gradleSubprojectsApplyPlugins.reIndent(1)}
            }
        """.trimIndent()

        val gradleRunner = DslGradleRunner(projectLayout, builder.gradleBuildName, mainBuildFileContent)

        gradleRunner.setupProject()
        gradleRunner.runDetektTaskAndCheckResult { result ->
            projectLayout.submodules.forEach { submodule ->
                assertThat(result.task(":${submodule.name}:detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            }

            assertThat(projectFile("build/reports/detekt/mainSourceSet.xml")).doesNotExist()
            assertThat(projectFile("build/reports/detekt/mainSourceSet.html")).doesNotExist()
            assertThat(projectFile("build/reports/detekt/mainSourceSet.txt")).doesNotExist()
            projectLayout.submodules.forEach {
                assertThat(projectFile("${it.name}/build/reports/detekt/mainSourceSet.xml")).exists()
                assertThat(projectFile("${it.name}/build/reports/detekt/mainSourceSet.html")).exists()
                assertThat(projectFile("${it.name}/build/reports/detekt/mainSourceSet.txt")).exists()
            }
        }
    }

    @Test
    @DisplayName(
        "it is applied with defaults to main project and subprojects " +
            "individually using the allprojects block"
    )
    fun applyWithAllprojectsBlock() {
        val projectLayout = ProjectLayout(1).apply {
            addSubmodule("child1", 2)
            addSubmodule("child2", 4)
        }

        val builder = DslTestBuilder.kotlin()

        val mainBuildFileContent: String = """
            ${builder.gradlePlugins.reIndent()}
            
            allprojects {
                ${builder.gradleRepositories.reIndent(1)}
                ${builder.gradleSubprojectsApplyPlugins.reIndent(1)}
            }
        """.trimIndent()

        val gradleRunner = DslGradleRunner(projectLayout, builder.gradleBuildName, mainBuildFileContent)

        gradleRunner.setupProject()
        gradleRunner.runDetektTaskAndCheckResult { result ->
            assertThat(result.task(":detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            projectLayout.submodules.forEach { submodule ->
                assertThat(result.task(":${submodule.name}:detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            }

            assertThat(projectFile("build/reports/detekt/mainSourceSet.xml")).exists()
            assertThat(projectFile("build/reports/detekt/mainSourceSet.html")).exists()
            assertThat(projectFile("build/reports/detekt/mainSourceSet.txt")).exists()
            projectLayout.submodules.forEach {
                assertThat(projectFile("${it.name}/build/reports/detekt/mainSourceSet.xml")).exists()
                assertThat(projectFile("${it.name}/build/reports/detekt/mainSourceSet.html")).exists()
                assertThat(projectFile("${it.name}/build/reports/detekt/mainSourceSet.txt")).exists()
            }
        }
    }

    @Test
    fun `it uses custom configs when configured in allprojects block`() {
        val projectLayout = ProjectLayout(1).apply {
            addSubmodule("child1", 2)
            addSubmodule("child2", 4)
        }

        val builder = DslTestBuilder.kotlin()

        val mainBuildFileContent: String = """
            ${builder.gradlePlugins.reIndent()}
            
            allprojects {
                ${builder.gradleRepositories.reIndent(1)}
                ${builder.gradleSubprojectsApplyPlugins.reIndent(1)}
            
                detekt {
                    reportsDir = file("build/detekt-reports")
                }
            }
        """.trimIndent()

        val gradleRunner = DslGradleRunner(projectLayout, builder.gradleBuildName, mainBuildFileContent)
        gradleRunner.setupProject()
        gradleRunner.runDetektTaskAndCheckResult { result ->
            assertThat(result.task(":detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            projectLayout.submodules.forEach { submodule ->
                assertThat(result.task(":${submodule.name}:detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            }

            assertThat(projectFile("build/detekt-reports/mainSourceSet.xml")).exists()
            assertThat(projectFile("build/detekt-reports/mainSourceSet.html")).exists()
            assertThat(projectFile("build/detekt-reports/mainSourceSet.txt")).exists()
            projectLayout.submodules.forEach {
                assertThat(projectFile("${it.name}/build/detekt-reports/mainSourceSet.xml")).exists()
                assertThat(projectFile("${it.name}/build/detekt-reports/mainSourceSet.html")).exists()
                assertThat(projectFile("${it.name}/build/detekt-reports/mainSourceSet.txt")).exists()
            }
        }
    }

    @Test
    @DisplayName("it allows changing defaults in allprojects block that can be overwritten in subprojects")
    fun allowsChangingDefaultsInAllProjectsThatAreOverwrittenInSubprojects() {
        val child2DetektConfig = """
            plugins {
                kotlin("jvm")
            }
            detekt {
               reportsDir = file("build/custom")
            }
        """.trimIndent()

        val projectLayout = ProjectLayout(1).apply {
            addSubmodule("child1", 2)
            addSubmodule("child2", 4, buildFileContent = child2DetektConfig)
        }

        val builder = DslTestBuilder.kotlin()

        val mainBuildFileContent: String = """
            ${builder.gradlePlugins.reIndent()}
            
            allprojects {
                ${builder.gradleRepositories.reIndent(1)}
                ${builder.gradleSubprojectsApplyPlugins.reIndent(1)}
            
                detekt {
                    reportsDir = file("build/detekt-reports")
                }
            }
        """.trimIndent()

        val gradleRunner = DslGradleRunner(projectLayout, builder.gradleBuildName, mainBuildFileContent)

        gradleRunner.setupProject()
        gradleRunner.runDetektTaskAndCheckResult { result ->
            assertThat(result.task(":detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            assertThat(projectFile("build/detekt-reports/mainSourceSet.xml")).exists()
            assertThat(projectFile("build/detekt-reports/mainSourceSet.html")).exists()
            assertThat(projectFile("build/detekt-reports/mainSourceSet.txt")).exists()
            assertThat(projectFile("child1/build/detekt-reports/mainSourceSet.xml")).exists()
            assertThat(projectFile("child1/build/detekt-reports/mainSourceSet.html")).exists()
            assertThat(projectFile("child1/build/detekt-reports/mainSourceSet.txt")).exists()
            assertThat(projectFile("child2/build/custom/mainSourceSet.xml")).exists()
            assertThat(projectFile("child2/build/custom/mainSourceSet.html")).exists()
            assertThat(projectFile("child2/build/custom/mainSourceSet.txt")).exists()
        }
    }

    @Test
    fun `it can be applied to all files in entire project resulting in 1 report`() {
        val projectLayout = ProjectLayout(1).apply {
            addSubmodule("child1", 2)
            addSubmodule("child2", 4)
        }

        val gradleRunner = DslTestBuilder.kotlin()
            .withProjectLayout(projectLayout)
            .build()

        gradleRunner.runDetektTaskAndCheckResult { result ->
            assertThat(result.task(":detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            projectLayout.submodules.forEach { submodule ->
                assertThat(result.task(":${submodule.name}:detekt")).isNull()
            }

            assertThat(projectFile("build/reports/detekt/mainSourceSet.xml")).exists()
            assertThat(projectFile("build/reports/detekt/mainSourceSet.html")).exists()
            assertThat(projectFile("build/reports/detekt/mainSourceSet.txt")).exists()
            projectLayout.submodules.forEach { submodule ->
                assertThat(projectFile("${submodule.name}/build/reports/detekt/mainSourceSet.xml")).doesNotExist()
                assertThat(projectFile("${submodule.name}/build/reports/detekt/mainSourceSet.html")).doesNotExist()
                assertThat(projectFile("${submodule.name}/build/reports/detekt/mainSourceSet.txt")).doesNotExist()
            }
        }
    }
}
