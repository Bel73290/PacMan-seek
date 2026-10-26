package com.cachecache.game.data

import android.util.Log
import com.cachecache.game.model.GeoPoint
import com.cachecache.game.model.LobbyPlayer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Real-time reads use Firestore; all game decisions are authenticated server calls. */
class GameRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val TAG = "GameRepository"

        const val STATUS_WAITING = "waiting"
        const val STATUS_STARTED = "started"
        const val STATUS_CANCELLED = "cancelled"
        const val STATUS_FINISHED = "finished"

        const val WINNER_PACMAN = "pacman"
        const val WINNER_GHOSTS = "ghosts"

        // Sans 0/O ni 1/I pour éviter les confusions à la saisie
        private const val CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }

    class GameNotFoundException : Exception("Aucune partie ne correspond à ce code.")
    class GameAlreadyStartedException : Exception("Cette partie a déjà commencé.")

    private fun games() = db.collection("games")

    /** Connexion anonyme : chaque téléphone obtient un identifiant unique, sans compte. */
    private suspend fun currentUserId(): String =
        auth.currentUser?.uid ?: auth.signInAnonymously().await().user!!.uid

    private fun defaultName(uid: String) = "Joueur ${uid.takeLast(4).uppercase()}"

    private val online = OnlineRepository()

    suspend fun createGame(codeLength: Int): String = online.call("createGame")["code"] as String
    suspend fun joinGame(code: String): Map<String, Any?> = online.call("joinGame", mapOf("code" to code))
    suspend fun startGame(code: String, hidingSeconds: Int, seekingSeconds: Int) {
        online.call("startGame", mapOf("code" to code, "hidingSeconds" to hidingSeconds, "seekingSeconds" to seekingSeconds))
    }
    suspend fun setReady(code: String, ready: Boolean) { online.call("setReady", mapOf("code" to code, "ready" to ready)) }
    suspend fun markGhostFound(code: String, ghostId: String) { online.call("captureGhost", mapOf("code" to code, "ghostId" to ghostId)) }
    suspend fun finishGame(code: String, winner: String) { online.call("finishGame", mapOf("code" to code)) }
    suspend fun serverTime(): Long = (online.call("serverTime")["now"] as Number).toLong()
    suspend fun updateMyLocation(code: String, location: GeoPoint, accuracy: Float, heading: Float, measuredAt: Long) {
        val uid = auth.currentUser?.uid ?: return
        games().document(code).collection("players").document(uid).update(mapOf(
            "lat" to location.latitude, "lng" to location.longitude, "accuracy" to accuracy.toDouble(),
            "heading" to heading.toDouble(), "measuredAt" to com.google.firebase.Timestamp(java.util.Date(measuredAt)), "locationUpdatedAt" to FieldValue.serverTimestamp()
        )).await()
    }
    suspend fun myUserId(): String = currentUserId()
    suspend fun leaveGame(code: String, isHost: Boolean) { online.call("leaveGame", mapOf("code" to code)) }

    fun observeStatus(code: String): Flow<String?> = callbackFlow {
        val registration = games().document(code).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.getString("status"))
        }
        awaitClose { registration.remove() }
    }

    fun observePlayers(code: String): Flow<List<LobbyPlayer>> = callbackFlow {
        val myId = auth.currentUser?.uid
        val registration = games().document(code).collection("players")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                // Tri côté appareil : un joinedAt encore en attente du serveur est null
                val players = snapshot?.documents.orEmpty()
                    .sortedBy { it.getTimestamp("joinedAt")?.seconds ?: Long.MAX_VALUE }
                    .map { doc ->
                    LobbyPlayer(
                        id = doc.id,
                        name = doc.getString("name") ?: defaultName(doc.id),
                        isHost = doc.getBoolean("isHost") == true,
                        isMe = doc.id == myId,
                        ready = doc.getBoolean("ready") == true
                    )
                }
                trySend(players)
            }
        awaitClose { registration.remove() }
    }

    data class GameSnapshot(
        val status: String?,
        val hostId: String?,
        val startedAtMillis: Long?,
        val hidingSeconds: Int,
        val seekingSeconds: Int,
        val foundGhostIds: Set<String>,
        val winner: String?
    )

    data class PlayerSnapshot(
        val id: String,
        val name: String,
        val isHost: Boolean,
        val location: GeoPoint?,
        val skinId: String = "ghost_red_classic",
        val pacmanSkin: String = "pacman_classic",
        val heading: Float = 0f,
        val active: Boolean = true,
        val accuracy: Float = 1000f,
        val locationUpdatedAt: Long = 0L
    )

    fun observeGame(code: String): Flow<GameSnapshot?> = callbackFlow {
        val registration = games().document(code).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot == null || !snapshot.exists()) {
                trySend(null)
                return@addSnapshotListener
            }
            trySend(
                GameSnapshot(
                    status = snapshot.getString("status"),
                    hostId = snapshot.getString("hostId"),
                    // ESTIMATE : chez l'hôte, l'heure serveur n'est pas encore connue juste après le lancement
                    startedAtMillis = snapshot.getTimestamp(
                        "startedAt",
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )?.toDate()?.time,
                    hidingSeconds = snapshot.getLong("hidingSeconds")?.toInt() ?: 180,
                    seekingSeconds = snapshot.getLong("seekingSeconds")?.toInt() ?: 540,
                    foundGhostIds = (snapshot.get("foundGhostIds") as? List<*>)
                        ?.filterIsInstance<String>()?.toSet().orEmpty(),
                    winner = snapshot.getString("winner")
                )
            )
        }
        awaitClose { registration.remove() }
    }

    /** Joueurs de la partie, dans l'ordre d'arrivée (identique sur tous les téléphones). */
    fun observeMatchPlayers(code: String): Flow<List<PlayerSnapshot>> = callbackFlow {
        val registration = games().document(code).collection("players")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val players = snapshot?.documents.orEmpty()
                    .sortedWith(
                        compareBy<DocumentSnapshot>(
                            { it.getTimestamp("joinedAt")?.seconds ?: Long.MAX_VALUE },
                            { it.getTimestamp("joinedAt")?.nanoseconds ?: 0 },
                            { it.id }
                        )
                    )
                    .map { doc ->
                        val lat = doc.getDouble("lat")
                        val lng = doc.getDouble("lng")
                        PlayerSnapshot(
                            id = doc.id,
                            name = doc.getString("name") ?: defaultName(doc.id),
                            isHost = doc.getBoolean("isHost") == true,
                            location = if (lat != null && lng != null) GeoPoint(lat, lng) else null,
                            skinId = doc.getString("ghostSkin") ?: "ghost_red_classic",
                            pacmanSkin = doc.getString("pacmanSkin") ?: "pacman_classic",
                            heading = (doc.getDouble("heading") ?: 0.0).toFloat(),
                            active = doc.getBoolean("active") != false,
                            accuracy = (doc.getDouble("accuracy") ?: 1000.0).toFloat(),
                            locationUpdatedAt = doc.getTimestamp("locationUpdatedAt")?.toDate()?.time ?: 0L
                        )
                    }
                trySend(players)
            }
        awaitClose { registration.remove() }
    }

    private fun playerData(uid: String, isHost: Boolean) = mapOf(
        "name" to defaultName(uid),
        "isHost" to isHost,
        "joinedAt" to FieldValue.serverTimestamp()
    )
}
