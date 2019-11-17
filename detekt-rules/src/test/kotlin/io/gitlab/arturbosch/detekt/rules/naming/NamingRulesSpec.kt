package io.gitlab.arturbosch.detekt.rules.naming

import io.gitlab.arturbosch.detekt.api.SourceLocation
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLint
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class NamingRulesSpec : Spek({

    describe("properties in classes") {

        it("should detect all positive cases") {
            val code = """
                class C {
                    private val _FIELD = 5
                    val FIELD get() = _FIELD
                    val camel_Case_Property = 5
                }
            """
            assertThat(VariableNaming().compileAndLint(code))
                .hasSourceLocations(
                    SourceLocation(2, 5),
                    SourceLocation(3, 5),
                    SourceLocation(4, 5)
                )
        }

        it("checks all negative cases") {
            val code = """
                class C {
                    private val _field = 5
                    val field get() = _field
                    val camelCaseProperty = 5
                }
            """
            assertThat(VariableNaming().compileAndLint(code)).isEmpty()
        }

        it("should not flag overridden member properties by default") {
            val code = """
                class C : I {
                    override val SHOULD_NOT_BE_FLAGGED = "banana"
                }
                interface I : I2 {
                    override val SHOULD_NOT_BE_FLAGGED: String
                }
                interface I2 {
                    @Suppress("VariableNaming") val SHOULD_NOT_BE_FLAGGED: String
                }
            """
            assertThat(VariableNaming().compileAndLint(code)).isEmpty()
        }

        it("doesn't ignore overridden member properties if ignoreOverridden is false") {
            val code = """
                class C : I {
                    override val SHOULD_BE_FLAGGED = "banana"
                }
                interface I : I2 {
                    override val SHOULD_BE_FLAGGED: String
                }
                interface I2 {
                    @Suppress("VariableNaming") val SHOULD_BE_FLAGGED: String
                }
            """
            val config = TestConfig(mapOf(IGNORE_OVERRIDDEN to "false"))
            assertThat(VariableNaming(config).compileAndLint(code))
                .hasSourceLocations(
                    SourceLocation(2, 5),
                    SourceLocation(5, 5)
                )
        }
    }

    describe("naming like in constants is allowed for destructuring and lambdas") {
        it("should not detect any") {
            val code = """
                data class D(val i: Int, val j: Int)
                fun doStuff() {
                    val (_, HOLY_GRAIL) = D(5, 4)
                    emptyMap<String, String>().forEach { _, V -> println(V) }
                }
            """
            assertThat(NamingRules().compileAndLint(code)).isEmpty()
        }
    }
})

const val IGNORE_OVERRIDDEN = "ignoreOverridden"
