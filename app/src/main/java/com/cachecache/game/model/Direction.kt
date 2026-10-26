package com.cachecache.game.model

import kotlin.math.*

/** Six clockwise orientations. Heading follows GPS bearings: 0=north, 90=east. */
object Direction {
    fun facesRight(heading: Float, previous: Boolean = true): Boolean {
        if (!heading.isFinite()) return previous
        val east = sin(Math.toRadians(heading.toDouble()))
        return when { east > .1 -> true; east < -.1 -> false; else -> previous }
    }
    fun index(heading: Float): Int = if (heading.isFinite()) ((heading / 60f).roundToInt() % 6 + 6) % 6 else 0
    fun angle(heading: Float): Float = index(heading) * 60f
    fun bearing(from: GeoPoint, to: GeoPoint): Float {
        val a = Math.toRadians(from.latitude); val b = Math.toRadians(to.latitude)
        val dl = Math.toRadians(to.longitude - from.longitude)
        return ((Math.toDegrees(atan2(sin(dl) * cos(b), cos(a) * sin(b) - sin(a) * cos(b) * cos(dl))) + 360) % 360).toFloat()
    }
}
