package com.qalqan.antifraud.buildlogic

import org.gradle.api.Project
import java.util.Properties

internal data class AppVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
) {
    val name: String = "$major.${"%03d".format(minor)}.${"%03d".format(patch)}"
    val code: Int = (major * MAJOR_MULTIPLIER + minor * MINOR_MULTIPLIER + patch).coerceAtLeast(1)

    companion object {
        private const val MAJOR_MULTIPLIER = 1_000_000
        private const val MINOR_MULTIPLIER = 1_000
    }
}

internal fun Project.resolveAppVersion(): AppVersion {
    val props = Properties()
    val file = rootProject.file("version.properties")
    if (file.exists()) {
        file.inputStream().use(props::load)
    }
    val major = props.getProperty("MAJOR", "0").trim().toInt()
    val minor = props.getProperty("MINOR", "0").trim().toInt()
    val patch = gitCommitCount()
    return AppVersion(major = major, minor = minor, patch = patch)
}

private fun Project.gitCommitCount(): Int {
    val exec = providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
        workingDir = rootProject.rootDir
        isIgnoreExitValue = true
    }
    return try {
        exec.standardOutput.asText.get().trim().toIntOrNull() ?: 0
    } catch (_: Exception) {
        0
    }
}
