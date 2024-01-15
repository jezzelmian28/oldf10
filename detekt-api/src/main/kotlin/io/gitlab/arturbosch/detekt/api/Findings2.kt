package io.gitlab.arturbosch.detekt.api

interface Finding2 : Compactable, HasEntity {
    val issue: Issue
    val references: List<Entity>
    val message: String
    val severity: Severity
        get() = Severity.DEFAULT
    val autoCorrectEnabled: Boolean
}

/**
 * Describes a source code position.
 */
interface HasEntity {
    val entity: Entity
    val location: Location
        get() = entity.location
    val startPosition: SourceLocation
        get() = location.source
    val charPosition: TextLocation
        get() = location.text
    val file: String
        get() = location.filePath.absolutePath.toString()
    val signature: String
        get() = entity.signature
}

/**
 * Provides a compact string representation.
 */
interface Compactable {
    /**
     * Contract to format implementing object to a string representation.
     */
    fun compact(): String

    /**
     * Same as [compact] except the content should contain a substring which represents
     * this exact findings via a custom identifier.
     */
    fun compactWithSignature(): String = compact()
}

fun Finding.toFinding2(): Finding2 {
    return when (this) {
        is CorrectableCodeSmell -> Finding2Impl(issue, entity, message, references, autoCorrectEnabled).also {
            it.internalSeverity = severity
        }

        is CodeSmell -> Finding2Impl(issue, entity, message, references).also {
            it.internalSeverity = severity
        }

        else -> error("wtf?")
    }
}

open class Finding2Impl(
    final override val issue: Issue,
    final override val entity: Entity,
    final override val message: String,
    final override val references: List<Entity> = emptyList(),
    final override val autoCorrectEnabled: Boolean = false,
) : Finding2 {
    init {
        require(message.isNotBlank()) { "The message should not be empty" }
    }

    var internalSeverity: Severity? = null
    override val severity: Severity
        get() = internalSeverity ?: super.severity

    override fun compact(): String = "${issue.id} - ${entity.compact()}"

    override fun compactWithSignature(): String = compact() + " - Signature=" + entity.signature

    override fun toString(): String {
        return "CodeSmell(issue=$issue, " +
            "entity=$entity, " +
            "message=$message, " +
            "references=$references, " +
            "severity=$severity)"
    }
}
