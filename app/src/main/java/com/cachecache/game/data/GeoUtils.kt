package com.cachecache.game.data

import com.cachecache.game.model.GeoPoint
import com.cachecache.game.model.GhostZone
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object GeoUtils {

    /** Rayon d'une zone suspecte : environ la taille d'un bâtiment (70 m de diamètre). */
    const val ZONE_RADIUS_METERS = 35.0

    private const val METERS_PER_DEGREE_LAT = 111_320.0

    /** Point situé à `meters` mètres de `origin`, dans la direction `bearingRad` (0 = nord). */
    fun offset(origin: GeoPoint, meters: Double, bearingRad: Double): GeoPoint {
        val dLat = meters * cos(bearingRad) / METERS_PER_DEGREE_LAT
        val dLon = meters * sin(bearingRad) / (METERS_PER_DEGREE_LAT * cos(Math.toRadians(origin.latitude)))
        return GeoPoint(origin.latitude + dLat, origin.longitude + dLon)
    }

    /**
     * Zone dans laquelle un fantôme est caché. Le centre est décalé au hasard pour que le
     * fantôme ne soit pas pile au milieu, mais il reste toujours à l'intérieur du cercle.
     * Le hasard dépend de la partie et du fantôme : la zone reste la même si l'app redémarre.
     */
    fun buildZone(gameCode: String, ghostId: String, ghostLocation: GeoPoint): GhostZone {
        val random = Random((gameCode + ghostId).hashCode())
        val shift = random.nextDouble() * ZONE_RADIUS_METERS * 0.6
        val center = offset(ghostLocation, shift, random.nextDouble() * 2 * PI)
        val polygon = (0 until 40).map { i -> offset(center, ZONE_RADIUS_METERS, 2 * PI * i / 40) }
        return GhostZone(ghostId, center, ZONE_RADIUS_METERS, polygon)
    }
}
