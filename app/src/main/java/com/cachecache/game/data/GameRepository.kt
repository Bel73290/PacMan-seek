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

/**
 * Parties multijoueur stockées dans Firestore :
 *   games/{code}                 -> hostId, status ("waiting" | "started" | "finished" | "cancelled"),
 *                                   createdAt, startedAt, hidingSeconds, seekingSeconds,
 *                                   foundGhostIds, winner ("pacman" | "ghosts")
 *   games/{code}/players/{uid}   -> name, isHost, joinedAt, lat, lng, locationUpdatedAt
 *
 * L'hôte (Pacman) est le seul à modifier le document de la partie ; chaque joueur
 * ne modifie que son propre document (voir firestore.rules).
 *
 * Hors ligne : Firestore garde une copie locale des parties (cache persistant activé par défaut
 * sur Android) et met les écritures en file d'attente. Pendant la partie, on n'attend donc jamais
 * la réponse du serveur : l'écriture est visible tout de suite en local et part dès que le
 * réseau revient.
 */
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

    suspend fun createGame(codeLength: Int): String {
        val uid = currentUserId()
        repeat(5) {
            val code = (1..codeLength).map { CODE_ALPHABET.random() }.joinToString("")
            val gameRef = games().document(code)
            val created = db.runTransaction { tx ->
                if (tx.get(gameRef).exists()) return@runTransaction false
                tx.set(
                    gameRef,
                    mapOf(
                        "hostId" to uid,
                        "status" to STATUS_WAITING,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(gameRef.collection("players").document(uid), playerData(uid, isHost = true))
                true
            }.await()
            if (created) return code
        }
        throw IllegalStateException("Impossible de générer un code de partie, réessaie.")
    }

    suspend fun joinGame(code: String) {
        val uid = currentUserId()
        val game = games().document(code).get().await()
        if (!game.exists()) throw GameNotFoundException()
        if (game.getString("status") != STATUS_WAITING) throw GameAlreadyStartedException()
        games().document(code).collection("players").document(uid)
            .set(playerData(uid, isHost = false)).await()
    }

    /** Lance la partie : les chronos de tous les téléphones partent de `startedAt` (heure serveur). */
    suspend fun startGame(code: String, hidingSeconds: Int, seekingSeconds: Int) {
        games().document(code).update(
            mapOf(
                "status" to STATUS_STARTED,
                "startedAt" to FieldValue.serverTimestamp(),
                "hidingSeconds" to hidingSeconds,
                "seekingSeconds" to seekingSeconds,
                "foundGhostIds" to emptyList<String>()
            )
        ).await()
    }

    /** Pacman (hôte) déclare un fantôme trouvé. Fonctionne hors ligne (envoi différé). */
    fun markGhostFound(code: String, ghostId: String) {
        games().document(code).update("foundGhostIds", FieldValue.arrayUnion(ghostId))
            .addOnFailureListener { Log.w(TAG, "Capture refusée par le serveur", it) }
    }

    /** Fonctionne hors ligne (envoi différé). */
    fun finishGame(code: String, winner: String) {
        games().document(code).update(mapOf("status" to STATUS_FINISHED, "winner" to winner))
            .addOnFailureListener { Log.w(TAG, "Fin de partie refusée par le serveur", it) }
    }

    /** Fonctionne hors ligne : seule la dernière position compte, Firestore regroupe les envois. */
    fun updateMyLocation(code: String, location: GeoPoint) {
        val uid = auth.currentUser?.uid ?: return
        games().document(code).collection("players").document(uid).update(
            mapOf(
                "lat" to location.latitude,
                "lng" to location.longitude,
                "locationUpdatedAt" to FieldValue.serverTimestamp()
            )
        ).addOnFailureListener { Log.w(TAG, "Position refusée par le serveur", it) }
    }

    suspend fun myUserId(): String = currentUserId()

    /** Quitte le salon ; si c'est l'hôte, la partie est annulée pour tout le monde. */
    suspend fun leaveGame(code: String, isHost: Boolean) {
        val uid = currentUserId()
        // Pas d'attente du serveur : hors ligne, le départ sera envoyé au retour du réseau
        if (isHost) {
            games().document(code).update("status", STATUS_CANCELLED)
        }
        games().document(code).collection("players").document(uid).delete()
    }

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
                        isMe = doc.id == myId
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
        val location: GeoPoint?
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
                            location = if (lat != null && lng != null) GeoPoint(lat, lng) else null
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
