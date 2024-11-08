plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "ca.yapper.yapperapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "ca.yapper.yapperapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.glide)
    implementation(libs.espresso.core)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.ext.junit)
    androidTestImplementation(libs.ext.junit)
    annotationProcessor(libs.compiler)

    // Use Firebase BoM to manage Firebase library versions
    implementation(platform(libs.firebase.bom))

    // Declare Firebase libraries without versions; BoM manages these
    implementation(libs.firebase.firestore)
    implementation(libs.google.firebase.messaging)

    implementation(libs.play.services.base)
    implementation(libs.core)
    implementation(libs.zxing.android.embedded)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    //testImplementation("junit:junit:4.13.2")
    testImplementation (libs.junit.jupiter.api.v501)
    androidTestImplementation(libs.junit.v115)
    androidTestImplementation(libs.espresso.core.v351)
    //androidTestImplementation ("androidx.test.espresso:espresso-contrib:3.6.1")

}


