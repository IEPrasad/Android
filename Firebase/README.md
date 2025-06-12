# Firebase Integration in Android Studio Project

This guide describes the necessary files and steps to connect a Firebase project with an Android Studio project.

---

## 1️⃣ Add Firebase SDK to the Project

### Edit `Project-level build.gradle` (`build.gradle`)

```groovy
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.google.gms:google-services:4.3.15' // Google Services plugin (latest version)'
    }
}
```

```groovy
## 2️⃣ Add Firebase Dependencies
### Edit App-level build.gradle (app/build.gradle)

plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services' // Apply the Google Services plugin
}

android {
    compileSdk 34

    defaultConfig {
        applicationId "com.example.myfirebaseapp"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }
}

dependencies {
    // Firebase BoM - Bill of Materials
    implementation platform('com.google.firebase:firebase-bom:32.3.1')  // Use latest version

    // Example Firebase dependencies
    implementation 'com.google.firebase:firebase-auth'        // Firebase Authentication
    implementation 'com.google.firebase:firebase-database'    // Realtime Database
    implementation 'com.google.firebase:firebase-firestore'   // Cloud Firestore
    implementation 'com.google.firebase:firebase-storage'     // Cloud Storage (optional)
}
```
