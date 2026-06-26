package com.example.lifelab.app

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.assertTrue

class AndroidManifestConfigurationTest {

    @Test
    fun launcherManifestDeclaresAppIconsBackedByResources() {
        val manifest = readMainText("AndroidManifest.xml")

        assertTrue(
            manifest.contains("""android:icon="@mipmap/ic_launcher""""),
            "Application manifest must declare the launcher icon resource.",
        )
        assertTrue(
            manifest.contains("""android:roundIcon="@mipmap/ic_launcher_round""""),
            "Application manifest must declare the round launcher icon resource.",
        )

        listOf(
            "res/mipmap-anydpi-v26/ic_launcher.xml",
            "res/mipmap-anydpi-v26/ic_launcher_round.xml",
            "res/drawable-nodpi/ic_launcher_foreground.png",
            "res/drawable-nodpi/ic_launcher_monochrome.png",
            "res/values/icon_colors.xml",
        ).forEach { path ->
            assertTrue(mainSourcePath(path).exists(), "Missing launcher icon resource: $path")
        }
    }

    @Test
    fun appCompatActivityUsesAppCompatTheme() {
        val mainActivity = readMainText("java/com/example/lifelab/app/MainActivity.kt")
        val themes = readMainText("res/values/themes.xml")

        assertTrue(
            mainActivity.contains("AppCompatActivity"),
            "This check is tied to MainActivity using AppCompatActivity.",
        )
        assertTrue(
            themes.contains("""parent="Theme.AppCompat"""),
            "AppCompatActivity must use a Theme.AppCompat descendant to avoid launch crashes.",
        )
    }

    private fun readMainText(path: String): String = Files.readString(mainSourcePath(path))

    private fun mainSourcePath(path: String): Path {
        val cwd = Path.of("").toAbsolutePath()
        val appRoot = generateSequence(cwd) { current -> current.parent }
            .first { current ->
                current.name == "app" && current.resolve("src/main/AndroidManifest.xml").exists() ||
                    current.resolve("app/src/main/AndroidManifest.xml").exists()
            }

        val mainRoot = if (appRoot.name == "app") appRoot.resolve("src/main") else appRoot.resolve("app/src/main")
        return mainRoot.resolve(path)
    }
}
