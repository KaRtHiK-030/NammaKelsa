package com.karthik.nammakelsa

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Cascade-deletes the current user's account:
 *   - profile doc in workers/hirers
 *   - users/{uid}
 *   - all reviews authored by user
 *   - all favorites under user
 *   - all requests where user is hirer
 *   - finally `FirebaseAuth.user.delete()`
 *
 * If `delete()` fails with RECENT_LOGIN_REQUIRED the caller should ask the user
 * to log in again and retry. We don't try to re-auth silently.
 */
fun deleteCurrentAccount(
    role: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser ?: return onError("Not signed in")
    val uid = user.uid
    val db = FirebaseFirestore.getInstance()
    val collection = if (role == "worker") "workers" else "hirers"

    fun deleteAuthUser() {
        user.delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                if (e is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                    onError("RECENT_LOGIN_REQUIRED")
                } else {
                    onError(e.localizedMessage ?: "Failed to delete account")
                }
            }
    }

    fun deleteCollectionDocs(query: com.google.firebase.firestore.Query, then: () -> Unit) {
        query.get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) { then(); return@addOnSuccessListener }
                var done = 0
                snap.documents.forEach { doc ->
                    doc.reference.delete().addOnCompleteListener {
                        done++
                        if (done == snap.documents.size) then()
                    }
                }
            }
            .addOnFailureListener { then() }
    }

    // Reviews (by user)
    deleteCollectionDocs(db.collection("reviews").whereEqualTo("userId", uid)) {
        // Requests (by hirer or worker)
        deleteCollectionDocs(db.collection("requests").whereEqualTo("hirerId", uid)) {
            deleteCollectionDocs(db.collection("requests").whereEqualTo("workerId", uid)) {
                // Favorites under hirer
                db.collection("hirers").document(uid).collection("favorites").get()
                    .addOnCompleteListener {
                        it.result?.documents?.forEach { fav -> fav.reference.delete() }
                        // Profile + users entry
                        db.collection(collection).document(uid).delete()
                            .addOnCompleteListener {
                                db.collection("users").document(uid).delete()
                                    .addOnCompleteListener { deleteAuthUser() }
                            }
                    }
            }
        }
    }
}
