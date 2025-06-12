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

---
## 2️⃣ Add Firebase Dependencies
### Edit `App-level build.gradle` (`app/build.gradle`)

```groovy
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
---
Always check for the latest Firebase BoM version:
https://firebase.google.com/docs/android/setup#available-libraries
---

## 3️⃣ Add `google-services.json`
Go to Firebase Console → Project Settings → Your App → Download `google-services.json`.

Place the file into:

```groovy
app/google-services.json
```
---

## 4️⃣ Initialize Firebase in Code (Optional)

### In `MainActivity.java` or `MainActivity.kt`
#### For Java:

```groovy
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
    }
}
```
---
#### For Kotlin:

```groovy
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)
    }
}
```
---
Note: In most cases, Firebase initializes automatically using `google-services.json`.
---

## 5️⃣ Edit `AndroidManifest.xml`
### Ensure Internet permission is added:

```groovy
<uses-permission android:name="android.permission.INTERNET" />
```
---

## 6️⃣ (Optional) `Proguard Rules`
### If using Proguard or R8, add Firebase-specific rules in `proguard-rules.pro`.

```groovy
# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
```
---

## ✅ Summary of Files to Edit

| File                                  | Purpose                            |
| ------------------------------------- | ---------------------------------- |
| `build.gradle (Project)`              | Add Google Services classpath      |
| `build.gradle (App)`                  | Add Firebase dependencies & plugin |
| `google-services.json`                | Firebase configuration             |
| `MainActivity` or `Application` class | Optional initialization            |
| `AndroidManifest.xml`                 | Required permissions               |
| `proguard-rules.pro`                  | Optional (for release builds)      |

---
