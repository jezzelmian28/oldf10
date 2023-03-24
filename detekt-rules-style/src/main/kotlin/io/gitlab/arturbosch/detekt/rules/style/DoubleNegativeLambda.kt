package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.config
import io.gitlab.arturbosch.detekt.api.internal.Configuration
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtOperationReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/**
 * Detects negation in lambda blocks where the function name is also in the negative (like `takeUnless`).
 * A double negative is harder to read than a positive. In particular, if there are multiple conditions with `&&` etc. inside
 * the lambda, then the reader may need to unpack these using DeMorgan's laws. Consider rewriting the lambda to use a positive version
 * of the function (like `takeIf`).
 *
 * <noncompliant>
 * Random.Default.nextInt().takeUnless { !it.isEven() }
 * </noncompliant>
 * <compliant>
 * Random.Default.nextInt().takeIf { it.isOdd() }
 * </compliant>
 */
class DoubleNegativeLambda(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        "DoubleNegativeLambdaBlock",
        Severity.Style,
        "Double negative from a function name expressed in the negative (like `takeUnless`) with a lambda block " +
            "that also contains negation. This is more readable when rewritten using a positive form of the function " +
            "(like `takeIf`).",
        Debt.FIVE_MINS,
    )

    private val splitCamelCaseRegex = "(?<=[a-z])(?=[A-Z])".toRegex()

    private val negationTokens = listOf(
        KtTokens.EXCL,
        KtTokens.EXCLEQ,
        KtTokens.EXCLEQEQEQ,
        KtTokens.NOT_IN,
        KtTokens.NOT_IS,
    )

    private val negatingFunctionNameParts = listOf("not", "non")

    @Configuration("Function names expressed in the negative that can form double negatives with their lambda blocks.")
    private val negativeFunctions: Set<String> by config(listOf("takeUnless")) { it.toSet() }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val calleeExpression = expression.calleeExpression?.text ?: return

        if (calleeExpression in negativeFunctions) {
            val lambdaExpression = expression.lambdaArguments.firstOrNull() ?: return
            val forbiddenChildren = lambdaExpression.collectDescendantsOfType<KtExpression> {
                it.isForbiddenNegation()
            }

            if (forbiddenChildren.isNotEmpty()) {
                report(
                    CodeSmell(
                        issue,
                        Entity.from(expression),
                        "Double negative through using ${forbiddenChildren.joinInBackTicks()} inside a " +
                            "`$calleeExpression` lambda. Rewrite in the positive."
                    )
                )
            }
        }
    }

    private fun KtExpression.isForbiddenNegation(): Boolean {
        return when (this) {
            is KtOperationReferenceExpression -> operationSignTokenType in negationTokens
            is KtCallExpression -> text == "not()" || text.split(splitCamelCaseRegex).map { it.lowercase() }
                .any { it in negatingFunctionNameParts }

            else -> false
        }
    }

    private fun List<KtExpression>.joinInBackTicks() = joinToString { "`${it.text}`" }

    companion object {
        const val NEGATIVE_FUNCTIONS = "negativeFunctions"
    }
}
