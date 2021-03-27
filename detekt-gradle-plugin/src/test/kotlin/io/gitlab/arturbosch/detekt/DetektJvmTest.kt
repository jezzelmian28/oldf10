package io.gitlab.arturbosch.detekt

import io.gitlab.arturbosch.detekt.testkit.DslGradleRunner
import io.gitlab.arturbosch.detekt.testkit.ProjectLayout
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object DetektJvmTest : Spek({
    describe("When applying detekt in a JVM project") {

        it("configures detekt type resolution task") {
            val gradleRunner = DslGradleRunner(
                projectLayout = ProjectLayout(1),
                buildFileName = "build.gradle",
                mainBuildFileContent = """
                    plugins {
                        id "org.jetbrains.kotlin.jvm"
                        id "io.gitlab.arturbosch.detekt"
                    }

                    repositories {
                        mavenCentral()
                        jcenter()
                        mavenLocal()
                    }

                    detekt {
                        reports {
                            sarif.enabled = true
                            txt.enabled = false
                        }
                    }
                """.trimIndent(),
                dryRun = true
            )
            gradleRunner.setupProject()
            gradleRunner.runTasksAndCheckResult(":detektMain") { buildResult ->
                assertThat(buildResult.output).contains("--report xml:")
                assertThat(buildResult.output).contains("--report sarif:")
                assertThat(buildResult.output).doesNotContain("--report txt:")
            }
        }
    }
})
