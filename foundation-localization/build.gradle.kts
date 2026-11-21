import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
plugins { kotlin("multiplatform") }
kotlin {
    jvm()
    @OptIn(ExperimentalWasmDsl::class) wasmJs { browser() }
    sourceSets {
        commonMain {
            kotlin.srcDir("../vendor/kmp-application-foundation/feature/localization/src/commonMain/kotlin")
            
            dependencies { api(project(":foundation-core")) }
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
