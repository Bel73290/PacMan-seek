package com.cachecache.game.data

import android.app.Activity
import com.cachecache.game.BuildConfig
import com.cachecache.game.R
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OnlineRepository {
    val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val spark = SparkService(auth,db)

    suspend fun call(name: String, data: Map<String, Any?> = emptyMap()): Map<String, Any?> {
        return spark.execute(name,data)
    }

    suspend fun initialize() {
        if (auth.currentUser == null) auth.signInAnonymously().await()
        call("ensureProfile")
    }

    class ExistingGoogleAccount : Exception("Ce compte Google possède déjà un profil. Tu peux ouvrir ce profil ; la progression invitée ne sera pas fusionnée.")

    suspend fun connectGoogle(activity: Activity, openExisting: Boolean = false) {
        val resource=activity.resources.getIdentifier("default_web_client_id","string",activity.packageName)
        val clientId=BuildConfig.GOOGLE_WEB_CLIENT_ID.ifBlank{if(resource!=0)activity.getString(resource) else ""}
        check(clientId.isNotBlank()) { "La connexion Google doit être activée dans Firebase et le fichier google-services.json actualisé." }
        val option=GetSignInWithGoogleOption.Builder(clientId).build()
        val request=GetCredentialRequest.Builder().addCredentialOption(option).build()
        val result=CredentialManager.create(activity).getCredential(activity,request).credential
        check(result is CustomCredential && result.type==GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) { "Connexion Google annulée." }
        val token=GoogleIdTokenCredential.createFrom(result.data).idToken
        val credential=GoogleAuthProvider.getCredential(token,null)
        try {
            val user = auth.currentUser
            if (user?.isAnonymous == true && !openExisting) user.linkWithCredential(credential).await()
            else auth.signInWithCredential(credential).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            throw ExistingGoogleAccount()
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
