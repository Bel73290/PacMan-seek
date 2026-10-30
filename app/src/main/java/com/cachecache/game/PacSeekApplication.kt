package com.cachecache.game

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PacSeekApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG && BuildConfig.USE_FIREBASE_EMULATORS) {
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8188)
        }
    }
}
