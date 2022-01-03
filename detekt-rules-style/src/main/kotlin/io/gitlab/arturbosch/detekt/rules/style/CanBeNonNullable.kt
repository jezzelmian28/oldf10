package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.DetektVisitor
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import io.gitlab.arturbosch.detekt.rules.isNonNullCheck
import io.gitlab.arturbosch.detekt.rules.isNullCheck
import io.gitlab.arturbosch.detekt.rules.isOpen
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPostfixExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtPropertyDelegate
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtWhenCondition
import org.jetbrains.kotlin.psi.KtWhenConditionIsPattern
import org.jetbrains.kotlin.psi.KtWhenConditionWithExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.allChildren
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.callUtil.getType
import org.jetbrains.kotlin.resolve.calls.smartcasts.getKotlinTypeForComparison
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.types.isNullable

/**
 * This rule inspects variables marked as nullable and reports which could be
 * declared as non-nullable instead.
 *
 * <noncompliant>
 * class A {
 *     var a: Int? = 5
 *
 *     fun foo() {
 *         a = 6
 *     }
 * }
 *
 * class A {
 *     val a: Int?
 *         get() = 5
 * }
 *
 * fun foo(a: Int?) {
 *     val b = a!! + 2
 * }
 * </noncompliant>
 *
 * <compliant>
 * class A {
 *     var a: Int = 5
 *
 *     fun foo() {
 *         a = 6
 *     }
 * }
 *
 * class A {
 *     val a: Int
 *         get() = 5
 * }
 *
 * fun foo(a: Int) {
 *     val b = a + 2
 * }
 * </compliant>
 */
@RequiresTypeResolution
class CanBeNonNullable(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Variable can be changed to non-nullable, as it is never set to null.",
        Debt.TEN_MINS
    )

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        PropertyCheckVisitor().visitKtFile(file)
        ParameterCheckVisitor().visitKtFile(file)
    }

    @Suppress("TooManyFunctions")
    private inner class ParameterCheckVisitor : DetektVisitor() {
        private val nullableParams = mutableMapOf<DeclarationDescriptor, NullableParam>()

        override fun visitNamedFunction(function: KtNamedFunction) {
            val candidateDescriptors = mutableSetOf<DeclarationDescriptor>()
            function.valueParameters.asSequence()
                .filter {
                    it.typeReference?.typeElement is KtNullableType
                }.mapNotNull { parameter ->
                    bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, parameter]?.let {
                        it to parameter
                    }
                }.forEach { (descriptor, param) ->
                    candidateDescriptors.add(descriptor)
                    nullableParams[descriptor] = NullableParam(param)
                }

            val validSingleChildExpression = when (val functionInitializer = function.initializer) {
                null -> {
                    function.bodyBlockExpression.isEligibleSingleExpression(candidateDescriptors)
                }
                is KtCallExpression -> functionInitializer.isEligibleSingleExpression(candidateDescriptors)
                is KtSafeQualifiedExpression -> functionInitializer.isEligibleSingleExpression(candidateDescriptors)
                else -> {
                    ELIGIBLE_SINGLE_EXPRESSION
                }
            }

            // Evaluate the function, then analyze afterwards whether the candidate properties
            // could be made non-nullable.
            super.visitNamedFunction(function)

            candidateDescriptors.asSequence()
                .mapNotNull(nullableParams::remove)
                // The heuristic for whether a nullable param can be made non-nullable is:
                // * It has been forced into a non-null type, either by `!!` or by
                //   `checkNonNull()`/`requireNonNull()`, or
                // * The containing function only consists of a single non-null check on
                //   the param, either via an if/when check or with a safe-qualified expression.
                .filter {
                    val onlyNonNullCheck = validSingleChildExpression && it.isNonNullChecked && !it.isNullChecked
                    it.isNonNullForced || onlyNonNullCheck
                }.forEach { nullableParam ->
                    report(
                        CodeSmell(
                            issue,
                            Entity.from(nullableParam.param),
                            "The nullable parameter '${nullableParam.param.name}' can be made non-nullable."
                        )
                    )
                }
        }

        override fun visitCallExpression(expression: KtCallExpression) {
            val calleeName = expression.calleeExpression
                .getResolvedCall(bindingContext)
                ?.resultingDescriptor
                ?.name
                ?.toString()
            // Check for whether a call to `checkNonNull()` or `requireNonNull()` has
            // been made.
            if (calleeName == REQUIRE_NOT_NULL_NAME || calleeName == CHECK_NOT_NULL_NAME) {
                expression.valueArguments.forEach { valueArgument ->
                    updateNullableParam(valueArgument.getArgumentExpression()) { it.isNonNullForced = true }
                }
            }
            super.visitCallExpression(expression)
        }

        override fun visitPostfixExpression(expression: KtPostfixExpression) {
            if (expression.operationToken == KtTokens.EXCLEXCL) {
                updateNullableParam(expression.baseExpression) { it.isNonNullForced = true }
            }
            super.visitPostfixExpression(expression)
        }

        override fun visitWhenExpression(expression: KtWhenExpression) {
            val subjectDescriptor = expression.subjectExpression
                ?.let { it as? KtNameReferenceExpression }
                ?.getResolvedCall(bindingContext)
                ?.resultingDescriptor
            val whenConditions = expression.entries.flatMap { it.conditions.asList() }
            if (subjectDescriptor != null) {
                whenConditions.evaluateSubjectWhenExpression(expression, subjectDescriptor)
            } else {
                whenConditions.forEach { whenCondition ->
                    if (whenCondition is KtWhenConditionWithExpression) {
                        whenCondition.expression.evaluateCheckStatement(expression.elseExpression)
                    }
                }
            }
            super.visitWhenExpression(expression)
        }

        override fun visitIfExpression(expression: KtIfExpression) {
            expression.condition.evaluateCheckStatement(expression.`else`)
            super.visitIfExpression(expression)
        }

        override fun visitSafeQualifiedExpression(expression: KtSafeQualifiedExpression) {
            updateNullableParam(expression.receiverExpression) { it.isNonNullChecked = true }
            super.visitSafeQualifiedExpression(expression)
        }

        override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
            val isExtensionForNullable = expression.getResolvedCall(bindingContext)
                ?.resultingDescriptor
                ?.extensionReceiverParameter
                ?.type
                ?.isMarkedNullable
            if (isExtensionForNullable == true) {
                updateNullableParam(expression.receiverExpression.getRootExpression()) { it.isNullChecked = true }
            }
            super.visitDotQualifiedExpression(expression)
        }

        override fun visitBinaryExpression(expression: KtBinaryExpression) {
            if (expression.operationToken == KtTokens.ELVIS) {
                updateNullableParam(expression.left.getRootExpression()) { it.isNullChecked = true }
            }
            super.visitBinaryExpression(expression)
        }

        private fun KtExpression?.getRootExpression(): KtExpression? {
            // Look for the expression that was the root of a potential call chain.
            var receiverExpression = this
            while (receiverExpression is KtQualifiedExpression) {
                receiverExpression = receiverExpression.receiverExpression
            }
            return receiverExpression
        }

        private fun KtSafeQualifiedExpression.isEligibleSingleExpression(candidates: Set<DeclarationDescriptor>): Boolean {
            return this.getRootExpression()
                .getResolvedCall(bindingContext)
                ?.resultingDescriptor
                ?.let(candidates::contains) != true
        }

        private fun KtCallExpression.isEligibleSingleExpression(candidates: Set<DeclarationDescriptor>): Boolean {
            val isFromNullableParam = this.getRootExpression()
                .getResolvedCall(bindingContext)
                ?.resultingDescriptor
                ?.let(candidates::contains)
            return if (isFromNullableParam == true) {
                NOT_ELIGIBLE_SINGLE_EXPRESSION
            } else {
                lambdaArguments.isNotEmpty() && lambdaArguments.all { lambdaArgument ->
                    lambdaArgument.getLambdaExpression()
                        ?.functionLiteral
                        ?.bodyBlockExpression.isEligibleSingleExpression(candidates)
                }
            }
        }

        private fun KtReturnExpression.isEligibleSingleExpression(candidates: Set<DeclarationDescriptor>): Boolean {
            return when (val returnedExpression = returnedExpression) {
                is KtCallExpression -> returnedExpression.isEligibleSingleExpression(candidates)
                is KtSafeQualifiedExpression -> returnedExpression.isEligibleSingleExpression(candidates)
                else -> ELIGIBLE_SINGLE_EXPRESSION
            }
        }

        private fun KtBlockExpression?.isEligibleSingleExpression(candidates: Set<DeclarationDescriptor>): Boolean {
            val children = this?.allChildren
                ?.filterIsInstance<KtExpression>()
                ?.toList()
                .orEmpty()
            return if (children.size == 1) {
                when(val child = children.first()) {
                    is KtCallExpression -> child.isEligibleSingleExpression(candidates)
                    is KtReturnExpression -> child.isEligibleSingleExpression(candidates)
                    else -> ELIGIBLE_SINGLE_EXPRESSION
                }
            } else {
                NOT_ELIGIBLE_SINGLE_EXPRESSION
            }
        }

        private fun KtExpression?.getNonNullChecks(): List<CallableDescriptor>? {
            return when (this) {
                is KtBinaryExpression -> evaluateBinaryExpression()
                is KtIsExpression -> evaluateIsExpression()
                else -> null
            }
        }

        private fun KtExpression?.evaluateCheckStatement(elseExpression: KtExpression?) {
            this.getNonNullChecks()?.let { nonNullChecks ->
                val nullableParamCallback = if (elseExpression.isValidElseExpression()) {
                    { nullableParam: NullableParam ->
                        nullableParam.isNonNullChecked = true
                        nullableParam.isNullChecked = true
                    }
                } else {
                    { nullableParam -> nullableParam.isNonNullChecked = true }
                }
                nonNullChecks.forEach { nullableParams[it]?.let(nullableParamCallback) }
            }
        }

        // Helper function for if- and when-statements that will recursively check for whether
        // any function params have been checked for being a non-nullable type.
        private fun KtBinaryExpression.evaluateBinaryExpression(): List<CallableDescriptor> {
            val leftExpression = left
            val rightExpression = right
            val nonNullChecks = mutableListOf<CallableDescriptor>()

            fun getDescriptor(leftExpression: KtExpression?, rightExpression: KtExpression?): CallableDescriptor? {
                return when {
                    leftExpression is KtNameReferenceExpression -> leftExpression
                    rightExpression is KtNameReferenceExpression -> rightExpression
                    else -> null
                }?.getResolvedCall(bindingContext)
                    ?.resultingDescriptor
            }

            if (isNullCheck()) {
                getDescriptor(leftExpression, rightExpression)
                    ?.let { nullableParams[it] }
                    ?.let { it.isNullChecked = true }
            } else if (isNonNullCheck()) {
                getDescriptor(leftExpression, rightExpression)?.let(nonNullChecks::add)
            }

            // Recursively iterate into the if-check if possible
            leftExpression.getNonNullChecks()?.let(nonNullChecks::addAll)
            rightExpression.getNonNullChecks()?.let(nonNullChecks::addAll)
            return nonNullChecks
        }

        private fun KtIsExpression.evaluateIsExpression(): List<CallableDescriptor> {
            val descriptor = this.leftHandSide.getResolvedCall(bindingContext)?.resultingDescriptor
                ?: return emptyList()
            return if (isNullableCheck(typeReference, isNegated)) {
                nullableParams[descriptor]?.let { it.isNullChecked = true }
                emptyList()
            } else {
                listOf(descriptor)
            }
        }

        private fun List<KtWhenCondition>.evaluateSubjectWhenExpression(
            expression: KtWhenExpression,
            subjectDescriptor: CallableDescriptor
        ) {
            var isNonNullChecked = false
            var isNullChecked = false
            forEach { whenCondition ->
                when (whenCondition) {
                    is KtWhenConditionWithExpression -> {
                        if (whenCondition.expression?.text == "null") {
                            isNullChecked = true
                        }
                    }
                    is KtWhenConditionIsPattern -> {
                        if (isNullableCheck(whenCondition.typeReference, whenCondition.isNegated)) {
                            isNullChecked = true
                        } else {
                            isNonNullChecked = true
                        }
                    }
                }
            }
            if (expression.elseExpression.isValidElseExpression()) {
                if (isNullChecked) {
                    isNonNullChecked = true
                } else if (isNonNullChecked) {
                    isNullChecked = true
                }
            }
            nullableParams[subjectDescriptor]?.let {
                if (isNullChecked) it.isNullChecked = true
                if (isNonNullChecked) it.isNonNullChecked = true
            }
        }

        private fun isNullableCheck(typeReference: KtTypeReference?, isNegated: Boolean): Boolean {
            val isNullable = typeReference.isNullable(bindingContext)
            return (isNullable && !isNegated) || (!isNullable && isNegated)
        }

        private fun KtExpression?.isValidElseExpression(): Boolean {
            return this != null && this !is KtIfExpression && this !is KtWhenExpression
        }

        private fun KtTypeReference?.isNullable(bindingContext: BindingContext): Boolean {
            return this?.let { bindingContext[BindingContext.TYPE, it] }?.isMarkedNullable == true
        }

        private fun updateNullableParam(expression: KtExpression?, updateCallback: (NullableParam) -> Unit) {
            expression?.getResolvedCall(bindingContext)
                ?.resultingDescriptor
                ?.let { nullableParams[it] }
                ?.let(updateCallback)
        }
    }

    private class NullableParam(val param: KtParameter) {
        var isNullChecked = false
        var isNonNullChecked = false
        var isNonNullForced = false
    }

    private inner class PropertyCheckVisitor : DetektVisitor() {
        // A list of properties that are marked as nullable during their
        // declaration but do not explicitly receive a nullable value in
        // the declaration, so they could potentially be marked as non-nullable
        // if the file does not encounter these properties being assigned
        // a nullable value.
        private val candidateProps = mutableMapOf<FqName, KtProperty>()

        override fun visitKtFile(file: KtFile) {
            super.visitKtFile(file)
            // Any candidate properties that were not removed during the inspection
            // of the Kotlin file were never assigned nullable values in the code,
            // thus they can be converted to non-nullable.
            candidateProps.forEach { (_, property) ->
                report(
                    CodeSmell(
                        issue,
                        Entity.from(property),
                        "The nullable variable '${property.name}' can be made non-nullable."
                    )
                )
            }
        }

        override fun visitClass(klass: KtClass) {
            if (!klass.isInterface()) {
                super.visitClass(klass)
            }
        }

        override fun visitProperty(property: KtProperty) {
            if (property.getKotlinTypeForComparison(bindingContext)?.isNullable() == true) {
                val fqName = property.fqName
                if (property.isCandidate() && fqName != null) {
                    candidateProps[fqName] = property
                }
            }
            super.visitProperty(property)
        }

        override fun visitBinaryExpression(expression: KtBinaryExpression) {
            if (expression.operationToken == KtTokens.EQ) {
                val fqName = expression.left
                    ?.getResolvedCall(bindingContext)
                    ?.resultingDescriptor
                    ?.fqNameOrNull()
                if (
                    fqName != null &&
                    candidateProps.containsKey(fqName) &&
                    expression.right?.isNullableType() == true
                ) {
                    // A candidate property has been assigned a nullable value
                    // in the file's code, so it can be removed from the map of
                    // candidates for flagging.
                    candidateProps.remove(fqName)
                }
            }
            super.visitBinaryExpression(expression)
        }

        private fun KtProperty.isCandidate(): Boolean {
            if (isOpen()) return false
            val isSetToNonNullable = initializer?.isNullableType() != true &&
                getter?.isNullableType() != true &&
                delegate?.returnsNullable() != true
            val cannotSetViaNonPrivateMeans = !isVar || (isPrivate() || (setter?.isPrivate() == true))
            return isSetToNonNullable && cannotSetViaNonPrivateMeans
        }

        private fun KtPropertyDelegate?.returnsNullable(): Boolean {
            val property = this?.parent as? KtProperty ?: return false
            val propertyDescriptor =
                bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, property] as? PropertyDescriptor
            return propertyDescriptor?.getter?.let {
                bindingContext[BindingContext.DELEGATED_PROPERTY_RESOLVED_CALL, it]
                    ?.resultingDescriptor
                    ?.returnType
                    ?.isNullable() == true
            } ?: false
        }

        private fun KtExpression?.isNullableType(): Boolean {
            return when (this) {
                is KtConstantExpression -> {
                    this.text == "null"
                }
                is KtIfExpression -> {
                    this.then.isNullableType() || this.`else`.isNullableType()
                }
                is KtPropertyAccessor -> {
                    (initializer?.getType(bindingContext)?.isNullable() == true) ||
                        (
                            bodyExpression
                                ?.collectDescendantsOfType<KtReturnExpression>()
                                ?.any { it.returnedExpression.isNullableType() } == true
                            )
                }
                else -> {
                    this?.getType(bindingContext)?.isNullable() == true
                }
            }
        }
    }

    private companion object {
        private const val REQUIRE_NOT_NULL_NAME = "requireNotNull"
        private const val CHECK_NOT_NULL_NAME = "checkNotNull"

        private const val NOT_ELIGIBLE_SINGLE_EXPRESSION = false
        private const val ELIGIBLE_SINGLE_EXPRESSION = true
    }
}
