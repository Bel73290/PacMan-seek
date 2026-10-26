package com.cachecache.game.model

import org.junit.Assert.*
import org.junit.Test

class DirectionTest {
    @Test fun wrapsAndSnapsToSixDirections() {
        assertEquals(0,Direction.index(0f));assertEquals(0,Direction.index(359f))
        assertEquals(1,Direction.index(60f));assertEquals(5,Direction.index(-60f))
        assertEquals(0,Direction.index(Float.NaN))
        assertEquals((0..5).toSet(), (0..359).map { Direction.index(it.toFloat()) }.toSet())
    }
    @Test fun gpsBearingsUseNorthAndEast() {
        val origin=GeoPoint(45.0,5.0)
        assertEquals(0f,Direction.bearing(origin,GeoPoint(45.001,5.0)),.01f)
        assertEquals(90f,Direction.bearing(origin,GeoPoint(45.0,5.001)),.01f)
        assertEquals(180f,Direction.bearing(origin,GeoPoint(44.999,5.0)),.01f)
    }
}
