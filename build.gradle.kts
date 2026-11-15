import java.net.URI
import java.security.MessageDigest
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    kotlin("multiplatform") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"
    id("org.jetbrains.compose") version "1.8.2"
}

composeCompiler { targetKotlinPlatforms.set(setOf(KotlinPlatformType.wasm)) }

// Bundle the pinned OFL font so Japanese glyphs also render on a fresh Wasm canvas.
val fontResources = layout.buildDirectory.dir("generated/storeFonts")
val prepareStoreFont by tasks.registering {
    val source = "https://raw.githubusercontent.com/google/fonts/cc39c4c2c304bc368c9b7c311ea2bcc6013299fe/ofl/notosansjp/NotoSansJP%5Bwght%5D.ttf"
    val checksum = "c2f3b4d463500a2ddcd3849cded1fceeb9fd6d1c32e6cbecd568453ba50fc68f"
    inputs.property("sha256", checksum)
    val target = fontResources.map { it.file("fonts/NotoSansJP.ttf") }
    outputs.file(target)
    doLast {
        val connection = URI(source).toURL().openConnection()
        connection.connectTimeout = 15000
        connection.readTimeout = 60000
        val bytes = connection.getInputStream().use { it.readBytes() }
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        check(digest == checksum) { "Noto Sans JP checksum mismatch" }
        val file = target.get().asFile
        file.parentFile.mkdirs()
        file.writeBytes(bytes)
    }
}

kotlin {
    jvm() // Runs shared order rules on the JVM.
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser { commonWebpackConfig { outputFileName = "store.js" } }
        binaries.executable()
    }
    sourceSets {
        wasmJsMain.get().resources.srcDir(prepareStoreFont.map { fontResources.get() })
        commonMain.dependencies {
            implementation(project(":b3-foundation"))
            implementation(project(":order-application"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
        }
        commonTest.dependencies { implementation(kotlin("test")) }
        wasmJsMain.dependencies {
            implementation(project(":b3-ui"))
            implementation(compose.material3)
            implementation("io.ktor:ktor-client-js:3.1.3")
        }
    }
}
