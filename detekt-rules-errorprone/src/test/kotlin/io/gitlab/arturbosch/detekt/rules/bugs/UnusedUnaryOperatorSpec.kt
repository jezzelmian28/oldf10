package io.gitlab.arturbosch.detekt.rules.bugs

import io.gitlab.arturbosch.detekt.rules.setupKotlinEnvironment
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class UnusedUnaryOperatorSpec : Spek({
    setupKotlinEnvironment()
    val env: KotlinCoreEnvironment by memoized()
    val subject by memoized { UnusedUnaryOperator() }

    describe("UnusedUnaryOperatorSpec rule") {
        it("unused plus operator") {
            val code = """
                fun test() {
                    val x = 1 + 2
                        + 3
                }
            """
            val findings = subject.compileAndLintWithContext(env, code)
            assertThat(findings).hasSize(1)
            assertThat(findings).hasSourceLocation(3, 9)
            assertThat(findings[0]).hasMessage("This '+ 3' is not used")
        }

        it("unused minus operator") {
            val code = """
                fun test() {
                    val x = 1 + 2
                        - 3
                }
            """
            val findings = subject.compileAndLintWithContext(env, code)
            assertThat(findings).hasSize(1)
            assertThat(findings).hasSourceLocation(3, 9)
            assertThat(findings[0]).hasMessage("This '- 3' is not used")
        }

        it("unused plus operator in binary expression") {
            val code = """
                fun test() {
                    val x = 1 + 2
                        + 3 + 4 + 5
                }
            """
            val findings = subject.compileAndLintWithContext(env, code)
            assertThat(findings).hasSize(1)
            assertThat(findings[0]).hasMessage("This '+ 3 + 4 + 5' is not used")
        }

        it("used plus operator") {
            val code = """
                fun test() {
                    val x = (1 + 2
                        + 3 + 4 + 5)
                }
            """
            val findings = subject.compileAndLintWithContext(env, code)
            assertThat(findings).isEmpty()
        }

        it("used minus operator") {
            val code = """
                fun test() {
                    val x = -1
                }
            """
            val findings = subject.compileAndLintWithContext(env, code)
            assertThat(findings).isEmpty()
        }

        it("used as return value") {
            val code = """
                fun test(): Int {
                    return -1
                }
            """
            val findings = subject.compileAndLintWithContext(env, code)
            assertThat(findings).isEmpty()
        }

        it("used as value argument") {
            val code = """
                fun foo(x: Int) {}
                fun test() {
                    foo(x = -1)
                }
            """
            val findings = subject.compileAndLintWithContext(env, code)
            assertThat(findings).isEmpty()
        }

        it("used as annotation value argument") {
            val code = """
                annotation class Ann(val x: Int)
                @Ann(x = -1)
                val y = 2
            """
            val findings = subject.compileAndLintWithContext(env, code)
            assertThat(findings).hasSize(0)
        }

        it("overloaded unary operator") {
            val code = """
                data class Foo(val x: Int)
                operator fun Foo.plus(other: Foo) = Foo(this.x + other.x)
                operator fun Foo.unaryMinus() = Foo(-x)
                fun test() {
                    val p = Foo(1) + Foo(2)
                        - Foo(3)
                } 
            """
            val findings = subject.compileAndLintWithContext(env, code)
            assertThat(findings).isEmpty()
        }
    }
})
