plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

val camerax_version = "1.1.0"

android {
    namespace = "com.example.personalizedskincareproductsrecommendation"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.personalizedskincareproductsrecommendation"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
//        multiDexEnabled = true
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
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.android.car.ui:car-ui-lib:2.6.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth:23.0.0")
    implementation ("com.google.firebase:firebase-database:20.0.2")
    implementation ("com.google.android.material:material:1.8.0")
    implementation ("com.github.f0ris.sweetalert:library:1.5.6")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation ("com.google.firebase:firebase-storage:20.0.1")
    implementation ("androidx.drawerlayout:drawerlayout:1.0.0")

    implementation ("androidx.camera:camera-core:$camerax_version")
    implementation ("androidx.camera:camera-camera2:$camerax_version")
    implementation ("androidx.camera:camera-lifecycle:$camerax_version")
    implementation ("androidx.camera:camera-view:$camerax_version")

    implementation ("com.github.chrisbanes:PhotoView:2.3.0")

    // for password encryption
    implementation ("org.mindrot:jbcrypt:0.4")

    // for fetching product list
    implementation ("com.google.firebase:firebase-firestore:24.4.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    // For notification page
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("androidx.cardview:cardview:1.0.0")

    // For editProfile upload image
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    // For chart usage
    implementation ("com.github.PhilJay:MPAndroidChart:v3.0.3")

    // For model training usage
    implementation ("org.tensorflow:tensorflow-lite:2.9.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.3.1")


}