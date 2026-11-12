import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.apollo)
    alias(libs.plugins.ktlint)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    android {
        namespace = "com.chy.radio.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        // Compose resources (strings.xml) are packaged as Android resources.
        androidResources {
            enable = true
        }
        // Runs commonTest on the JVM: the `:shared:testAndroidHostTest` task.
        withHostTest {
            isIncludeAndroidResources = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            // Apollo picks its HTTP engine per platform: OkHttp on Android,
            // NSURLSession on iOS. No Ktor.
            implementation(libs.apollo.runtime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

// The Radio France API key, from local.properties (never committed). No key gives an
// empty string: the build still works (CI) and the app shows the missing-key message.
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val apiKey: String = localProperties.getProperty("RADIOFRANCE_API_KEY").orEmpty()

buildkonfig {
    packageName = "com.chy.radio.data"
    // The generated object is `internal`: the key never leaves the shared module.
    defaultConfigs {
        buildConfigField(STRING, "RADIOFRANCE_API_KEY", apiKey)
    }
}

apollo {
    service("radiofrance") {
        packageName = "com.chy.radio.data.graphql"
        // Generated models are `internal`: no Apollo type leaves the shared module.
        generateAsInternal = true

        // Run once by hand, then commit the schema (it costs 1 API request):
        // ./gradlew :shared:downloadRadiofranceApolloSchemaFromIntrospection
        // It sends the key as the `X-Token` header, the same transport as the app.
        introspection {
            endpointUrl = "https://openapi.radiofrance.fr/v1/graphql"
            headers.put("X-Token", apiKey)
            schemaFile = file("src/commonMain/graphql/schema.graphqls")
        }
    }
}

ktlint {
    version = libs.versions.ktlint
    // Skip generated sources (Compose resources, BuildKonfig, Apollo). We use
    // invariantSeparatorsPath because on Windows the raw path contains backslashes.
    filter { exclude { it.file.invariantSeparatorsPath.contains("/build/") } }
}
