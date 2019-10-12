package io.gitlab.arturbosch.detekt.rules.naming

import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileContentForTest
import io.gitlab.arturbosch.detekt.test.lint
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class TopLevelPropertyNamingSpec : Spek({

    val subject by memoized { TopLevelPropertyNaming() }

    describe("constants on top level") {

        it("should not detect any constants not complying to the naming rules") {
            val code = compileContentForTest("""
                const val MY_NAME_8 = "Artur"
                const val MYNAME = "Artur"
            """)
            assertThat(subject.lint(code)).hasSize(0)
        }

        it("should detect five constants not complying to the naming rules") {
            val code = compileContentForTest("""
                const val MyNAME = "Artur"
                const val name = "Artur"
                const val nAme = "Artur"
                private const val _nAme = "Artur"
                const val serialVersionUID = 42L
            """)
            assertThat(subject.lint(code)).hasSize(5)
        }
    }

    describe("variables on top level") {

        it("should not report any") {
            val code = compileContentForTest("""
                val name = "Artur"
                val nAme8 = "Artur"
                private val _name = "Artur"
                val serialVersionUID = 42L
                val MY_NAME = "Artur"
                val MYNAME = "Artur"
                private val NAME = "Artur"
                private val INTERNAL_VERSION = "1.0.0"
                private val _INTERNAL_VERSION = "1.0.0"
            """)
            assertThat(subject.lint(code)).hasSize(0)
        }

        it("should report non private top level property using underscore") {
            val code = compileContentForTest("""
                val _nAme = "Artur"
            """)
            assertThat(subject.lint(code)).hasSize(1)
        }

        it("should report private top level property using two underscores") {
            val code = compileContentForTest("""
                private val __NAME = "Artur"
            """)
            assertThat(subject.lint(code)).hasSize(1)
        }

        it("should report top level property using lowercases and underscores") {
            val code = compileContentForTest("""
                val s_d_d_1 = listOf("")
            """)
            assertThat(subject.lint(code)).hasSize(1)
        }

        it("should report private top level property using lowercases and underscores") {
            val code = compileContentForTest("""
                private val s_d_d_1 = listOf("")
            """)
            assertThat(subject.lint(code)).hasSize(1)
        }

        it("should report top level property starting with upercase and then using lowercases") {
            val code = compileContentForTest("""
                val MyNAME = "Artur"
            """)
            assertThat(subject.lint(code)).hasSize(1)
        }

        it("should report private top level property starting with upercase and then using lowercases") {
            val code = compileContentForTest("""
                private val MyNAME = "Artur"
            """)
            assertThat(subject.lint(code)).hasSize(1)
        }
    }
})
