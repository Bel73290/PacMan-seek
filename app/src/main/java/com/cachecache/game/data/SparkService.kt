package com.cachecache.game.data

import com.cachecache.game.model.GeoPoint
import com.cachecache.game.viewmodel.GameViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Spark-only data operations. Every mutation is also checked by firestore.rules. */
class SparkService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val stamp get() = FieldValue.serverTimestamp()
    private val uid get() = checkNotNull(auth.currentUser?.uid) { "Connecte-toi pour continuer." }
    private fun user(id: String = uid) = db.collection("users").document(id)
    private fun publicUser(id: String = uid) = db.collection("players").document(id)
    private fun game(code: String) = db.collection("games").document(code)
    private fun code(data: Map<String, Any?>): String = (data["code"] as? String).orEmpty().trim().uppercase(Locale.ROOT).also {
        require(it.matches(Regex("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}"))) { "Code de partie invalide." }
    }
    private fun peer(data: Map<String, Any?>) = (data["uid"] as? String).orEmpty().also {
        require(it.matches(Regex("[A-Za-z0-9_-]{1,128}"))) { "Identifiant joueur invalide." }
    }
    private fun DocumentSnapshot.int(key: String) = getLong(key)?.toInt() ?: 0
    private fun DocumentSnapshot.strings(key: String) = (get(key) as? List<*>)?.filterIsInstance<String>().orEmpty()
    private fun modern(g: DocumentSnapshot) {
        check(g.exists()) { "Aucune partie ne correspond à ce code." }
        check(g.getLong("schemaVersion") == 2L) { "Cette ancienne partie est conservée dans l’historique. Crée une nouvelle partie pour jouer avec cette version." }
    }
    private fun member(g: DocumentSnapshot) { modern(g); check(uid in g.strings("memberIds")) { "Tu ne fais pas partie de ce salon." } }
    private fun publicData(p: Map<String, Any?>) = mapOf(
        "name" to p["name"], "friendCode" to p["friendCode"], "score" to p["score"],
        "level" to (1 + ((p["xp"] as? Number)?.toInt() ?: 0) / 500),
        "ghostSkin" to p["ghostSkin"], "pacmanSkin" to p["pacmanSkin"]
    )
    private fun player(p: DocumentSnapshot, host: Boolean) = mapOf(
        "name" to p.getString("name"), "ghostSkin" to p.getString("ghostSkin"), "pacmanSkin" to p.getString("pacmanSkin"),
        "isHost" to host, "ready" to host, "active" to true, "joinedAt" to stamp, "heading" to 0
    )
    private fun season(date: Date = Date()) = SimpleDateFormat("yyyy-MM", Locale.ROOT).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(date)

    suspend fun execute(name: String, data: Map<String, Any?> = emptyMap()): Map<String, Any?> = when (name) {
        "ensureProfile" -> { ensureProfile(); mapOf("uid" to uid) }
        "saveProfile" -> { saveName(data["name"] as? String ?: ""); mapOf("ok" to true) }
        "purchaseSkin" -> { purchase(data["skinId"] as? String ?: ""); mapOf("ok" to true) }
        "equipSkin" -> { equip(data["skinId"] as? String ?: ""); mapOf("ok" to true) }
        "claimBattlePass" -> { claimPass(); mapOf("ok" to true) }
        "createGame" -> mapOf("code" to createGame())
        "joinGame" -> joinGame(code(data))
        "setReady" -> { ready(code(data), data["ready"] == true); mapOf("ok" to true) }
        "startGame" -> { start(code(data), (data["hidingSeconds"] as? Number)?.toInt() ?: 180, (data["seekingSeconds"] as? Number)?.toInt() ?: 540); mapOf("ok" to true) }
        "captureGhost" -> { capture(code(data), data["ghostId"] as? String ?: ""); mapOf("ok" to true) }
        "finishGame" -> { finish(code(data)); claimResult(code(data)); mapOf("ok" to true) }
        "claimResult" -> { claimResult(code(data)); mapOf("ok" to true) }
        "leaveGame" -> { leave(code(data)); mapOf("ok" to true) }
        "serverTime" -> mapOf("now" to serverTime())
        "leaderboard" -> leaderboard(data)
        "sendFriendRequest" -> { requestFriend(data["friendCode"] as? String ?: ""); mapOf("ok" to true) }
        "respondFriendRequest" -> { respond(peer(data), data["accept"] == true); mapOf("ok" to true) }
        "removeFriend" -> { removeFriend(peer(data)); mapOf("ok" to true) }
        "inviteFriend" -> { invite(peer(data), code(data)); mapOf("ok" to true) }
        else -> error("Action inconnue.")
    }

    suspend fun ensureProfile() {
        val id = uid
        val friendCode = ByteArray(6).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02X".format(it.toInt() and 255) }
        db.runTransaction { tx ->
            val ref = user(id); val existing = tx.get(ref)
            if (!existing.exists()) {
                val codeRef = db.collection("friendCodes").document(friendCode)
                check(!tx.get(codeRef).exists()) { "Réessaie pour créer ton profil." }
                val p = mapOf("name" to "Joueur ${friendCode.take(4)}", "friendCode" to friendCode,
                    "score" to 0, "coins" to 0, "xp" to 0, "games" to 0, "victories" to 0, "captures" to 0,
                    "ownedSkins" to listOf("ghost_red_classic", "pacman_classic"), "ghostSkin" to "ghost_red_classic", "pacmanSkin" to "pacman_classic", "createdAt" to stamp)
                tx.set(ref, p); tx.set(publicUser(id), publicData(p)); tx.set(codeRef, mapOf("uid" to id))
            }
        }.await()
        // Old games stay untouched. An abandoned pointer is cleared only on the current player's profile.
        val p = user().get(Source.SERVER).await()
        val active = p.getString("activeGame") ?: return
        val g = game(active).get(Source.SERVER).await()
        if (g.getLong("schemaVersion") != 2L || g.getString("status") == "cancelled" || uid !in g.strings("activePlayerIds")) clearActive(active)
        else if (g.getString("status") == "finished") claimResult(active)
        else if (g.getString("status") == "started") {
            val end = (g.getTimestamp("startedAt")?.toDate()?.time ?: Long.MAX_VALUE) + (g.int("hidingSeconds") + g.int("seekingSeconds")) * 1000L
            if (serverTime() >= end) { finish(active); claimResult(active) }
        }
    }

    private suspend fun saveName(raw: String) {
        val name = java.text.Normalizer.normalize(raw.trim(), java.text.Normalizer.Form.NFC)
        require(name.length in 2..24 && name.none { it.isISOControl() }) { "Le pseudo doit contenir entre 2 et 24 caractères." }
        db.runTransaction { tx ->
            val ref = user(); val p = tx.get(ref); check(p.exists()) { "Profil indisponible." }
            tx.update(ref, "name", name); tx.set(publicUser(), publicData(p.data.orEmpty() + ("name" to name)))
        }.await()
    }
    private suspend fun purchase(id: String) {
        val skin = MockData.AvatarItemsCatalog.firstOrNull { it.id == id } ?: error("Skin inconnu.")
        check(MockData.BattlePassTiers.none { it.avatarId == id }) { "Ce skin se débloque dans le pass de combat." }
        db.runTransaction { tx ->
            val ref = user(); val p = tx.get(ref); val owned = p.strings("ownedSkins")
            if (id !in owned) {
                check(p.int("coins") >= skin.price) { "Pas assez de pièces." }
                tx.update(ref, mapOf("coins" to p.int("coins") - skin.price, "ownedSkins" to owned + id,
                    "lastAction" to mapOf("kind" to "purchase", "skinId" to id)))
            }
        }.await()
    }
    private suspend fun equip(id: String) {
        check(MockData.AvatarItemsCatalog.any { it.id == id }) { "Skin inconnu." }
        db.runTransaction { tx ->
            val ref = user(); val p = tx.get(ref); check(id in p.strings("ownedSkins")) { "Ce skin ne fait pas partie de ta collection." }
            val active = p.getString("activeGame"); val g = active?.let { tx.get(game(it)) }
            val field = if (id.startsWith("pacman_")) "pacmanSkin" else "ghostSkin"
            tx.update(ref, field, id); tx.set(publicUser(), publicData(p.data.orEmpty() + (field to id)))
            if (g != null && g.getLong("schemaVersion") == 2L && uid in g.strings("activePlayerIds") && g.getString("status") in listOf("waiting", "started"))
                tx.update(g.reference.collection("players").document(uid), field, id)
        }.await()
    }
    private suspend fun claimPass() {
        val current=user().get(Source.SERVER).await()
        for (tier in MockData.BattlePassTiers.filter{it.level<=1+current.int("xp")/500 && it.avatarId !in current.strings("ownedSkins")}) db.runTransaction { tx ->
            val ref = user(); val p = tx.get(ref); val owned = p.strings("ownedSkins")
            if (1 + p.int("xp") / 500 >= tier.level && tier.avatarId !in owned) tx.update(ref, mapOf(
                "ownedSkins" to owned + tier.avatarId, "lastAction" to mapOf("kind" to "pass", "skinId" to tier.avatarId)))
        }.await()
    }
    private fun previous(tx: Transaction, p: DocumentSnapshot) {
        val active = p.getString("activeGame") ?: return
        val old = tx.get(game(active))
        check(old.getLong("schemaVersion") != 2L || old.getString("status") !in listOf("waiting", "started")) { "Tu as déjà une partie : $active. Reprends-la ou quitte-la." }
    }
    private suspend fun createGame(): String {
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; val random = SecureRandom()
        repeat(5) {
            val code = (1..6).map { alphabet[random.nextInt(alphabet.length)] }.joinToString("")
            val created = db.runTransaction { tx ->
                val ref = game(code); val g = tx.get(ref); val p = tx.get(user())
                if (g.exists()) false else {
                    previous(tx, p)
                    tx.set(ref, mapOf("schemaVersion" to 2, "hostId" to uid, "status" to "waiting", "memberIds" to listOf(uid),
                        "activePlayerIds" to listOf(uid), "readyIds" to listOf(uid), "foundGhostIds" to emptyList<String>(), "createdAt" to stamp,
                        "hidingSeconds" to 180, "seekingSeconds" to 540))
                    tx.set(ref.collection("players").document(uid), player(p, true)); tx.update(user(), "activeGame", code); true
                }
            }.await()
            if (created) return code
        }
        error("Impossible de créer un code. Réessaie.")
    }
    private suspend fun joinGame(code: String) = db.runTransaction { tx ->
        val ref = game(code); val g = tx.get(ref); modern(g)
        val p = tx.get(user()); val existing = tx.get(ref.collection("players").document(uid))
        val members = g.strings("memberIds")
        if (g.getString("status") == "started" && uid in g.strings("activePlayerIds") && existing.getBoolean("active") != false)
            mapOf("code" to code, "isHost" to (g.getString("hostId") == uid), "started" to true)
        else {
            check(g.getString("status") == "waiting") { "Cette partie est déjà lancée ou terminée." }
            check(members.size < 12 || uid in members) { "Ce salon est complet." }
            if (p.getString("activeGame") != code) previous(tx, p)
            if (uid !in members) {
                tx.update(ref, mapOf("memberIds" to members + uid, "activePlayerIds" to g.strings("activePlayerIds") + uid))
                tx.set(ref.collection("players").document(uid), player(p, false))
            }
            tx.update(user(), "activeGame", code)
            mapOf("code" to code, "isHost" to (g.getString("hostId") == uid), "started" to false)
        }
    }.await()

    private suspend fun ready(code: String, ready: Boolean) {
        db.runTransaction { tx ->
            val ref = game(code); val g = tx.get(ref); member(g)
            check(g.getString("status") == "waiting") { "Le salon est fermé." }
            val ids = g.strings("readyIds").filterNot { it == uid } + if (ready) listOf(uid) else emptyList()
            tx.update(ref, "readyIds", ids); tx.update(ref.collection("players").document(uid), "ready", ready)
        }.await()
    }
    private suspend fun start(code: String, hide: Int, seek: Int) {
        require(hide in 30..600 && seek in 60..3600) { "Durées invalides." }
        db.runTransaction { tx ->
            val ref = game(code); val g = tx.get(ref); member(g)
            check(g.getString("hostId") == uid && g.getString("status") == "waiting") { "Seul l’hôte peut lancer le salon." }
            check(g.strings("memberIds").size >= 2 && g.strings("readyIds").toSet() == g.strings("memberIds").toSet()) { "Tous les joueurs doivent être prêts." }
            tx.update(ref, mapOf("status" to "started", "startedAt" to stamp, "hidingSeconds" to hide, "seekingSeconds" to seek))
        }.await()
    }
    private suspend fun capture(code: String, target: String) {
        db.runTransaction { tx ->
            val ref = game(code); val g = tx.get(ref); member(g)
            check(g.getString("hostId") == uid && target != uid && target in g.strings("activePlayerIds")) { "Capture interdite." }
            if (target !in g.strings("foundGhostIds")) {
                val host = tx.get(ref.collection("players").document(uid)); val ghost = tx.get(ref.collection("players").document(target))
                fun point(p: DocumentSnapshot) = GeoPoint(p.getDouble("lat") ?: error("Position GPS absente."), p.getDouble("lng") ?: error("Position GPS absente."))
                check(GameViewModel.calculateDistanceMeters(point(host), point(ghost)) <= 10.0) { "Rapproche-toi du fantôme." }
                tx.update(ref, mapOf("foundGhostIds" to g.strings("foundGhostIds") + target, "lastCapture" to target))
            }
        }.await()
    }
    private suspend fun finish(code: String) {
        db.runTransaction { tx ->
            val ref = game(code); val g = tx.get(ref); member(g)
            if (g.getString("status") == "started") {
                val ghosts = g.strings("activePlayerIds").filterNot { it == g.getString("hostId") }
                val all = ghosts.isNotEmpty() && g.strings("foundGhostIds").containsAll(ghosts)
                tx.update(ref, mapOf("status" to "finished", "winner" to if (all) "pacman" else "ghosts", "finishedAt" to stamp))
            }
        }.await()
    }
    suspend fun clearActive(code: String) {
        db.runTransaction { tx -> val p = tx.get(user()); if (p.getString("activeGame") == code) tx.update(user(), "activeGame", FieldValue.delete()) }.await()
    }
    private suspend fun leave(code: String) {
        db.runTransaction { tx ->
            val ref = game(code); val g = tx.get(ref); member(g); val p = tx.get(user())
            val status = g.getString("status")
            if (status in listOf("waiting", "started")) {
                if (g.getString("hostId") == uid || (status == "started" && g.strings("activePlayerIds").size <= 2))
                    tx.update(ref, "status", "cancelled")
                else {
                    val changes = mutableMapOf<String, Any>("activePlayerIds" to g.strings("activePlayerIds").filterNot { it == uid })
                    if (status == "waiting") { changes["memberIds"] = g.strings("memberIds").filterNot { it == uid }; changes["readyIds"] = g.strings("readyIds").filterNot { it == uid } }
                    tx.update(ref, changes)
                }
                if (status == "waiting") tx.delete(ref.collection("players").document(uid))
                else tx.update(ref.collection("players").document(uid), "active", false)
            }
            if (p.getString("activeGame") == code) tx.update(user(), "activeGame", FieldValue.delete())
        }.await()
    }
    suspend fun claimResult(code: String) {
        val month = season(Date(serverTime()))
        db.runTransaction { tx ->
            val ref = user(); val p = tx.get(ref); val g = tx.get(game(code))
            val receipt = ref.collection("history").document(code); val previous = tx.get(receipt)
            val scoreRef = db.collection("seasons").document(month).collection("scores").document(uid); val oldSeason = tx.get(scoreRef)
            if (previous.exists()) {
                if (p.getString("activeGame") == code) tx.update(ref, "activeGame", FieldValue.delete())
                return@runTransaction
            }
            if (g.getLong("schemaVersion") != 2L || g.getString("status") != "finished" || uid !in g.strings("activePlayerIds")) return@runTransaction
            val host = uid == g.getString("hostId")
            val won = if (host) g.getString("winner") == "pacman" else g.getString("winner") == "ghosts" && uid !in g.strings("foundGhostIds")
            val captures = if (host) g.strings("foundGhostIds").size else 0
            val score = 20 + captures * 50 + if (won) 100 else 0
            val xp = 50 + captures * 25 + if (won) 100 else 0
            val coins = 25 + captures * 10 + if (won) 50 else 0
            val updated = p.data.orEmpty() + mapOf("score" to p.int("score") + score, "coins" to p.int("coins") + coins, "xp" to p.int("xp") + xp,
                "games" to p.int("games") + 1, "victories" to p.int("victories") + if (won) 1 else 0, "captures" to p.int("captures") + captures)
            tx.update(ref, updated.filterKeys { it in setOf("score", "coins", "xp", "games", "victories", "captures") } + mapOf(
                "activeGame" to FieldValue.delete(), "lastAction" to mapOf("kind" to "reward", "code" to code, "season" to month)))
            tx.set(publicUser(), publicData(updated))
            tx.set(receipt, mapOf("code" to code, "won" to won, "score" to score, "coins" to coins, "xp" to xp,
                "role" to if (host) "pacman" else "ghost", "at" to stamp, "season" to month))
            tx.set(scoreRef, publicData(updated) + ("score" to oldSeason.int("score") + score))
        }.await()
        claimPass()
    }

    private suspend fun serverTime(): Long {
        val ref = user().collection("clock").document("current")
        ref.set(mapOf("at" to stamp)).await()
        return ref.get(Source.SERVER).await().getTimestamp("at")!!.toDate().time
    }
    private suspend fun requestFriend(raw: String) {
        val code = raw.trim().uppercase(Locale.ROOT); require(code.matches(Regex("[A-F0-9]{12}"))) { "Code ami invalide." }
        val target = db.collection("friendCodes").document(code).get(Source.SERVER).await().getString("uid") ?: error("Joueur introuvable.")
        check(target != uid) { "C’est ton propre code." }
        val p = user().get().await()
        user(target).collection("requests").document(uid).set(mapOf("uid" to uid, "name" to p.getString("name"), "at" to stamp)).await()
    }
    private suspend fun respond(other: String, accept: Boolean) {
        db.runTransaction { tx ->
            val request = user().collection("requests").document(other); check(tx.get(request).exists()) { "Cette demande n’existe plus." }
            val me = tx.get(publicUser()); val peer = tx.get(publicUser(other))
            if (accept) {
                tx.set(user().collection("friends").document(other), mapOf("uid" to other, "name" to peer.getString("name")))
                tx.set(user(other).collection("friends").document(uid), mapOf("uid" to uid, "name" to me.getString("name")))
            }
            tx.delete(request)
        }.await()
    }
    private suspend fun removeFriend(other: String) {
        db.batch().apply { delete(user().collection("friends").document(other)); delete(user(other).collection("friends").document(uid)) }.commit().await()
    }
    private suspend fun invite(other: String, code: String) {
        val p = user().get().await()
        user(other).collection("invites").document(code).set(mapOf("code" to code, "from" to p.getString("name"), "senderId" to uid, "at" to stamp)).await()
    }
    private suspend fun leaderboard(data: Map<String, Any?>): Map<String, Any?> {
        val scope = data["scope"] as? String ?: "general"
        val collection = if (scope == "season") db.collection("seasons").document(season()).collection("scores") else db.collection("players")
        fun row(doc: DocumentSnapshot) = mapOf("uid" to doc.id) + doc.data.orEmpty()
        if (scope == "friends") {
            val friends = user().collection("friends").limit(200).get().await().documents.map { it.id }
            val entries = (listOf(uid) + friends).chunked(30).flatMap { collection.whereIn(FieldPath.documentId(), it).get().await().documents.map(::row) }
                .sortedWith(compareByDescending<Map<String, Any?>> { (it["score"] as? Number)?.toInt() ?: 0 }.thenBy { it["uid"].toString() })
            return mapOf("entries" to entries, "rank" to entries.indexOfFirst { it["uid"] == uid } + 1, "hasMore" to false)
        }
        var query = collection.orderBy("score", Query.Direction.DESCENDING).orderBy(FieldPath.documentId()).limit(50)
        val cursor = data["cursor"] as? Map<*, *>
        if (cursor?.get("uid") is String && cursor["score"] is Number) query = query.startAfter(cursor["score"], cursor["uid"])
        val rows = query.get().await().documents
        val me = collection.document(uid).get().await()
        val rank = if (me.exists()) {
            val above = collection.whereGreaterThan("score", me.int("score")).count().get(AggregateSource.SERVER).await().count
            val ties = collection.whereEqualTo("score", me.int("score")).whereLessThan(FieldPath.documentId(), uid).count().get(AggregateSource.SERVER).await().count
            (above + ties + 1).toInt()
        } else null
        return mapOf("entries" to rows.map(::row), "rank" to rank, "hasMore" to (rows.size == 50),
            "cursor" to rows.lastOrNull()?.let { mapOf("uid" to it.id, "score" to it.int("score")) })
    }
}
