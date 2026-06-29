package com.example.lifelab.app

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppIdentityGradleConfigurationTest {

    @Test
    fun appIdentityDefaultsAreDeclaredInGradleProperties() {
        val properties = readRootProperties("gradle.properties")

        assertEquals("com.study.lifelab", properties["LIFELAB_APPLICATION_ID"])
        assertEquals("3", properties["LIFELAB_VERSION_CODE"])
        assertEquals("1.1.1", properties["LIFELAB_VERSION_NAME"])
    }

    @Test
    fun appBuildReadsApplicationIdAndVersionFromGradleProperties() {
        val buildGradle = readRootText("app/build.gradle.kts")

        listOf(
            "LIFELAB_APPLICATION_ID",
            "LIFELAB_VERSION_CODE",
            "LIFELAB_VERSION_NAME",
        ).forEach { propertyName ->
            assertTrue(
                buildGradle.contains("""providers.gradleProperty("$propertyName")"""),
                "app/build.gradle.kts must read $propertyName through Gradle providers.",
            )
        }

        assertFalse(
            buildGradle.contains("""applicationId = "com.example.lifelab""""),
            "The release applicationId must not be hardcoded to the old example id.",
        )
    }

    @Test
    fun releaseSigningUsesOnlyGradlePropertiesForSecretsAndKeystore() {
        val buildGradle = readRootText("app/build.gradle.kts")

        listOf(
            "LIFELAB_RELEASE_STORE_FILE",
            "LIFELAB_RELEASE_STORE_PASSWORD",
            "LIFELAB_RELEASE_KEY_ALIAS",
            "LIFELAB_RELEASE_KEY_PASSWORD",
        ).forEach { propertyName ->
            assertTrue(
                buildGradle.contains("""providers.gradleProperty("$propertyName")"""),
                "Release signing must read $propertyName through Gradle providers.",
            )
        }

        listOf("storePassword", "keyAlias", "keyPassword").forEach { fieldName ->
            assertFalse(
                Regex("""$fieldName\s*=\s*"[^"]+"""").containsMatchIn(buildGradle),
                "Release signing must not hardcode $fieldName.",
            )
        }
        assertFalse(
            Regex("""storeFile\s*=\s*file\("([^"]+\.(jks|keystore|p12|pfx))"\)""").containsMatchIn(buildGradle),
            "Release signing must not hardcode a keystore file path.",
        )
    }

    @Test
    fun pullRequestWorkflowDoesNotBuildApk() {
        val workflow = readRootText(".github/workflows/android-ci.yml")

        assertTrue(workflow.contains("pull_request:"), "Android CI must keep a pull request trigger.")
        assertTrue(
            workflow.contains("github.event_name == 'workflow_dispatch' || startsWith(github.ref, 'refs/tags/v')"),
            "APK assembly must stay gated to manual workflow dispatch or v* tags.",
        )
        assertFalse(
            workflow.contains(":app:assembleDebug"),
            "CI must not upload debug APKs as the release artifact.",
        )
        assertTrue(
            workflow.contains(":app:assembleRelease"),
            "Manual/tag APK builds must use the release variant.",
        )
        assertTrue(
            workflow.contains("lifelab-release-apk"),
            "Release APK artifact should have the stable release artifact name.",
        )
    }

    private fun readRootText(path: String): String =
        String(Files.readAllBytes(projectRoot().resolve(path)), Charsets.UTF_8)

    private fun readRootProperties(path: String): Map<String, String> =
        readRootText(path)
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { line ->
                val separatorIndex = line.indexOf('=')
                if (separatorIndex == -1) {
                    null
                } else {
                    line.substring(0, separatorIndex).trim() to line.substring(separatorIndex + 1).trim()
                }
            }
            .toMap()

    private fun projectRoot(): Path {
        val cwd = Path.of("").toAbsolutePath()
        return generateSequence(cwd) { current -> current.parent }
            .first { current ->
                current.resolve("settings.gradle.kts").exists() &&
                    current.resolve("app/build.gradle.kts").exists()
            }
    }
}
