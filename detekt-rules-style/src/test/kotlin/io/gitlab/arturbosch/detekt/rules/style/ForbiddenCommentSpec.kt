@file:Suppress("ClassName")

package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

private const val VALUES = "values"
private const val COMMENTS = "comments"
private const val ALLOWED_PATTERNS = "allowedPatterns"
private const val MESSAGE = "customMessage"

class ForbiddenCommentSpec {

    val todoColon = "// TODO: I need to fix this."
    val todo = "// TODO I need to fix this."

    val fixmeColon = "// FIXME: I need to fix this."
    val fixme = "// FIXME I need to fix this."

    val stopShipColon = "// STOPSHIP: I need to fix this."
    val stopShip = "// STOPSHIP I need to fix this."

    @Nested
    inner class `the default values are configured` {

        @Test
        @DisplayName("should report TODO: usages")
        fun reportTodoColon() {
            val findings = ForbiddenComment().compileAndLint(todoColon)
            assertThat(findings).hasSize(1)
            assertThat(findings[0]).hasMessage(
                String.format(ForbiddenComment.DEFAULT_ERROR_MESSAGE, "TODO:", "some changes are pending.")
            )
        }

        @Test
        fun `should not report TODO usages`() {
            val findings = ForbiddenComment().compileAndLint(todo)
            assertThat(findings).isEmpty()
        }

        @Test
        @DisplayName("should report FIXME: usages")
        fun reportFixMe() {
            val findings = ForbiddenComment().compileAndLint(fixmeColon)
            assertThat(findings).hasSize(1)
        }

        @Test
        fun `should not report FIXME usages`() {
            val findings = ForbiddenComment().compileAndLint(fixme)
            assertThat(findings).isEmpty()
        }

        @Test
        @DisplayName("should report STOPSHIP: usages")
        fun reportStopShipColon() {
            val findings = ForbiddenComment().compileAndLint(stopShipColon)
            assertThat(findings).hasSize(1)
            assertThat(findings[0]).hasMessage(
                String.format(
                    ForbiddenComment.DEFAULT_ERROR_MESSAGE,
                    "STOPSHIP:",
                    "some changes are present which needs to be addressed before ship."
                )
            )
        }

        @Test
        fun `should not report STOPSHIP usages`() {
            val findings = ForbiddenComment().compileAndLint(stopShip)
            assertThat(findings).isEmpty()
        }

        @Test
        fun `should report violation in multiline comment`() {
            val code = """
                /*
                 TODO: I need to fix this.
                 */
            """.trimIndent()
            val findings = ForbiddenComment().compileAndLint(code)
            assertThat(findings).hasSize(1)
        }

        @Test
        fun `should report violation in single line block comment`() {
            val code = """
                /*TODO: I need to fix this.*/
            """.trimIndent()
            val findings = ForbiddenComment().compileAndLint(code)
            assertThat(findings).hasSize(1)
        }

        @Test
        fun `should report violation in KDoc`() {
            val code = """
                /**
                 * TODO: I need to fix this.
                 */
                class A {
                    /**
                     * TODO: I need to fix this.
                     */
                }
            """.trimIndent()
            val findings = ForbiddenComment().compileAndLint(code)
            assertThat(findings).hasSize(2)
        }

        @Test
        fun `should report violation in KDoc 1`() {
            @Suppress("MultilineRawStringIndentation")
            val code = """
                /**
                 * TODO: I need to fix this.
* left shifted asterik
                            * right shifted astrik
                 *      with space astrik
                 *with 0 space astrik
                 * with 1 space astrik
                 *  with 2 space astrik
                 *   with 3 space astrik
                 *    with 4 space astrik
                 *     with 5 space astrik
                 *      with 6 space astrik
                 *       with 7 space astrik
                 */
                class A {
                    /**
                     * TODO: I need to fix this.
                     */
                }
            """.trimIndent()
            val findings = ForbiddenComment().compileAndLint(code)
            assertThat(findings).hasSize(2)
        }
    }

    @Nested
    inner class `custom default values are configured` {
        val banana = "// Banana."

        @Nested
        inner class `when given Banana` {
            val config = TestConfig(COMMENTS to listOf("Banana"))

            @Test
            @DisplayName("should not report TODO: usages")
            fun todoColon() {
                val findings = ForbiddenComment(config).compileAndLint(todoColon)
                assertThat(findings).isEmpty()
            }

            @Test
            @DisplayName("should not report FIXME: usages")
            fun fixmeColon() {
                val findings = ForbiddenComment(config).compileAndLint(fixmeColon)
                assertThat(findings).isEmpty()
            }

            @Test
            @DisplayName("should not report STOPME: usages")
            fun stopShipColon() {
                val findings = ForbiddenComment(config).compileAndLint(stopShipColon)
                assertThat(findings).isEmpty()
            }

            @Test
            fun `should report Banana usages`() {
                val findings = ForbiddenComment(config).compileAndLint(banana)
                assertThat(findings).hasSize(1)
            }

            @Test
            fun `should report Banana usages regardless of case sensitive`() {
                val forbiddenComment = ForbiddenComment(TestConfig(VALUES to "bAnAnA"))
                val findings = forbiddenComment.compileAndLint(banana)
                assertThat(findings).hasSize(1)
            }
        }

        @Nested
        @DisplayName("when given listOf(\"banana\")")
        inner class ListOfBanana {
            val config = TestConfig(COMMENTS to listOf("Banana"))

            @Test
            @DisplayName("should not report TODO: usages")
            fun todoColon() {
                val findings = ForbiddenComment(config).compileAndLint(todoColon)
                assertThat(findings).isEmpty()
            }

            @Test
            @DisplayName("should not report FIXME: usages")
            fun fixmeColon() {
                val findings = ForbiddenComment(config).compileAndLint(fixmeColon)
                assertThat(findings).isEmpty()
            }

            @Test
            @DisplayName("should not report STOPME: usages")
            fun stopShipColon() {
                val findings = ForbiddenComment(config).compileAndLint(stopShipColon)
                assertThat(findings).isEmpty()
            }

            @Test
            fun `should report Banana usages`() {
                val findings = ForbiddenComment(config).compileAndLint(banana)
                assertThat(findings).hasSize(1)
            }

            @Test
            fun `should report Banana usages regardless of case sensitive`() {
                val forbiddenComment = ForbiddenComment(TestConfig(VALUES to "bAnAnA"))
                val findings = forbiddenComment.compileAndLint(banana)
                assertThat(findings).hasSize(1)
            }
        }
    }

    @Nested
    inner class `custom default values with allowed patterns are configured` {

        private val patternsConfig = TestConfig(
            VALUES to "Comment",
            ALLOWED_PATTERNS to "Ticket|Task",
        )

        @Test
        fun `should report Comment usages when regex does not match`() {
            val comment = "// Comment is added here."
            val findings = ForbiddenComment(patternsConfig).compileAndLint(comment)
            assertThat(findings).hasSize(1)
        }

        @Test
        fun `should not report Comment usages when any one pattern is present`() {
            val comment = "// Comment Ticket:234."
            val findings = ForbiddenComment(patternsConfig).compileAndLint(comment)
            assertThat(findings).isEmpty()
        }

        @Test
        fun `should not report Comment usages when all patterns are present`() {
            val comment = "// Comment Ticket:123 Task:456 comment."
            val findings = ForbiddenComment(patternsConfig).compileAndLint(comment)
            assertThat(findings).isEmpty()
        }
    }

    @Nested
    inner class `custom message is configured` {
        private val messageConfig = TestConfig(
            VALUES to "Comment",
            MESSAGE to "Custom Message",
        )

        @Test
        fun `should report a Finding with message 'Custom Message'`() {
            val comment = "// Comment"
            val findings = ForbiddenComment(messageConfig).compileAndLint(comment)
            assertThat(findings).hasSize(1)
            assertThat(findings.first().message).isEqualTo("Custom Message")
        }
    }

    @Nested
    inner class `custom message is not configured` {
        private val messageConfig = TestConfig(VALUES to "Comment")

        @Test
        fun `should report a Finding with default Message`() {
            val comment = "// Comment"
            val findings = ForbiddenComment(messageConfig).compileAndLint(comment)
            val expectedMessage = String.format(ForbiddenComment.DEFAULT_ERROR_MESSAGE_WITH_NO_REASON, "Comment")
            assertThat(findings).hasSize(1)
            assertThat(findings.first().message).isEqualTo(expectedMessage)
        }
    }

    @Nested
    inner class `custom value pattern is configured` {
        private val patternStr = """^(?i)REVIEW\b"""
        private val messageConfig = TestConfig(
            COMMENTS to listOf("STOPSHIP", patternStr),
        )

        @Test
        fun `should not report a finding when review doesn't match the pattern`() {
            val comment = "// to express in the preview that it's not a normal TextView."
            val findings = ForbiddenComment(messageConfig).compileAndLint(comment)
            assertThat(findings).isEmpty()
        }

        @Test
        fun `should report a finding when STOPSHIP is present`() {
            val comment = "// STOPSHIP to express in the preview that it's not a normal TextView."
            val findings = ForbiddenComment(messageConfig).compileAndLint(comment)
            assertThat(findings).hasSize(1)
            assertThat(findings[0])
                .hasMessage(String.format(ForbiddenComment.DEFAULT_ERROR_MESSAGE_WITH_NO_REASON, "STOPSHIP"))
        }

        @Test
        fun `should report a finding when review pattern is matched with comment with leading space`() {
            val comment = "// REVIEW foo -> flag"
            val findings = ForbiddenComment(messageConfig).compileAndLint(comment)
            assertThat(findings).hasSize(1)
            assertThat(findings[0])
                .hasMessage(String.format(ForbiddenComment.DEFAULT_ERROR_MESSAGE_WITH_NO_REASON, patternStr))
        }

        @Test
        fun `should report a finding when review pattern is matched with comment with out leading space`() {
            val comment = "//REVIEW foo -> flag"
            val findings = ForbiddenComment(messageConfig).compileAndLint(comment)
            assertThat(findings).hasSize(1)
            assertThat(findings[0])
                .hasMessage(String.format(ForbiddenComment.DEFAULT_ERROR_MESSAGE_WITH_NO_REASON, patternStr))
        }

        @Test
        fun `should report a finding matching two patterns`() {
            val comment = "// REVIEW foo -> flag STOPSHIP"
            val findings = ForbiddenComment(messageConfig).compileAndLint(comment)
            assertThat(findings).hasSize(2)
        }

        @Test
        fun `should report a finding matching a pattern contained in the comment`() {
            val comment = "// foo STOPSHIP bar"
            val findings = ForbiddenComment(messageConfig).compileAndLint(comment)
            assertThat(findings).hasSize(1)
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    inner class `comment getContent` {

        @Suppress("LongMethod", "UnusedPrivateMember")
        private fun getCommentsContentArguments() = listOf(
            Arguments.of("// comment", "comment"),
            Arguments.of("//  comment", " comment"),
            Arguments.of("//comment", "comment"),
            Arguments.of("/* comment */", "comment"),
            Arguments.of("/*  comment  */", " comment "),
            Arguments.of("/*comment*/", "comment"),
            Arguments.of("/*** comment ***/", "** comment **"),
            Arguments.of(
                """
                    /*
                    *bad
                    * good
                    */
                """.trimIndent(),
                """
                    
                    bad
                    good
                    
                """.trimIndent()
            ),
            Arguments.of(
                """
                    /*
                    *bad
                    *   good
                    */
                """.trimIndent(),
                """
                    
                    bad
                      good
                    
                """.trimIndent()
            ),
            Arguments.of(
                """
                    /*
                    comment
                    * a
                    * b
                    * c*/
                """.trimIndent(),
                """
                    
                    comment
                    a
                    b
                    c
                """.trimIndent()
            ),
            Arguments.of(
                """
                    /*comment
                    * a
                    * b
                    * c*/
                """.trimIndent(),
                """
                    comment
                    a
                    b
                    c
                """.trimIndent()
            ),
            Arguments.of(
                """
                    /*
                    a
                    b
                    c
                    */
                """.trimIndent(),
                """
                    
                    a
                    b
                    c
                    
                """.trimIndent()
            ),
            Arguments.of(
                """
                    /*
                    a
                    b

                    c
                    */
                """.trimIndent(),
                """
                    
                    a
                    b

                    c
                    
                """.trimIndent()
            ),
        )

        @ParameterizedTest(name = "Given {0} comment, getContent return {1}")
        @MethodSource("getCommentsContentArguments")
        fun test(comment: String, content: String) {
            assertThat(comment.getCommentContent()).isEqualTo(content)
        }
    }
}
