package com.cachecache.game

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.android.gms.games.PlayGamesSdk

class PacSeekApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG && BuildConfig.USE_FIREBASE_EMULATORS) {
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8188)
            FirebaseFunctions.getInstance("europe-west1").useEmulator("10.0.2.2", 5001)
        }
        if (getString(R.string.play_games_project_id) != "0") PlayGamesSdk.initialize(this)
    }
}
