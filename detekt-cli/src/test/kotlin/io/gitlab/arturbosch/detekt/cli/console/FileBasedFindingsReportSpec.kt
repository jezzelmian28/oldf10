package io.gitlab.arturbosch.detekt.cli.console

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.cli.createFinding
import io.gitlab.arturbosch.detekt.test.TestDetektion
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class FileBasedFindingsReportSpec : Spek({

    val subject by memoized { createFileBasedFindingsReport() }

    describe("findings report") {

        context("reports the debt per file and rule set with the overall debt") {
            val expectedContent = readResource("grouped-findings-report.txt")
            val detektion = object : TestDetektion() {
                override val findings: Map<String, List<Finding>> = mapOf(
                    Pair(
                        "Ruleset1",
                        listOf(
                            createFinding(fileName = "File1.kt"),
                            createFinding(fileName = "File2.kt")
                        )
                    ),
                    Pair(
                        "Ruleset2",
                        listOf(createFinding(fileName = "File1.kt"))
                    )
                )
            }

            it("has the reference content") {
                val output = subject.render(detektion)?.trimEnd()?.decolorized()
                assertThat(output).isEqualTo(expectedContent)
            }
        }

        it("reports no findings") {
            val detektion = TestDetektion()
            assertThat(subject.render(detektion)).isNull()
        }

        it("reports no findings when no rule set contains smells") {
            val detektion = object : TestDetektion() {
                override val findings: Map<String, List<Finding>> = mapOf(
                    Pair("EmptySmells", emptyList())
                )
            }
            assertThat(subject.render(detektion)).isNull()
        }

        it("should not add auto corrected issues to report") {
            val report = FileBasedFindingsReport()
            AutoCorrectableIssueAssert.isReportNull(report)
        }
    }
})

private fun createFileBasedFindingsReport(): FileBasedFindingsReport {
    val report = FileBasedFindingsReport()
    report.init(Config.empty)
    return report
}
