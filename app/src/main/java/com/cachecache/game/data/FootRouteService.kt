package com.cachecache.game.data

import com.cachecache.game.model.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Itinéraire piéton calculé sur les chemins OpenStreetMap (serveur OSRM public de FOSSGIS),
 * utilisé pour tracer les pac-gommes jusqu'à la zone. Renvoie null en cas d'échec.
 */
object FootRouteService {

    private const val BASE_URL = "https://routing.openstreetmap.de/routed-foot/route/v1/foot"

    suspend fun route(from: GeoPoint, to: GeoPoint): List<GeoPoint>? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(
                "$BASE_URL/${from.longitude},${from.latitude};${to.longitude},${to.latitude}" +
                    "?overview=full&geometries=geojson"
            )
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5_000
                readTimeout = 5_000
                setRequestProperty("User-Agent", "PacAndSeek/1.0 (Android)")
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val coordinates = JSONObject(body)
                .getJSONArray("routes").getJSONObject(0)
                .getJSONObject("geometry").getJSONArray("coordinates")
            List(coordinates.length()) { i ->
                val point = coordinates.getJSONArray(i)
                GeoPoint(latitude = point.getDouble(1), longitude = point.getDouble(0))
            }.takeIf { it.size >= 2 }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
