import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
plugins { kotlin("multiplatform"); id("org.jetbrains.kotlin.plugin.compose"); id("org.jetbrains.compose") }
kotlin {
    jvm()
    @OptIn(ExperimentalWasmDsl::class) wasmJs { browser() }
    sourceSets {
        commonMain {
            kotlin.srcDir("../vendor/kmp-application-foundation/ui/src/commonMain/kotlin")
            kotlin.exclude("**/ViewId.kt", "**/BindLifecycle.kt")
            dependencies { api(project(":foundation-localization")); api(compose.material3); implementation(compose.components.resources) }
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
