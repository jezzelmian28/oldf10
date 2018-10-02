package io.gitlab.arturbosch.detekt.extensions

import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

class DetektReports(project: Project) {

	val xml = DetektReport(XML_REPORT_NAME, project)

	val html = DetektReport(HTML_REPORT_NAME, project)

	val all = listOf(xml, html)

	fun forEach(configure: (DetektReport) -> Unit) = all.forEach(configure)

	fun withName(name: String, configure: DetektReport.() -> Unit) = all.find { it.name == name }?.let(configure)

	fun xml(configure: DetektReport.() -> Unit) = xml.configure()
	fun xml(closure: Closure<*>): DetektReport = ConfigureUtil.configure(closure, xml)

	fun html(configure: DetektReport.() -> Unit) = html.configure()
	fun html(closure: Closure<*>): DetektReport = ConfigureUtil.configure(closure, html)

	companion object {
		const val XML_REPORT_NAME = "xml"
		const val HTML_REPORT_NAME = "html"
	}
}
