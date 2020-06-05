import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    jacoco
}

allprojects {
    group = "io.gitlab.arturbosch.detekt"
    version = Versions.currentOrSnapshot()

    repositories {
        jcenter()
    }
}

subprojects {

    val project = this

    apply {
        plugin("java-library")
        plugin("kotlin")
        plugin("jacoco")
    }

    // bundle detekt's version for all jars to use it at runtime
    tasks.withType<Jar>().configureEach {
        manifest {
            attributes(mapOf("DetektVersion" to Versions.DETEKT))
        }
    }

    jacoco.toolVersion = Versions.JACOCO

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        systemProperty("SPEK_TIMEOUT", 0) // disable test timeout
        val compileSnippetText: Boolean = if (project.hasProperty("compile-test-snippets")) {
            (project.property("compile-test-snippets") as String).toBoolean()
        } else {
            false
        }
        systemProperty("compile-snippet-tests", compileSnippetText)
        testLogging {
            // set options for log level LIFECYCLE
            events = setOf(
                TestLogEvent.FAILED,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STANDARD_OUT,
                TestLogEvent.SKIPPED
            )
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = Versions.JVM_TARGET
        kotlinOptions.freeCompilerArgs = listOf(
            "-progressive",
            "-Xopt-in=kotlin.RequiresOptIn"
        )
        // Usage: <code>./gradlew build -PwarningsAsErrors=true</code>.
        kotlinOptions.allWarningsAsErrors = project.findProperty("warningsAsErrors") == "true"
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))

        testImplementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
        testImplementation("org.spekframework.spek2:spek-dsl-jvm:${Versions.SPEK}")
        testImplementation("org.reflections:reflections:${Versions.REFLECTIONS}")
        testImplementation("io.mockk:mockk:${Versions.MOCKK}")

        testRuntimeOnly("org.junit.platform:junit-platform-launcher:${Versions.JUNIT}")
        testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:${Versions.SPEK}")
    }
}

configure(listOf(project(":detekt-rules"), project(":detekt-formatting"))) {
    tasks.build { finalizedBy(":detekt-generator:generateDocumentation") }
}

jacoco.toolVersion = Versions.JACOCO

val examplesOrTestUtils = setOf("detekt-test", "detekt-test-utils", "detekt-sample-extensions")

tasks {
    jacocoTestReport {
        executionData.setFrom(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))

        subprojects
            .filterNot { it.name in examplesOrTestUtils }
            .forEach {
                this@jacocoTestReport.sourceSets(it.sourceSets.main.get())
                this@jacocoTestReport.dependsOn(it.tasks.test)
            }

        reports {
            xml.isEnabled = true
            xml.destination = file("$buildDir/reports/jacoco/report.xml")
        }
    }
}
