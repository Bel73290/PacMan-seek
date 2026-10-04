package com.cachecache.game.data

import android.content.Context
import com.cachecache.game.model.ActiveMatch

/**
 * Mémorise la partie en cours sur le téléphone : si l'app est fermée (par erreur ou par le
 * système), on y revient directement au redémarrage, même sans réseau, grâce au cache Firestore.
 */
class MatchSessionStore(context: Context) {

    companion object {
        // Au-delà, la partie est forcément terminée (cache 3 min + chasse 30 min max)
        private const val MAX_AGE_MS = 45 * 60 * 1000L

        private const val KEY_CODE = "code"
        private const val KEY_IS_HOST = "isHost"
        private const val KEY_SAVED_AT = "savedAt"
    }

    private val prefs = context.applicationContext.getSharedPreferences("match_session", Context.MODE_PRIVATE)

    fun save(match: ActiveMatch) {
        // On garde l'heure de la première sauvegarde pour que l'expiration reste correcte
        if (prefs.getString(KEY_CODE, null) == match.code) return
        prefs.edit()
            .putString(KEY_CODE, match.code)
            .putBoolean(KEY_IS_HOST, match.isHost)
            .putLong(KEY_SAVED_AT, System.currentTimeMillis())
            .apply()
    }

    fun load(): ActiveMatch? {
        val code = prefs.getString(KEY_CODE, null) ?: return null
        if (System.currentTimeMillis() - prefs.getLong(KEY_SAVED_AT, 0L) > MAX_AGE_MS) {
            clear()
            return null
        }
        return ActiveMatch(code, prefs.getBoolean(KEY_IS_HOST, false))
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
