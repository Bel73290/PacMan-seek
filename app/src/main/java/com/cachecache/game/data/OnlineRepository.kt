package com.cachecache.game.data

import android.app.Activity
import com.cachecache.game.BuildConfig
import com.cachecache.game.R
import com.google.android.gms.games.PlayGames
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PlayGamesAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OnlineRepository {
    val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance("europe-west1")

    suspend fun call(name: String, data: Map<String, Any?> = emptyMap()): Map<String, Any?> {
        @Suppress("UNCHECKED_CAST")
        return functions.getHttpsCallable(name).call(data).await().getData() as? Map<String, Any?> ?: emptyMap()
    }

    suspend fun initialize() {
        if (auth.currentUser == null) auth.signInAnonymously().await()
        call("ensureProfile")
    }

    class ExistingPlayAccount : Exception("Ce compte Play Jeux possède déjà un profil. Tu peux ouvrir ce profil ; la progression invitée ne sera pas fusionnée.")

    suspend fun connectPlayGames(activity: Activity, openExisting: Boolean = false) {
        check(activity.getString(R.string.play_games_project_id) != "0" && BuildConfig.PLAY_GAMES_WEB_CLIENT_ID.isNotBlank()) {
            "La connexion Play Jeux doit être configurée pour cette version de l’application."
        }
        val client = PlayGames.getGamesSignInClient(activity)
        check(client.signIn().await().isAuthenticated) { "Connexion Play Jeux annulée." }
        val code = client.requestServerSideAccess(BuildConfig.PLAY_GAMES_WEB_CLIENT_ID, false).await()
        val credential = PlayGamesAuthProvider.getCredential(code)
        try {
            val user = auth.currentUser
            if (user?.isAnonymous == true && !openExisting) user.linkWithCredential(credential).await()
            else auth.signInWithCredential(credential).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            throw ExistingPlayAccount()
        }
        call("ensureProfile")
    }

    fun profile(uid: String) = callbackFlow {
        val registration = db.collection("users").document(uid).addSnapshotListener { doc, error ->
            if (error != null) close(error) else doc?.data?.let { trySend(it) }
        }
        awaitClose { registration.remove() }
    }

    fun list(uid: String, collection: String, ordered: Boolean = false) = callbackFlow {
        val base = db.collection("users").document(uid).collection(collection)
        val query = if (ordered) base.orderBy("at", Query.Direction.DESCENDING).limit(30) else base.limit(200)
        val registration = query.addSnapshotListener { docs, error ->
            if (error != null) close(error) else trySend(docs?.documents.orEmpty().map { mapOf("id" to it.id) + it.data.orEmpty() })
        }
        awaitClose { registration.remove() }
    }
}
