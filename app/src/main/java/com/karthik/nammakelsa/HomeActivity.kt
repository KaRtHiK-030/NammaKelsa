package com.karthik.nammakelsa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class HomeActivity : ComponentActivity() {

    private var presenceObserver: DefaultLifecycleObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val role = intent.getStringExtra("role") ?: "worker"

        // Track foreground/background to update online status against the right collection.
        val collection = if (role == "worker") "workers" else "hirers"
        presenceObserver = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = setOnline(collection, true)
            override fun onStop(owner: LifecycleOwner)  = setOnline(collection, false)
        }.also { ProcessLifecycleOwner.get().lifecycle.addObserver(it) }

        setContent {
            NammaKelsaTheme {
                MainScreen(role)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenceObserver?.let { ProcessLifecycleOwner.get().lifecycle.removeObserver(it) }
    }

    private fun setOnline(collection: String, online: Boolean) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection(collection).document(uid)
            .update("online", online)
    }
}
