package com.cachecache.game.ui.components

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Point
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.cachecache.game.model.*
import com.cachecache.game.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.MapView
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OsmMapView(
    seekerLocation: GeoPoint,
    seekerHeading: Float,
    hiddenPlayers: List<HiddenPlayer>,
    suspectZone: SuspectZone?,
    pacDotsRoute: List<GeoPoint>,
    isRadarScanning: Boolean = false,
    onMapTap: (GeoPoint) -> Unit = {},
    onPlayerTap: (HiddenPlayer) -> Unit = {},
    onRecenterRequested: ((MapView) -> Unit)? = null
) {
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var redrawTrigger by remember { mutableLongStateOf(0L) }

    // Radar Wave Animation
    val infiniteTransition = rememberInfiniteTransition(label = "radarTransition")
    val radarRadiusProgress by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRadius"
    )

    // Pac-dots Phase Animation
    val pacDotsPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pacDotsPhase"
    )

    // Ghost Tremble Animation for Scared Ghosts
    val ghostTremble by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(70, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ghostTremble"
    )

    // Keep map centered when seeker moves if user desires
    LaunchedEffect(seekerLocation) {
        mapViewRef?.let { map ->
            // Trigger projection refresh
            redrawTrigger++
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true
                    setBuiltInZoomControls(false)
                    controller.setZoom(17.8)
                    controller.setCenter(OsmGeoPoint(seekerLocation.latitude, seekerLocation.longitude))

                    // Dark mode filter for OSM tiles to match theme
                    val inverseMatrix = ColorMatrix(
                        floatArrayOf(
                            -0.75f, 0f, 0f, 0f, 210f,
                            0f, -0.75f, 0f, 0f, 210f,
                            0f, 0f, -0.75f, 0f, 230f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(inverseMatrix))

                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            redrawTrigger++
                            return false
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean {
                            redrawTrigger++
                            return false
                        }
                    })

                    mapViewRef = this
                }
            },
            update = { map ->
                mapViewRef = map
            }
        )

        // Compose Vector Overlay on top of MapView (with exact coordinate projection)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(hiddenPlayers, redrawTrigger) {
                    detectTapGestures { tapOffset ->
                        val map = mapViewRef ?: return@detectTapGestures
                        val proj = map.projection ?: return@detectTapGestures

                        // Check if tapped on a ghost marker
                        val tempPoint = Point()
                        var tappedPlayer: HiddenPlayer? = null
                        for (player in hiddenPlayers) {
                            if (player.isFound) continue
                            val osmPoint = OsmGeoPoint(player.location.latitude, player.location.longitude)
                            proj.toPixels(osmPoint, tempPoint)
                            val dx = tapOffset.x - tempPoint.x
                            val dy = tapOffset.y - tempPoint.y
                            if (dx * dx + dy * dy < 1800f) { // ~42dp radius hit test
                                tappedPlayer = player
                                break
                            }
                        }

                        if (tappedPlayer != null) {
                            onPlayerTap(tappedPlayer)
                        } else {
                            val geo = proj.fromPixels(tapOffset.x.toInt(), tapOffset.y.toInt())
                            onMapTap(GeoPoint(geo.latitude, geo.longitude))
                        }
                    }
                }
        ) {
            // Read redrawTrigger to ensure recomposition on pan/zoom
            val trigger = redrawTrigger
            val map = mapViewRef ?: return@Canvas
            val proj = map.projection ?: return@Canvas

            val pt = Point()

            // 1. Draw Suspect Zone Polygon
            suspectZone?.let { zone ->
                if (zone.polygon.size >= 3) {
                    val polyPath = Path()
                    zone.polygon.forEachIndexed { index, geo ->
                        proj.toPixels(OsmGeoPoint(geo.latitude, geo.longitude), pt)
                        if (index == 0) polyPath.moveTo(pt.x.toFloat(), pt.y.toFloat())
                        else polyPath.lineTo(pt.x.toFloat(), pt.y.toFloat())
                    }
                    polyPath.close()

                    // Translucent blue fill (#2E86DE at 20% alpha)
                    drawPath(
                        path = polyPath,
                        color = ScaredBlue.copy(alpha = 0.22f),
                        style = Fill
                    )

                    // Dashed stroke outline
                    drawPath(
                        path = polyPath,
                        color = ScaredBlue,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                        )
                    )
                }
            }

            // 2. Draw Pac-dots Path (Dotted route to suspect zone)
            if (pacDotsRoute.size >= 2) {
                val screenPoints = pacDotsRoute.map { geo ->
                    val p = Point()
                    proj.toPixels(OsmGeoPoint(geo.latitude, geo.longitude), p)
                    Offset(p.x.toFloat(), p.y.toFloat())
                }

                // Interpolate Pac-dots along path
                for (i in 0 until screenPoints.size - 1) {
                    val p1 = screenPoints[i]
                    val p2 = screenPoints[i + 1]
                    val dx = p2.x - p1.x
                    val dy = p2.y - p1.y
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                    val step = 28f

                    var curr = (pacDotsPhase % step)
                    while (curr < dist) {
                        val fraction = curr / dist
                        val dotX = p1.x + dx * fraction
                        val dotY = p1.y + fraction * dy
                        drawCircle(
                            color = PacmanYellow,
                            radius = 4.5.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                        curr += step
                    }
                }
            }

            // 3. Draw Radar Scanning Waves around Seeker
            proj.toPixels(OsmGeoPoint(seekerLocation.latitude, seekerLocation.longitude), pt)
            val seekerCenter = Offset(pt.x.toFloat(), pt.y.toFloat())

            val maxRadarRadius = 140.dp.toPx()
            val currentRadius = maxRadarRadius * radarRadiusProgress
            val radarAlpha = (1f - radarRadiusProgress).coerceIn(0f, 1f) * 0.45f

            // Concentric wave 1
            drawCircle(
                color = ScaredBlue.copy(alpha = radarAlpha),
                radius = currentRadius,
                center = seekerCenter
            )
            drawCircle(
                color = ScaredBlue.copy(alpha = radarAlpha * 1.5f),
                radius = currentRadius,
                center = seekerCenter,
                style = Stroke(width = 2.dp.toPx())
            )

            // Concentric wave 2 (if in Radar Screen mode)
            if (isRadarScanning) {
                val wave2Progress = (radarRadiusProgress + 0.5f) % 1.0f
                val radius2 = maxRadarRadius * wave2Progress
                val alpha2 = (1f - wave2Progress).coerceIn(0f, 1f) * 0.35f
                drawCircle(
                    color = ScaredBlue.copy(alpha = alpha2),
                    radius = radius2,
                    center = seekerCenter,
                    style = Stroke(width = 1.8.dp.toPx())
                )
            }

            // 4. Draw Hidden Players (Ghosts)
            for (player in hiddenPlayers) {
                if (player.isFound) continue

                proj.toPixels(OsmGeoPoint(player.location.latitude, player.location.longitude), pt)
                val ghostSize = 34.dp.toPx()
                val ghostCenter = Offset(pt.x.toFloat(), pt.y.toFloat())

                // Draw pulsing alert ring if scared
                if (player.isScared) {
                    drawCircle(
                        color = ScaredBlue.copy(alpha = 0.35f),
                        radius = ghostSize * 0.9f,
                        center = ghostCenter
                    )
                }

                // Draw Ghost Vector
                val ghostOffsetX = if (player.isScared) ghostTremble else 0f
                val topLeft = Offset(ghostCenter.x - ghostSize / 2f + ghostOffsetX, ghostCenter.y - ghostSize / 2f)

                // Scope drawGhost within bounded size
                drawScopeGhost(
                    topLeft = topLeft,
                    size = Size(ghostSize, ghostSize),
                    color = if (player.isScared) ScaredBlue else player.color,
                    isScared = player.isScared
                )
            }

            // 5. Draw Seeker Pacman Marker
            val pacmanSize = 38.dp.toPx()
            val pacmanTopLeft = Offset(seekerCenter.x - pacmanSize / 2f, seekerCenter.y - pacmanSize / 2f)

            rotate(seekerHeading, seekerCenter) {
                drawScopePacman(
                    topLeft = pacmanTopLeft,
                    size = Size(pacmanSize, pacmanSize),
                    color = PacmanYellow
                )
            }
        }
    }
}

private fun DrawScope.drawScopePacman(
    topLeft: Offset,
    size: Size,
    color: Color
) {
    val radius = size.minDimension / 2f
    val center = Offset(topLeft.x + size.width / 2f, topLeft.y + size.height / 2f)

    drawArc(
        color = color,
        startAngle = 25f,
        sweepAngle = 310f,
        useCenter = true,
        topLeft = topLeft,
        size = size
    )

    // Eye
    val eyeRadius = radius * 0.13f
    val eyeX = center.x + radius * 0.18f
    val eyeY = center.y - radius * 0.45f
    drawCircle(
        color = DarkContrast,
        radius = eyeRadius,
        center = Offset(eyeX, eyeY)
    )
}

private fun DrawScope.drawScopeGhost(
    topLeft: Offset,
    size: Size,
    color: Color,
    isScared: Boolean
) {
    val w = size.width
    val h = size.height
    val radius = w / 2f

    val path = Path().apply {
        moveTo(topLeft.x, topLeft.y + radius)
        cubicTo(
            topLeft.x, topLeft.y,
            topLeft.x + w, topLeft.y,
            topLeft.x + w, topLeft.y + radius
        )
        lineTo(topLeft.x + w, topLeft.y + h * 0.85f)

        val waveWidth = w / 3f
        val bottomY = topLeft.y + h
        val innerY = topLeft.y + h * 0.82f

        quadraticTo(topLeft.x + w - waveWidth * 0.5f, bottomY, topLeft.x + w - waveWidth, innerY)
        quadraticTo(topLeft.x + w - waveWidth * 1.5f, bottomY, topLeft.x + w - waveWidth * 2f, innerY)
        quadraticTo(topLeft.x + w - waveWidth * 2.5f, bottomY, topLeft.x, innerY)
        lineTo(topLeft.x, topLeft.y + radius)
        close()
    }

    drawPath(path = path, color = color)

    // Eyes
    if (!isScared) {
        val eyeWidth = w * 0.22f
        val eyeHeight = h * 0.26f
        val pupilRadius = eyeWidth * 0.35f

        val leftEyeCenter = Offset(topLeft.x + w * 0.32f, topLeft.y + h * 0.38f)
        val rightEyeCenter = Offset(topLeft.x + w * 0.68f, topLeft.y + h * 0.38f)

        drawOval(
            color = TextPrimary,
            topLeft = Offset(leftEyeCenter.x - eyeWidth / 2f, leftEyeCenter.y - eyeHeight / 2f),
            size = Size(eyeWidth, eyeHeight)
        )
        drawOval(
            color = TextPrimary,
            topLeft = Offset(rightEyeCenter.x - eyeWidth / 2f, rightEyeCenter.y - eyeHeight / 2f),
            size = Size(eyeWidth, eyeHeight)
        )

        drawCircle(color = DarkContrast, radius = pupilRadius, center = leftEyeCenter)
        drawCircle(color = DarkContrast, radius = pupilRadius, center = rightEyeCenter)
    } else {
        val eyeRadius = w * 0.08f
        val leftEyeCenter = Offset(topLeft.x + w * 0.32f, topLeft.y + h * 0.36f)
        val rightEyeCenter = Offset(topLeft.x + w * 0.68f, topLeft.y + h * 0.36f)

        drawCircle(color = TextPrimary, radius = eyeRadius, center = leftEyeCenter)
        drawCircle(color = TextPrimary, radius = eyeRadius, center = rightEyeCenter)

        // Zigzag mouth
        val mouthPath = Path().apply {
            val startX = topLeft.x + w * 0.22f
            val endX = topLeft.x + w * 0.78f
            val mouthY = topLeft.y + h * 0.62f
            val segWidth = (endX - startX) / 6f
            val waveAmp = h * 0.06f

            moveTo(startX, mouthY)
            var currentX = startX
            var up = true
            for (i in 0 until 6) {
                val nextX = currentX + segWidth
                val targetY = if (up) mouthY - waveAmp else mouthY + waveAmp
                lineTo(nextX, targetY)
                currentX = nextX
                up = !up
            }
        }
        drawPath(
            path = mouthPath,
            color = TextPrimary,
            style = Stroke(width = w * 0.06f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}
