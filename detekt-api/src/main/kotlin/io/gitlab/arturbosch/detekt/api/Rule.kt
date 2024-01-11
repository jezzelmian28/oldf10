package io.gitlab.arturbosch.detekt.api

import io.gitlab.arturbosch.detekt.api.Config.Companion.SEVERITY_KEY
import io.gitlab.arturbosch.detekt.api.internal.PathFilters
import io.gitlab.arturbosch.detekt.api.internal.createPathFilters
import io.gitlab.arturbosch.detekt.api.internal.isSuppressedBy
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext

/**
 * A rule defines how one specific code structure should look like. If code is found
 * which does not meet this structure, it is considered as harmful regarding maintainability
 * or readability.
 *
 * A rule is implemented using the visitor pattern and should be started using the visit(KtFile)
 * function. If calculations must be done before or after the visiting process, here are
 * two predefined (preVisit/postVisit) functions which can be overridden to setup/teardown additional data.
 */
open class Rule(
    val config: Config,
    description: String,
) : DetektVisitor() {

    /**
     * A rule is motivated to point out a specific issue in the code base.
     */
    val issue: Issue by lazy(LazyThreadSafetyMode.NONE) { Issue(ruleId, description) }

    /**
     * An id this rule is identified with.
     * Conventionally the rule id is derived from the issue id as these two classes have a coexistence.
     */
    open val ruleId: RuleId = javaClass.simpleName

    /**
     * List of rule ids which can optionally be used in suppress annotations to refer to this rule.
     */
    val aliases: Set<String> get() = config.valueOrDefault("aliases", defaultRuleIdAliases)

    var bindingContext: BindingContext = BindingContext.EMPTY
    var compilerResources: CompilerResources? = null

    /**
     * The default names which can be used instead of this [ruleId] to refer to this rule in suppression's.
     *
     * When overriding this property make sure to meet following structure for detekt-generator to pick
     * it up and generate documentation for aliases:
     *
     *      override val defaultRuleIdAliases = setOf("Name1", "Name2")
     */
    open val defaultRuleIdAliases: Set<String> = emptySet()

    private val ruleSetId: RuleSetId? get() = config.parent?.parentPath

    val autoCorrect: Boolean
        get() = config.valueOrDefault(Config.AUTO_CORRECT_KEY, false) &&
            (config.parent?.valueOrDefault(Config.AUTO_CORRECT_KEY, true) != false)

    val active: Boolean get() = config.valueOrDefault(Config.ACTIVE_KEY, false)

    /**
     * Rules are aware of the paths they should run on via configuration properties.
     */
    open val filters: PathFilters? by lazy(LazyThreadSafetyMode.NONE) {
        config.createPathFilters()
    }

    private val findings: MutableList<Finding> = mutableListOf()

    /**
     * Before starting visiting kotlin elements, a check is performed if this rule should be triggered.
     * Pre- and post-visit-hooks are executed before/after the visiting process.
     * BindingContext holds the result of the semantic analysis of the source code by the Kotlin compiler. Rules that
     * rely on symbols and types being resolved can use the BindingContext for this analysis. Note that detekt must
     * receive the correct compile classpath for the code being analyzed otherwise the default value
     * [BindingContext.EMPTY] will be used and it will not be possible for detekt to resolve types or symbols.
     */
    fun visitFile(
        root: KtFile,
        bindingContext: BindingContext = BindingContext.EMPTY,
        compilerResources: CompilerResources? = null
    ): List<Finding> {
        findings.clear()
        this.bindingContext = bindingContext
        this.compilerResources = compilerResources
        if (visitCondition(root)) {
            preVisit(root)
            visit(root)
            postVisit(root)
        }
        return findings
    }

    /**
     * Could be overridden by subclasses to specify a behaviour which should be done before
     * visiting kotlin elements.
     */
    protected open fun preVisit(root: KtFile) {
        // nothing to do by default
    }

    /**
     * Init function to start visiting the [KtFile].
     * Can be overridden to start a different visiting process.
     */
    open fun visit(root: KtFile) {
        root.accept(this)
    }

    /**
     * Could be overridden by subclasses to specify a behaviour which should be done after
     * visiting kotlin elements.
     */
    protected open fun postVisit(root: KtFile) {
        // nothing to do by default
    }

    /**
     * Basic mechanism to decide if a rule should run or not.
     *
     * By default, any rule which is declared 'active' in the [Config]
     * or not suppressed by a [Suppress] annotation on file level should run.
     */
    open fun visitCondition(root: KtFile): Boolean =
        active && shouldRunOnGivenFile(root) && !root.isSuppressedBy(ruleId, aliases, ruleSetId)

    private fun shouldRunOnGivenFile(root: KtFile) =
        filters?.isIgnored(root)?.not() ?: true

    private fun Finding.updateWithComputedSeverity() {
        (this as? CodeSmell)?.internalSeverity = computeSeverity()
    }

    /**
     * Compute severity in the priority order:
     * - Severity of the rule
     * - Severity of the parent ruleset
     * - Default severity
     */
    private fun computeSeverity(): Severity {
        val configValue: String = config.valueOrNull(SEVERITY_KEY)
            ?: config.parent?.valueOrNull(SEVERITY_KEY)
            ?: Severity.DEFAULT.name
        return Severity.fromString(configValue)
    }

    /**
     * Reports a single code smell finding.
     *
     * Before adding a finding, it is checked if it is not suppressed
     * by @Suppress or @SuppressWarnings annotations.
     */
    fun report(finding: Finding) {
        finding.updateWithComputedSeverity()
        val ktElement = finding.entity.ktElement
        if (ktElement == null || !ktElement.isSuppressedBy(finding.issue.id, aliases, ruleSetId)) {
            findings.add(finding)
        }
    }
}

/**
 * The type to use when referring to rule ids giving it more context then a String would.
 */
typealias RuleId = String
