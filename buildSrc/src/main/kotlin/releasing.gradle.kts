import com.vdurmont.semver4j.Semver

plugins {
    id("com.github.breadmoirai.github-release")
}

githubRelease {
    token(project.findProperty("github.token") as? String ?: "")
    owner.set("arturbosch")
    repo.set("detekt")
    overwrite.set(true)
    dryRun.set(true)
    body {
        var changelog = project.file("docs/pages/changelog 1.x.x.md").readText()
        val sectionStart = "#### ${project.version}"
        changelog = changelog.substring(changelog.indexOf(sectionStart) + sectionStart.length)
        changelog = changelog.substring(0, changelog.indexOf("#### 1"))
        changelog.trim()
    }
    val cliBuildDir = project(":detekt-cli").buildDir
    releaseAssets.setFrom(cliBuildDir.resolve("libs/detekt-cli-${project.version}-all.jar"))
    releaseAssets.setFrom(cliBuildDir.resolve("run/detekt"))
}

val ln: String = System.lineSeparator()

fun updateVersion(increment: (Semver) -> Semver) {
    val versionsFile = file("${rootProject.rootDir}/buildSrc/src/main/kotlin/Versions.kt")
    val newContent = versionsFile.readLines()
        .joinToString(ln) {
            if (it.contains("const val DETEKT: String")) {
                val oldVersion = it.substringAfter("\"").substringBefore("\"")
                val newVersion = Semver(oldVersion).let(increment)
                println("Next release: $newVersion")
                """    const val DETEKT: String = "$newVersion""""
            } else {
                it
            }
        }
    versionsFile.writeText("$newContent$ln")
}

val incrementPatch by tasks.registering { doLast { updateVersion { it.nextPatch() } } }
val incrementMinor by tasks.registering { doLast { updateVersion { it.nextMinor() } } }
val incrementMajor by tasks.registering { doLast { updateVersion { it.nextMajor() } } }

val applyDocVersion by tasks.registering {
    doLast {
        val docConfigFile = file("${rootProject.rootDir}/docs/_config.yml")
        val content = docConfigFile.useLines { lines ->
            lines.mapNotNull {
                if (it.contains("detekt_version:")) {
                    null
                } else {
                    it
                }
            }
                .joinToString(ln)
                .trim()
        }
        println("Applied 'detekt_version: ${Versions.DETEKT}' to docs/_config.yml.")
        docConfigFile.writeText("${content}$ln${ln}detekt_version: ${Versions.DETEKT}$ln")
    }
}
