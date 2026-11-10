import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "mobi.librera.libgoogleFree"
    compileSdk = libs.versions.compileSdk.get().toInt()



    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
}

dependencies {

    //api("androidx.annotation:annotation:1.10.0")

    //implementation(libs.billing)

}
