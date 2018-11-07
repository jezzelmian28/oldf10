package io.gitlab.arturbosch.detekt.cli.baseline

import org.xml.sax.SAXParseException
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.SAXParserFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamWriter

/**
 * @author Artur Bosch
 */
internal class BaselineFormat {

	fun read(path: Path, sourceSetId: String? = null): Baseline =
			read(path) { createBaseline(sourceSetId) }

	fun readConsolidated(path: Path): ConsolidatedBaseline =
			read(path) { createConsolidatedBaseline() }

	private fun <T> read(path: Path, create: BaselineHandler.() -> T): T {
		try {
			Files.newInputStream(path).use {
				val reader = SAXParserFactory.newInstance().newSAXParser()
				val handler = BaselineHandler()
				reader.parse(it, handler)
				return handler.create()
			}
		} catch (error: SAXParseException) {
			val (line, column) = error.lineNumber to error.columnNumber
			throw InvalidBaselineState("Error on position $line:$column while reading the baseline xml file!", error)
		}
	}

	fun write(baseline: ConsolidatedBaseline, path: Path) {
		try {
			Files.newBufferedWriter(path).use {
				it.streamXml().prettyPrinter().save(baseline)
			}
		} catch (error: XMLStreamException) {
			val (line, column) = error.positions
			throw InvalidBaselineState("Error on position $line:$column while writing the baseline xml file!", error)
		}
	}

	private val XMLStreamException.positions
		get() = location.lineNumber to location.columnNumber

	private fun XMLStreamWriter.save(baseline: ConsolidatedBaseline) {
		document {
			tag(SMELL_BASELINE) {
				tag(BLACKLIST) {
					val (ids, timestamp) = baseline.blacklist
					attribute(TIMESTAMP, timestamp)
					ids.forEach { tag(ID, it) }
				}
				baseline.defaultWhitelist?.let { whitelist ->
					tag(WHITELIST) {
						val (sourceSetId, ids, timestamp) = whitelist
						sourceSetId?.let { attribute(SOURCE_SET_ID, sourceSetId) }
						attribute(TIMESTAMP, timestamp)
						ids.forEach { tag(ID, it) }
					}
				}
				baseline.whitelists.forEach { sourceSetId, whitelist ->
					tag(WHITELIST) {
						attribute(SOURCE_SET_ID, sourceSetId)
						attribute(TIMESTAMP, whitelist.timestamp)
						whitelist.ids.forEach { tag(ID, it) }
					}
				}
			}
		}
	}
}
