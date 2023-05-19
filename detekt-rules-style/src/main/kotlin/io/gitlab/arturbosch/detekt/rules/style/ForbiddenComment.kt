package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.config
import io.gitlab.arturbosch.detekt.api.internal.ActiveByDefault
import io.gitlab.arturbosch.detekt.api.internal.Configuration
import io.gitlab.arturbosch.detekt.api.valuesWithReason
import org.jetbrains.kotlin.com.intellij.psi.PsiComment
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

// Note: ​ (zero-width-space) is used to prevent the Kotlin parser getting confused by talking about comments in a comment.
/**
 * This rule allows to set a list of comments which are forbidden in the codebase and should only be used during
 * development. Offending code comments will then be reported.
 *
 * The regular expressions in `comments` list will have the following behaviors while matching the comments:
 *  * Each comment will be handled individually as a whole.
 *    * single line comments are always separate, conjoint lines are not merged.
 *    * multi line comments are not split up, the regex will be applied to the whole comment.
 *    * KDoc comments are not split up, the regex will be applied to the whole comment.
 *  * The comment markers (`//`, `/​*`, `/​**`, `*` aligners, `*​/`) are removed before applying the regex.
 *    One leading space is removed from each line of the comment, after starting markers and aligners.
 *  * The regex is applied as a multiline regex,
 *    see [Anchors](https://www.regular-expressions.info/anchors.html) for more info.
 *    To match the start and end of each line, use `^` and `$`.
 *    To match the start and end of the whole comment, use `\A` and `\Z`.
 *    To turn off multiline, use `(?-m)` at the start of your regex.
 *  * The regex is applied with dotall semantics, meaning `.` will match any character including newlines,
 *    this is to ensure that freeform line-wrapping doesn't mess with simple regexes.
 *    To turn off this behavior, use `(?-s)` at the start of your regex, or use `[^\r\n]*` instead of `.*`.
 *  * The regex will be searched using "contains" semantics not "matches",
 *    so partial comment matches will flag forbidden comments.
 *
 * The rule can be configured to add extra comments to the list of forbidden comments, here are some examples:
 * ```yaml
 *   ForbiddenComment:
 *     comments:
 *       # Repeat the default configuration if it's still needed.
 *       - reason: 'some fixes are pending.'
 *         value: 'FIXME:'
 *       - reason: 'some changes are present which needs to be addressed before ship.'
 *         value: 'STOPSHIP:'
 *       - reason: 'some changes are pending.'
 *         value: 'TODO:'
 *       # Add additional patterns to the list.
 *
 *       - reason: 'Authors are not recorded in KDoc.'
 *         value: '@author'
 *
 *       - reason: 'REVIEW markers are not allowed in production code, only use before PR is merged.'
 *         value: '^\s*(?i)REVIEW\b'
 *         # Explanation: at the beginning of the line, optional leading space,
 *         #              case insensitive (e.g. REVIEW, review, Review), and REVIEW only as a full word.
 *         # Non-compliant: // REVIEW this code before merging.
 *         # Compliant: // Preview will show up here.
 *
 *       - reason: 'Use @androidx.annotation.VisibleForTesting(otherwise = VisibleForTesting.PRIVATE) instead.'
 *         value: '^private$'
 *         # Non-compliant: /​*private*​/ fun f() { }
 *
 *       - reason: 'KDoc tag should have a value.'
 *         value: '^\s*@(?!suppress|hide)\w+\s*$'
 *         # Explanation: full line with optional leading and trailing space, and an at @ character followed by a word,
 *         #              but not @suppress or @hide because those don't need content afterwards.
 *         # Non-compliant: /​** ... @see *​/
 *
 *       - reason: 'include an issue link at the beginning preceded by a space'
 *         value: 'BUG:(?! https://github\.com/company/repo/issues/\d+).*'
 * ```
 *
 * By default the commonly used todo markers are forbidden: `TODO:`, `FIXME:` and `STOPSHIP:`.
 *
 * <noncompliant>
 * val a = "" // TODO: remove please
 * /**
 *  * FIXME: this is a hack
 *  */
 * fun foo() { }
 * /* STOPSHIP: */
 * </noncompliant>
 */
@ActiveByDefault(since = "1.0.0")
class ForbiddenComment(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Flags a forbidden comment.",
        Debt.TEN_MINS
    )

    @Configuration("forbidden comment strings")
    @Deprecated("Use `comments` instead, make sure you escape your text for Regular Expressions.")
    private val values: List<String> by config(emptyList())

    @Configuration("forbidden comment string patterns")
    private val comments: List<Comment> by config(
        valuesWithReason(
            "FIXME:" to "Forbidden FIXME todo marker in comment, please fix the problem.",
            "STOPSHIP:" to "Forbidden STOPSHIP todo marker in comment, please address the problem before shipping the code.",
            "TODO:" to "Forbidden TODO todo marker in comment, please do the changes.",
        )
    ) { list ->
        list.map { Comment(it.value.toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)), it.reason) }
    }

    @Configuration("ignores comments which match the specified regular expression. For example `Ticket|Task`.")
    private val allowedPatterns: Regex by config("", String::toRegex)

    @Configuration("error message which overrides the default one")
    @Deprecated("Use `comments` and provide `reason` against each `value`.")
    private val customMessage: String by config("")

    override fun visitComment(comment: PsiComment) {
        super.visitComment(comment)
        val text = comment.getContent()
        checkForbiddenComment(text, comment)
    }

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        file.collectDescendantsOfType<KDocSection>().forEach { comment ->
            val text = comment.getContent()
            checkForbiddenComment(text, comment)
        }
    }

    private fun checkForbiddenComment(text: String, comment: PsiElement) {
        if (allowedPatterns.pattern.isNotEmpty() && allowedPatterns.containsMatchIn(text)) return

        @Suppress("DEPRECATION")
        values.forEach {
            if (text.contains(it, ignoreCase = true)) {
                reportIssue(comment, getErrorMessage(it))
            }
        }

        comments.forEach {
            if (it.value.containsMatchIn(text)) {
                reportIssue(comment, getErrorMessage(it))
            }
        }
    }

    private fun reportIssue(comment: PsiElement, msg: String) {
        report(
            CodeSmell(
                issue,
                Entity.from(comment),
                msg
            )
        )
    }

    private fun PsiComment.getContent(): String = text.getCommentContent()

    private fun getErrorMessage(comment: Comment): String =
        comment.reason
            ?.let { reason -> String.format(DEFAULT_ERROR_MESSAGE, comment.value.pattern, reason) }
            ?: String.format(DEFAULT_ERROR_MESSAGE_WITH_NO_REASON, comment.value.pattern)

    @Suppress("DEPRECATION")
    private fun getErrorMessage(value: String): String =
        customMessage.takeUnless { it.isEmpty() }
            ?: String.format(DEFAULT_ERROR_MESSAGE_WITH_NO_REASON, value)

    private data class Comment(val value: Regex, val reason: String?)

    companion object {
        const val DEFAULT_ERROR_MESSAGE_WITH_NO_REASON = "This comment contains '%s' " +
            "that has been defined as forbidden in detekt."

        const val DEFAULT_ERROR_MESSAGE = "This comment contains '%s' " +
            "that has been forbidden: %s"
    }
}

internal fun String.getCommentContent(): String {
    return if (this.startsWith("//")) {
        this.removePrefix("//").removePrefix(" ")
    } else {
        this
            .trimIndentIgnoringFirstLine()
            // Process line by line.
            .lineSequence()
            // Remove starting, aligning and ending markers.
            .map {
                it
                    .let { fullLine ->
                        val trimmedStartLine = fullLine.trimStart()
                        if (trimmedStartLine.startsWith("/*")) {
                            trimmedStartLine.removePrefix("/*").removePrefix(" ")
                        } else if (trimmedStartLine.startsWith("*") && trimmedStartLine.startsWith("*/").not()) {
                            trimmedStartLine.removePrefix("*").removePrefix(" ")
                        } else {
                            fullLine
                        }
                    }
                    .let { lineWithoutStartMarker ->
                        if (lineWithoutStartMarker.endsWith("*/")) {
                            lineWithoutStartMarker.removeSuffix("*/").removeSuffix(" ")
                        } else {
                            lineWithoutStartMarker
                        }
                    }
            }
            // Trim trailing empty lines.
            .dropWhile(String::isEmpty)
            // Reconstruct the comment contents.
            .joinToString("\n")
    }
}

private fun String.trimIndentIgnoringFirstLine(): String =
    if ('\n' !in this) {
        this
    } else {
        val lines = this.lineSequence()
        lines.first() + "\n" + lines.drop(1).joinToString("\n").trimIndent()
    }
