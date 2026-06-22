import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}


val localProperties = Properties()
localProperties.load(rootProject.file("local.properties").inputStream())

android {
    namespace = "com.example.goodfood"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.goodfood"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "CLOUDINARY_CLOUD_NAME",
            "\"${localProperties.getProperty("CLOUDINARY_CLOUD_NAME")}\""
        )

        buildConfigField(
            "String",
            "CLOUDINARY_API_KEY",
            "\"${localProperties.getProperty("CLOUDINARY_API_KEY")}\""
        )

        buildConfigField(
            "String",
            "CLOUDINARY_API_SECRET",
            "\"${localProperties.getProperty("CLOUDINARY_API_SECRET")}\""
        )
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // dependencias para fireBase(fireStore, fireAuth)
    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    //conexión con glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    //conexión con cloudinary
    implementation("com.cloudinary:cloudinary-android:2.4.0")
    //biblioteca de google
    implementation("com.google.android.material:material:1.11.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}