import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
plugins { kotlin("multiplatform") }
kotlin {
    jvm()
    @OptIn(ExperimentalWasmDsl::class) wasmJs { browser() }
    sourceSets {
        commonMain {
            kotlin.srcDir("../vendor/kmp-application-foundation/core/src/commonMain/kotlin")
            
            dependencies {  }
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
