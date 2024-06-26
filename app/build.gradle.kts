plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.daniel.apibusunhevalconductor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.daniel.apibusunhevalconductor"
        minSdk = 23
        targetSdk = 33
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")


    implementation("com.beust:klaxon:5.5")
    implementation(platform("com.google.firebase:firebase-bom:30.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx:21.0.5")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation ("com.google.firebase:firebase-analytics-ktx")

    implementation("com.google.maps.android:maps-ktx:3.2.0")
    implementation("com.google.maps.android:maps-utils-ktx:3.2.0")
    implementation("com.google.maps.android:android-maps-utils:2.2.3")
    implementation("com.google.android.gms:play-services-maps:18.0.2")
    implementation("com.google.android.gms:play-services-location:20.0.0")
    implementation("com.google.maps.android:places-ktx:2.0.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.dhaval2404:imagepicker:2.1")

    implementation ("com.github.prabhat1707:EasyWayLocation:2.4")
    implementation ("com.github.imperiumlabs:GeoFirestore-Android:v1.4.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.airbnb.android:lottie:5.2.0")
    implementation("androidx.activity:activity:1.9.0")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}