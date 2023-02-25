package io.gitlab.arturbosch.detekt.rules.naming

import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLint
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ConstructorParameterNamingSpec {

    @Test
    fun `should detect no violations`() {
        val code = """
            class C(val param: String, private val privateParam: String)
            
            class D {
                constructor(param: String) {}
                constructor(param: String, privateParam: String) {}
            }
        """.trimIndent()
        assertThat(ConstructorParameterNaming().compileAndLint(code)).isEmpty()
    }

    @Test
    fun `should find some violations`() {
        val code = """
            class C(val PARAM: String, private val PRIVATE_PARAM: String)
            
            class C {
                constructor(PARAM: String) {}
                constructor(PARAM: String, PRIVATE_PARAM: String) {}
            }
        """.trimIndent()
        assertThat(ConstructorParameterNaming().compileAndLint(code)).hasSize(5)
    }

    @Test
    fun `should find a violation in the correct text location`() {
        val code = """
            class C(val PARAM: String)
        """.trimIndent()
        assertThat(ConstructorParameterNaming().compileAndLint(code)).hasTextLocations(8 to 25)
    }

    @Test
    fun `should not complain about override by default`() {
        val code = """
            class C(override val PARAM: String) : I
            
            interface I { val PARAM: String }
        """.trimIndent()
        assertThat(ConstructorParameterNaming().compileAndLint(code)).isEmpty()
    }

    @Test
    fun `should not complain about override when ignore overridden = false`() {
        val code = """
            class C(override val PARAM: String) : I
            
            interface I { val PARAM: String }
        """.trimIndent()
        val config = TestConfig(IGNORE_OVERRIDDEN to "false")
        assertThat(ConstructorParameterNaming(config).compileAndLint(code)).hasTextLocations(8 to 34)
    }

    @Nested
    inner class `with backticks` {
        @Test
        fun `should not complain about public param name - #5531`() {
            val code = """
                class Foo(val `is`: Boolean)
            """.trimIndent()
            assertThat(ConstructorParameterNaming().compileAndLint(code)).isEmpty()
        }

        @Test
        fun `should not complain about private param name`() {
            val code = """
                class Foo(private val `is`: Boolean)
            """.trimIndent()
            assertThat(ConstructorParameterNaming().compileAndLint(code)).isEmpty()
        }

        @Test
        fun `should complain about param name with violation`() {
            val code = """
                class Foo(private val `PARAM_NAME`: Boolean)
            """.trimIndent()
            assertThat(ConstructorParameterNaming().compileAndLint(code))
                .hasSize(1)
                .hasStartSourceLocation(1, 11)
        }

        @Test
        fun `should not complain about param in secondary constructor`() {
            val code = """
                @JvmInline
                value class A1 constructor(val `is`: Boolean) {
                    constructor(`is`: Boolean, `when`: Boolean): this(`is`)
                }
            """.trimIndent()
            assertThat(ConstructorParameterNaming().compileAndLint(code)).isEmpty()
        }
    }
}

private const val IGNORE_OVERRIDDEN = "ignoreOverridden"
