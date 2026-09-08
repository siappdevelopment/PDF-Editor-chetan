plugins {
    alias(libs.plugins.android.application)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.reader.alldocviewer.pdfeditor"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.reader.alldocviewer.pdfeditor"
        minSdk = 24
        targetSdk = 37
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    lint {
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.browser:browser:1.8.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    //lifeCycleScopes
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")

    //UI size
    implementation("com.intuit.sdp:sdp-android:1.1.1")

    //Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-android-compiler:2.59.2")

    //for viewModels()
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    //Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    //Room Database
    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    //load lotti file
    implementation("com.airbnb.android:lottie:6.4.0")

    // PDF viewer (maintained fork with 16 KB native lib support)
    implementation("com.github.marain87:AndroidPdfViewer:3.2.7")
    implementation("org.greenrobot:eventbus:3.3.1")

    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    // iText 5 (Android-compatible build) for PDF password unlock, page organize, text extraction
    implementation("com.itextpdf:itextg:5.5.10")
    implementation("com.madgag.spongycastle:core:1.58.0.0")
    implementation("com.madgag.spongycastle:prov:1.58.0.0")

    // RoundedImageView dependency
    implementation("com.makeramen:roundedimageview:2.3.0")

    // ML Kit Document Scanner
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0")

    // SmartCropper
    implementation("com.moizhassan.smartcropper:smartcropper:2.1.5")

    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Firebase
    implementation ("com.google.firebase:firebase-config:21.6.0")
    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))
    implementation ("com.google.firebase:firebase-crashlytics")
    implementation ("com.google.firebase:firebase-analytics")
    implementation (libs.firebase.messaging)

    //Shimmer
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    //Google
    implementation ("com.google.android.gms:play-services-ads:24.9.0")
    implementation ("com.facebook.android:audience-network-sdk:6.+")

    //Mediation
    implementation ("com.google.ads.mediation:facebook:6.19.0.1")

    //Work Manager
    implementation("androidx.work:work-runtime:2.10.2")
    implementation("androidx.work:work-runtime-ktx:2.10.2")

    // Gson Converter
    api(libs.gson)

    // for encode decode
    implementation("com.sun.jersey:jersey-core:1.19.4")

    // App Update
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    implementation (libs.installreferrer)

}
