package io.gitlab.arturbosch.detekt.rules.complexity

import io.gitlab.arturbosch.detekt.rules.complexity.LongParameterList.Companion.DEFAULT_CONSTRUCTOR_THRESHOLD
import io.gitlab.arturbosch.detekt.rules.complexity.LongParameterList.Companion.DEFAULT_FUNCTION_THRESHOLD
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.compileAndLint
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class LongParameterListSpec : Spek({

    val subject by memoized { LongParameterList() }

    describe("LongParameterList rule") {

        val reportMessageForFunction =
            "The function long(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int) has too many parameters. " +
                    "The current threshold is set to $DEFAULT_FUNCTION_THRESHOLD."
        val reportMessageForConstructor =
            "The constructor(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int) has too many parameters. " +
                    "The current threshold is set to $DEFAULT_CONSTRUCTOR_THRESHOLD."

        it("reports too long parameter list") {
            val code = "fun long(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int) {}"
            val findings = subject.compileAndLint(code)
            assertThat(findings).hasSize(1)
            assertThat(findings.first().message).isEqualTo(reportMessageForFunction)
        }

        it("does not report short parameter list") {
            val code = "fun long(a: Int, b: Int, c: Int, d: Int, e: Int) {}"
            assertThat(subject.compileAndLint(code)).isEmpty()
        }

        it("reports too long parameter list event for parameters with defaults") {
            val code = "fun long(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int = 1) {}"
            assertThat(subject.compileAndLint(code)).hasSize(1)
        }

        it("does not report long parameter list if parameters with defaults should be ignored") {
            val config = TestConfig(mapOf(LongParameterList.IGNORE_DEFAULT_PARAMETERS to "true"))
            val rule = LongParameterList(config)
            val code = "fun long(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int = 1, g: Int = 2) {}"
            assertThat(rule.compileAndLint(code)).isEmpty()
        }

        it("reports too long parameter list for primary constructors") {
            val code = "class LongCtor(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int)"
            val findings = subject.compileAndLint(code)
            assertThat(findings).hasSize(1)
            assertThat(findings.first().message).isEqualTo(reportMessageForConstructor)
        }

        it("does not report short parameter list for primary constructors") {
            val code = "class LongCtor(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int)"
            assertThat(subject.compileAndLint(code)).isEmpty()
        }

        it("reports too long parameter list for secondary constructors") {
            val code = "class LongCtor() { constructor(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int) : this() }"
            val findings = subject.compileAndLint(code)
            assertThat(findings).hasSize(1)
            assertThat(findings.first().message).isEqualTo(reportMessageForConstructor)
        }

        it("does not report short parameter list for secondary constructors") {
            val code = "class LongCtor() { constructor(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int) : this() }"
            assertThat(subject.compileAndLint(code)).isEmpty()
        }

        it("reports long parameter list if custom threshold is set") {
            val config = TestConfig(mapOf(LongParameterList.CONSTRUCTOR_THRESHOLD to "2"))
            val rule = LongParameterList(config)
            val code = "class LongCtor(a: Int, b: Int, c: Int)"
            assertThat(rule.compileAndLint(code)).hasSize(1)
        }

        it("does not report long parameter list for constructors of data classes if asked") {
            val config = TestConfig(mapOf(LongParameterList.IGNORE_DATA_CLASSES to "true"))
            val rule = LongParameterList(config)
            val code = "data class Data(val a: Int, val b: Int, val c: Int, val d: Int, val e: Int, val f: Int, val g: Int)"
            assertThat(rule.compileAndLint(code)).isEmpty()
        }

        it("does not report long parameter list for constructors if file is annotated with ignored annotation") {
            val config = TestConfig(mapOf(LongParameterList.IGNORE_ANNOTATED to listOf("javax.annotation.Generated")))
            val rule = LongParameterList(config)
            val code = "@file:javax.annotation.Generated class Data(val a: Int, val b: Int, val c: Int, val d: Int, val e: Int, val f: Int, val g: Int)"
            assertThat(rule.compileAndLint(code)).isEmpty()
        }

        it("does not report long parameter list for functions if file is annotated with ignored annotation") {
            val config = TestConfig(mapOf(LongParameterList.IGNORE_ANNOTATED to listOf("javax.annotation.Generated")))
            val rule = LongParameterList(config)
            val code = "@file:javax.annotation.Generated class Data { fun foo(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int) {} }"
            assertThat(rule.compileAndLint(code)).isEmpty()
        }

        it("does not report long parameter list for constructors if class is annotated with ignored annotation") {
            val config = TestConfig(mapOf(LongParameterList.IGNORE_ANNOTATED to listOf("javax.annotation.Generated")))
            val rule = LongParameterList(config)
            val code = "@javax.annotation.Generated class Data(val a: Int, val b: Int, val c: Int, val d: Int, val e: Int, val f: Int, val g: Int)"
            assertThat(rule.compileAndLint(code)).isEmpty()
        }

        it("does not report long parameter list for functions if class is annotated with ignored annotation") {
            val config = TestConfig(mapOf(LongParameterList.IGNORE_ANNOTATED to listOf("javax.annotation.Generated")))
            val rule = LongParameterList(config)
            val code = "@javax.annotation.Generated class Data { fun foo(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int) {} }"
            assertThat(rule.compileAndLint(code)).isEmpty()
        }

        it("does not report long parameter list for constructors if constructor is annotated with ignored annotation") {
            val config = TestConfig(mapOf(LongParameterList.IGNORE_ANNOTATED to listOf("kotlin.Deprecated")))
            val rule = LongParameterList(config)
            val code = "class Data @kotlin.Deprecated(message = \"\") constructor(val a: Int, val b: Int, val c: Int, val d: Int, val e: Int, val f: Int, val g: Int)"
            assertThat(rule.compileAndLint(code)).isEmpty()
        }

        it("does not report long parameter list for functions if function is annotated with ignored annotation") {
            val config = TestConfig(mapOf(LongParameterList.IGNORE_ANNOTATED to listOf("kotlin.Deprecated")))
            val rule = LongParameterList(config)
            val code = "class Data { @kotlin.Deprecated(message = \"\") fun foo(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int) {} }"
            assertThat(rule.compileAndLint(code)).isEmpty()
        }
    }
})
