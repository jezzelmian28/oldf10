package io.gitlab.arturbosch.detekt.report

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File

private const val TAB = "\t"

internal class XmlOutputMergerSpec : Spek({

    describe("classpath changes") {

        it("passes for same files") {
            val file1 = File.createTempFile("detekt1", "xml").apply {
                writeText("""
                    <?xml version="1.0" encoding="utf-8"?>
                    <checkstyle version="4.3">
                    <file name="Sample1.kt">
                    $TAB<error line="1" column="1" severity="warning" message="TestMessage" source="detekt.id_a" />
                    </file>
                    </checkstyle>
                """.trimIndent())
            }
            val file2 = File.createTempFile("detekt2", "xml").apply {
                writeText("""
                    <?xml version="1.0" encoding="utf-8"?>
                    <checkstyle version="4.3">
                    <file name="Sample2.kt">
                    $TAB<error line="1" column="1" severity="warning" message="TestMessage" source="detekt.id_b" />
                    </file>
                    </checkstyle>
                """.trimIndent())
            }
            val output = File.createTempFile("output", "xml")
            XmlOutputMerger.merge(setOf(file1, file2), output)

            val text = output.readText()
            val expectedText = """
                <?xml version="1.0" encoding="utf-8"?>
                <checkstyle version="4.3">
                <file name="Sample1.kt">
                $TAB<error line="1" column="1" severity="warning" message="TestMessage" source="detekt.id_a" />
                </file>
                <file name="Sample2.kt">
                $TAB<error line="1" column="1" severity="warning" message="TestMessage" source="detekt.id_b" />
                </file>
                </checkstyle>
            """.trimIndent() + System.lineSeparator()
            assertThat(text.length).isEqualTo(expectedText.length)
            assertThat(text).isEqualTo(expectedText)
        }
    }
})
