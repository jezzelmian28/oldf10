package io.gitlab.arturbosch.detekt.rules.coroutines

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.resolve.calls.util.getCalleeExpressionIfAny

/**
 * Report usages of `GlobalScope.launch` and `GlobalScope.async`. It is highly discouraged by the Kotlin documentation:
 *
 * > Global scope is used to launch top-level coroutines which are operating on the whole application lifetime and are
 * > not cancelled prematurely.
 *
 * > Application code usually should use an application-defined CoroutineScope. Using async or launch on the instance
 * > of GlobalScope is highly discouraged.
 *
 * See https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-global-scope/
 *
 * <noncompliant>
 * fun foo() {
 *     GlobalScope.launch { delay(1_000L) }
 * }
 * </noncompliant>
 *
 * <compliant>
 * val scope = CoroutineScope(Dispatchers.Default)
 *
 * fun foo() {
 *     scope.launch { delay(1_000L) }
 * }
 *
 * fun onDestroy() {
 *    scope.cancel()
 * }
 * </compliant>
 */
class GlobalCoroutineUsage(config: Config) : Rule(
    config,
    "The usage of the `GlobalScope` instance is highly discouraged."
) {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        if (expression.receiverExpression.text == "GlobalScope" &&
            expression.getCalleeExpressionIfAny()?.text in listOf("launch", "async")
        ) {
            report(CodeSmell(issue, Entity.from(expression), MESSAGE))
        }

        super.visitDotQualifiedExpression(expression)
    }

    companion object {
        private const val MESSAGE =
            "This use of GlobalScope should be replaced by `CoroutineScope` or `coroutineScope`."
    }
}
