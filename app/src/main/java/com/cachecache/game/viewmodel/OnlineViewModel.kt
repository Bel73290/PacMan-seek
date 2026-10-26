package com.cachecache.game.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cachecache.game.data.OnlineRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnlineState(
    val uid: String = "", val guest: Boolean = true, val loading: Boolean = true, val busy: Boolean = false,
    val error: String? = null, val message: String? = null, val existingAccount: Boolean = false,
    val profile: Map<String, Any?> = emptyMap(), val history: List<Map<String, Any?>> = emptyList(),
    val friends: List<Map<String, Any?>> = emptyList(), val requests: List<Map<String, Any?>> = emptyList(),
    val invites: List<Map<String, Any?>> = emptyList(), val entries: List<Map<String, Any?>> = emptyList(),
    val scope: String = "general", val rank: Int? = null, val hasMore: Boolean = false, val boardLoading: Boolean = false
) {
    fun number(key: String) = (profile[key] as? Number)?.toInt() ?: 0
    val name get() = profile["name"] as? String ?: "Joueur"
    val coins get() = number("coins")
    val level get() = 1 + number("xp") / 500
    val owned get() = (profile["ownedSkins"] as? List<*>)?.filterIsInstance<String>().orEmpty()
    val ghostSkin get() = profile["ghostSkin"] as? String ?: "ghost_red_classic"
    val pacmanSkin get() = profile["pacmanSkin"] as? String ?: "pacman_classic"
}

class OnlineViewModel : ViewModel() {
    private val repository = OnlineRepository()
    private val _state = MutableStateFlow(OnlineState())
    val state = _state.asStateFlow()
    private var subscriptions: Job? = null
    private var boundUid: String? = null
    private var cursor: Map<String, Any?>? = null
    private var boardGeneration = 0
    private var started = false
    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        if (!started) return@AuthStateListener
        val uid = auth.currentUser?.uid ?: return@AuthStateListener
        if (uid != boundUid) bind(uid)
        _state.update { it.copy(guest = auth.currentUser?.isAnonymous != false) }
    }

    init {
        repository.auth.addAuthStateListener(authListener)
    }

    fun startIfNeeded() { if (!started) retry() }

    fun prepare(onReady: () -> Unit) = viewModelScope.launch {
        started = true
        _state.update { it.copy(loading = true, error = null) }
        try {
            repository.initialize()
            repository.auth.currentUser?.uid?.let { bind(it) }
            onReady()
        } catch (e: Exception) { report(e) }
    }

    fun retry() = viewModelScope.launch {
        started = true
        _state.update { it.copy(loading = true, error = null) }
        try { repository.initialize(); repository.auth.currentUser?.uid?.let { bind(it) } }
        catch (e: Exception) { report(e) }
    }

    private fun bind(uid: String) {
        subscriptions?.cancel()
        boundUid = uid
        _state.value = OnlineState(uid = uid, guest = repository.auth.currentUser?.isAnonymous != false)
        subscriptions = viewModelScope.launch {
            try { repository.call("ensureProfile") } catch (e: Exception) { report(e); return@launch }
            launch { repository.profile(uid).catch { report(it) }.collect { p ->
                _state.update { it.copy(profile = p, loading = false) }
                loadLeaderboard()
            } }
            listOf("history", "friends", "requests", "invites").forEach { collection ->
                launch { repository.list(uid, collection, collection == "history").catch { report(it) }.collect { rows ->
                    _state.update { when (collection) {
                        "history" -> it.copy(history = rows)
                        "friends" -> it.copy(friends = rows)
                        "requests" -> it.copy(requests = rows)
                        else -> it.copy(invites = rows)
                    } }
                } }
            }
        }
    }

    fun connect(activity: Activity, openExisting: Boolean = false) {
        if (_state.value.busy) return
        if (_state.value.profile["activeGame"] != null) {
            _state.update { it.copy(error = "Quitte ton salon ou termine ta partie avant de changer de compte.") }
            return
        }
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                repository.connectPlayGames(activity, openExisting)
                _state.update { it.copy(guest = false, existingAccount = false, message = "Compte Play Jeux connecté.") }
            } catch (e: Exception) {
                report(e)
                if (e is OnlineRepository.ExistingPlayAccount) _state.update { it.copy(existingAccount = true) }
            } finally { _state.update { it.copy(busy = false) } }
        }
    }

    fun action(name: String, data: Map<String, Any?> = emptyMap(), message: String = "Enregistré.") {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true, error = null, message = null) }
        viewModelScope.launch {
            try { repository.call(name, data); _state.update { it.copy(message = message) } }
            catch (e: Exception) { report(e) }
            finally { _state.update { it.copy(busy = false) } }
        }
    }

    fun loadLeaderboard(scope: String = _state.value.scope, more: Boolean = false) {
        if (!started || _state.value.uid.isEmpty()) return
        if (more && (_state.value.boardLoading || !_state.value.hasMore)) return
        val generation = ++boardGeneration
        val nextCursor = if (more) cursor else null
        _state.update { it.copy(scope = scope, boardLoading = true, entries = if (more) it.entries else emptyList()) }
        viewModelScope.launch {
            try {
                val result = repository.call("leaderboard", mapOf("scope" to scope, "cursor" to nextCursor))
                if (generation != boardGeneration) return@launch
                @Suppress("UNCHECKED_CAST")
                val rows = result["entries"] as? List<Map<String, Any?>> ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                cursor = result["cursor"] as? Map<String, Any?>
                _state.update { it.copy(entries = if (more) it.entries + rows else rows, rank = (result["rank"] as? Number)?.toInt(), hasMore = result["hasMore"] == true) }
            } catch (e: Exception) { if (generation == boardGeneration) report(e) }
            finally { if (generation == boardGeneration) _state.update { it.copy(boardLoading = false) } }
        }
    }

    fun clearNotice() { _state.update { it.copy(message = null, error = null) } }
    private fun report(e: Throwable) {
        if (e is CancellationException) throw e
        val message = when (e) {
            is FirebaseFunctionsException -> when (e.code) {
                FirebaseFunctionsException.Code.NOT_FOUND -> "Le service de jeu n’est pas encore disponible pour cette version."
                FirebaseFunctionsException.Code.UNAVAILABLE, FirebaseFunctionsException.Code.DEADLINE_EXCEEDED -> "Connexion indisponible. Réessaie quand le réseau revient."
                else -> e.message ?: "Action impossible. Réessaie."
            }
            else -> e.message ?: "Connexion impossible. Vérifie le réseau puis réessaie."
        }
        _state.update { it.copy(error = message, loading = false) }
    }
    override fun onCleared() { repository.auth.removeAuthStateListener(authListener); subscriptions?.cancel() }
}
