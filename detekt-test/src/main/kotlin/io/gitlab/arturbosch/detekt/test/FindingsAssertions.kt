package io.gitlab.arturbosch.detekt.test

import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.api.SourceLocation
import io.gitlab.arturbosch.detekt.api.TextLocation
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.AbstractListAssert
import org.assertj.core.util.Objects.areEqual

fun assertThat(findings: List<Finding>) = FindingsAssert(findings)

fun assertThat(finding: Finding) = FindingAssert(finding)

fun List<Finding>.assert() = FindingsAssert(this)

class FindingsAssert(actual: List<Finding>) :
        AbstractListAssert<FindingsAssert, List<Finding>,
                Finding, FindingAssert>(actual, FindingsAssert::class.java) {

    override fun newAbstractIterableAssert(iterable: MutableIterable<Finding>): FindingsAssert {
        throw UnsupportedOperationException("not implemented")
    }

    override fun toAssert(value: Finding?, description: String?): FindingAssert =
            FindingAssert(value).`as`(description)

    fun hasSourceLocations(vararg expected: SourceLocation) = apply {
        isNotNull

        val actualSources = actual.asSequence()
                .map { it.location.source }
                .sortedWith(compareBy({ it.line }, { it.column }))

        val expectedSources = expected.asSequence()
                .sortedWith(compareBy({ it.line }, { it.column }))

        if (!areEqual(actualSources.toList(), expectedSources.toList())) {
            failWithMessage("Expected source locations to be ${expectedSources.toList()} but was ${actualSources.toList()}")
        }
    }

    fun hasSourceLocation(line: Int, column: Int) = apply {
        hasSourceLocations(SourceLocation(line, column))
    }

    fun hasTextLocations(vararg expected: Pair<Int, Int>) = apply {
        isNotNull

        val actualSources = actual.asSequence()
                .map { it.location.text }
                .sortedWith(compareBy({ it.start }, { it.end }))

        val expectedSources = expected.asSequence()
                .map { (start, end) -> TextLocation(start, end) }
                .sortedWith(compareBy({ it.start }, { it.end }))

        if (!areEqual(actualSources.toList(), expectedSources.toList())) {
            failWithMessage("Expected text locations to be ${expectedSources.toList()} but was ${actualSources.toList()}")
        }
    }
}

class FindingAssert(val actual: Finding?) : AbstractAssert<FindingAssert, Finding>(actual, FindingAssert::class.java)
