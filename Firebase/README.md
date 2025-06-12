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


## 2️⃣ Add Firebase Dependencies
