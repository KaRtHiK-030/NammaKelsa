package com.karthik.nammakelsa

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings

class NammaKelsaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Enable Firestore offline persistence so the app stays usable on flaky networks.
        try {
            FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()
                )
                .build()
        } catch (_: IllegalStateException) {
            // Firestore was already used elsewhere with default settings; ignore.
        }
    }
}
