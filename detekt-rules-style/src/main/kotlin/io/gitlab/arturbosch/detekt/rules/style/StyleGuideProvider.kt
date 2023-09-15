package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.internal.ActiveByDefault
import io.gitlab.arturbosch.detekt.api.internal.DefaultRuleSetProvider
import io.gitlab.arturbosch.detekt.rules.style.movelambdaout.UnnecessaryBracesAroundTrailingLambda
import io.gitlab.arturbosch.detekt.rules.style.optional.MandatoryBracesLoops
import io.gitlab.arturbosch.detekt.rules.style.optional.OptionalUnit
import io.gitlab.arturbosch.detekt.rules.style.optional.PreferToOverPairSyntax

/**
 * The Style ruleset provides rules that assert the style of the code.
 * This will help keep code in line with the given
 * code style guidelines.
 */
@ActiveByDefault(since = "1.0.0")
class StyleGuideProvider : DefaultRuleSetProvider {

    override val ruleSetId: String = "style"

    @Suppress("LongMethod")
    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            CanBeNonNullable(config),
            CascadingCallWrapping(config),
            ClassOrdering(config),
            CollapsibleIfStatements(config),
            DestructuringDeclarationWithTooManyEntries(config),
            ReturnCount(config),
            ThrowsCount(config),
            TrimMultilineRawString(config),
            NewLineAtEndOfFile(config),
            WildcardImport(config),
            MaxLineLength(config),
            TrailingWhitespace(config),
            NoTabs(config),
            EqualsOnSignatureLine(config),
            EqualsNullCall(config),
            ForbiddenAnnotation(config),
            ForbiddenComment(config),
            ForbiddenImport(config),
            ForbiddenMethodCall(config),
            ForbiddenSuppress(config),
            FunctionOnlyReturningConstant(config),
            SpacingAfterPackageDeclaration(config),
            LoopWithTooManyJumpStatements(config),
            SafeCast(config),
            AbstractClassCanBeConcreteClass(config),
            AbstractClassCanBeInterface(config),
            UnnecessaryAnnotationUseSiteTarget(config),
            UnnecessaryParentheses(config),
            UnnecessaryInheritance(config),
            UnnecessaryInnerClass(config),
            UtilityClassWithPublicConstructor(config),
            ObjectLiteralToLambda(config),
            OptionalAbstractKeyword(config),
            OptionalUnit(config),
            ProtectedMemberInFinalClass(config),
            SerialVersionUIDInSerializableClass(config),
            MagicNumber(config),
            ModifierOrder(config),
            DataClassContainsFunctions(config),
            DataClassShouldBeImmutable(config),
            UseDataClass(config),
            UnusedImport(config),
            UnusedParameter(config),
            UnusedPrivateClass(config),
            UnusedPrivateMember(config),
            UnusedPrivateProperty(config),
            ExpressionBodySyntax(config),
            NestedClassesVisibility(config),
            RedundantVisibilityModifierRule(config),
            RangeUntilInsteadOfRangeTo(config),
            UnnecessaryApply(config),
            UnnecessaryBracesAroundTrailingLambda(config),
            UnnecessaryFilter(config),
            UnnecessaryLet(config),
            MayBeConstant(config),
            PreferToOverPairSyntax(config),
            BracesOnIfStatements(config),
            BracesOnWhenStatements(config),
            MandatoryBracesLoops(config),
            NullableBooleanCheck(config),
            VarCouldBeVal(config),
            ForbiddenVoid(config),
            ExplicitItLambdaParameter(config),
            ExplicitCollectionElementAccessMethod(config),
            UselessCallOnNotNull(config),
            UnderscoresInNumericLiterals(config),
            UseRequire(config),
            UseCheckOrError(config),
            UseIfInsteadOfWhen(config),
            RedundantExplicitType(config),
            UseArrayLiteralsInAnnotations(config),
            UseEmptyCounterpart(config),
            UseCheckNotNull(config),
            UseRequireNotNull(config),
            RedundantHigherOrderMapUsage(config),
            UseIfEmptyOrIfBlank(config),
            MultilineLambdaItParameter(config),
            MultilineRawStringIndentation(config),
            StringShouldBeRawString(config),
            UseIsNullOrEmpty(config),
            UseOrEmpty(config),
            UseAnyOrNoneInsteadOfFind(config),
            UnnecessaryBackticks(config),
            MaxChainedCallsOnSameLine(config),
            AlsoCouldBeApply(config),
            UseSumOfInsteadOfFlatMapSize(config),
            DoubleNegativeLambda(config),
            UseLet(config),
            StatementCouldBeExpression(config),
        ),
    )
}
