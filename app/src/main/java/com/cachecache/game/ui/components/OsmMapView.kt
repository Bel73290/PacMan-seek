package com.cachecache.game.ui.components

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Point
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.StrokeCap
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
    onPlayerTap: (HiddenPlayer) -> Unit = {}
) {
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var redrawTrigger by remember { mutableLongStateOf(0L) }

    // 1. Pacman Chomp Animation (0° to 60°)
    val infiniteTransition = rememberInfiniteTransition(label = "mapAnimations")
    val mouthAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mouthAngle"
    )

    // 2. Multi-Ring Radar Wave Animation
    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveProgress"
    )

    // 3. Pac-dots Conveyor Belt Phase Animation
    val pacDotsPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pacDotsPhase"
    )

    // 4. Ghost Hovering & Tremble Animation
    val hoverPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hoverPhase"
    )

    val ghostTremble by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(70, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ghostTremble"
    )

    // Bearing smooth interpolation
    val animatedBearing by androidx.compose.animation.core.animateFloatAsState(
        targetValue = seekerHeading,
        animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow),
        label = "bearingInterpolated"
    )

    // Camera continuously follows seeker centered
    LaunchedEffect(seekerLocation) {
        mapViewRef?.let { map ->
            map.controller.setCenter(OsmGeoPoint(seekerLocation.latitude, seekerLocation.longitude))
            redrawTrigger++
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // OSMDroid MapView with Dark Theme Styling
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true
                    controller.setZoom(18.2)
                    controller.setCenter(OsmGeoPoint(seekerLocation.latitude, seekerLocation.longitude))

                    // Dark Theme Filter (Slate blue buildings, dark navy streets, muted parks)
                    val darkMatrix = ColorMatrix(
                        floatArrayOf(
                            0.28f, 0f, 0f, 0f, 12f,
                            0f, 0.32f, 0f, 0f, 22f,
                            0f, 0f, 0.44f, 0f, 40f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(darkMatrix))

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

        // 60fps Vector Overlay
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(hiddenPlayers, redrawTrigger) {
                    detectTapGestures { tapOffset ->
                        val map = mapViewRef ?: return@detectTapGestures
                        val proj = map.projection ?: return@detectTapGestures

                        // Ghost touch hit test
                        val tempPoint = Point()
                        var tappedPlayer: HiddenPlayer? = null
                        for (player in hiddenPlayers) {
                            if (player.isFound) continue
                            val osmPoint = OsmGeoPoint(player.location.latitude, player.location.longitude)
                            proj.toPixels(osmPoint, tempPoint)
                            val dx = tapOffset.x - tempPoint.x
                            val dy = tapOffset.y - tempPoint.y
                            if (dx * dx + dy * dy < 2000f) {
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
            val trigger = redrawTrigger
            val map = mapViewRef ?: return@Canvas
            val proj = map.projection ?: return@Canvas

            val pt = Point()

            // 1. Suspect Zone Polygon with Animated Dashed Outline
            suspectZone?.let { zone ->
                if (zone.polygon.size >= 3) {
                    val polyPath = Path()
                    zone.polygon.forEachIndexed { index, geo ->
                        proj.toPixels(OsmGeoPoint(geo.latitude, geo.longitude), pt)
                        if (index == 0) polyPath.moveTo(pt.x.toFloat(), pt.y.toFloat())
                        else polyPath.lineTo(pt.x.toFloat(), pt.y.toFloat())
                    }
                    polyPath.close()

                    drawPath(
                        path = polyPath,
                        color = ScaredBlue.copy(alpha = 0.20f),
                        style = Fill
                    )

                    drawPath(
                        path = polyPath,
                        color = ScaredBlue,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                        )
                    )
                }
            }

            // 2. Animated Pac-Dots Route (Treadmill Conveyor Effect)
            if (pacDotsRoute.size >= 2) {
                val screenPoints = pacDotsRoute.map { geo ->
                    val p = Point()
                    proj.toPixels(OsmGeoPoint(geo.latitude, geo.longitude), p)
                    Offset(p.x.toFloat(), p.y.toFloat())
                }

                for (i in 0 until screenPoints.size - 1) {
                    val p1 = screenPoints[i]
                    val p2 = screenPoints[i + 1]
                    val dx = p2.x - p1.x
                    val dy = p2.y - p1.y
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                    val step = 32f

                    var curr = (pacDotsPhase % step)
                    while (curr < dist) {
                        val fraction = curr / dist
                        val dotX = p1.x + dx * fraction
                        val dotY = p1.y + fraction * dy

                        drawCircle(
                            color = PacmanYellow.copy(alpha = 0.4f),
                            radius = 7.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                        drawCircle(
                            color = PacmanYellow,
                            radius = 4.5.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                        curr += step
                    }
                }
            }

            // 3. Seeker Center Point
            proj.toPixels(OsmGeoPoint(seekerLocation.latitude, seekerLocation.longitude), pt)
            val seekerCenter = Offset(pt.x.toFloat(), pt.y.toFloat())

            // 3-Ring Concentric Radar Wave (Radii 10dp to 70dp, Alpha 0.4f to 0f)
            val minR = 10.dp.toPx()
            val maxR = 70.dp.toPx()

            for (ring in 0 until 3) {
                val ringProgress = (waveProgress + ring * 0.33f) % 1f
                val ringRadius = minR + (maxR - minR) * ringProgress
                val ringAlpha = (1f - ringProgress).coerceIn(0f, 1f) * 0.4f

                drawCircle(
                    color = ScaredBlue.copy(alpha = ringAlpha * 0.4f),
                    radius = ringRadius,
                    center = seekerCenter
                )
                drawCircle(
                    color = ScaredBlue.copy(alpha = ringAlpha),
                    radius = ringRadius,
                    center = seekerCenter,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 4. Hidden Ghosts with Hovering and Wavy Skirt
            val ghostHoverOffset = sin(hoverPhase) * 5.dp.toPx()

            for (player in hiddenPlayers) {
                if (player.isFound) continue

                proj.toPixels(OsmGeoPoint(player.location.latitude, player.location.longitude), pt)
                val ghostSize = 36.dp.toPx()
                val ghostCenter = Offset(pt.x.toFloat(), pt.y.toFloat() + ghostHoverOffset)

                // Ground shadow
                drawOval(
                    color = Color.Black.copy(alpha = 0.4f),
                    topLeft = Offset(ghostCenter.x - ghostSize * 0.4f, pt.y + ghostSize * 0.32f),
                    size = Size(ghostSize * 0.8f, ghostSize * 0.20f)
                )

                if (player.isScared) {
                    drawCircle(
                        color = ScaredBlue.copy(alpha = 0.35f),
                        radius = ghostSize * 0.95f,
                        center = ghostCenter
                    )
                }

                val ghostOffsetX = if (player.isScared) ghostTremble else 0f
                val topLeft = Offset(ghostCenter.x - ghostSize / 2f + ghostOffsetX, ghostCenter.y - ghostSize / 2f)

                drawGlossyGhost(
                    topLeft = topLeft,
                    size = Size(ghostSize, ghostSize),
                    color = if (player.isScared) ScaredBlue else player.color,
                    isScared = player.isScared
                )
            }

            // 5. Pacman Seeker Marker with Chomp Cycle and Interpolated Bearing
            val pacmanSize = 42.dp.toPx()
            val pacmanTopLeft = Offset(seekerCenter.x - pacmanSize / 2f, seekerCenter.y - pacmanSize / 2f)

            // Ground shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(seekerCenter.x - pacmanSize * 0.45f, seekerCenter.y + pacmanSize * 0.35f),
                size = Size(pacmanSize * 0.9f, pacmanSize * 0.25f)
            )

            rotate(animatedBearing, seekerCenter) {
                drawChompPacman(
                    topLeft = pacmanTopLeft,
                    size = Size(pacmanSize, pacmanSize),
                    mouthAngle = mouthAngle,
                    color = PacmanYellow
                )
            }
        }
    }
}

private fun DrawScope.drawChompPacman(
    topLeft: Offset,
    size: Size,
    mouthAngle: Float,
    color: Color
) {
    val radius = size.minDimension / 2f
    val center = Offset(topLeft.x + size.width / 2f, topLeft.y + size.height / 2f)

    val startAngle = mouthAngle / 2f
    val sweepAngle = 360f - mouthAngle

    // Dark outline
    drawArc(
        color = DarkContrast,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = topLeft,
        size = size,
        style = Stroke(width = 3.dp.toPx())
    )

    // Body fill
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = topLeft,
        size = size
    )

    // Eye
    val eyeRadius = radius * 0.13f
    val eyeX = center.x + radius * 0.16f
    val eyeY = center.y - radius * 0.46f
    drawCircle(
        color = DarkContrast,
        radius = eyeRadius,
        center = Offset(eyeX, eyeY)
    )
}

private fun DrawScope.drawGlossyGhost(
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
        lineTo(topLeft.x + w, topLeft.y + h * 0.84f)

        val waveWidth = w / 3f
        val bottomY = topLeft.y + h
        val innerY = topLeft.y + h * 0.80f

        quadraticTo(topLeft.x + w - waveWidth * 0.5f, bottomY, topLeft.x + w - waveWidth, innerY)
        quadraticTo(topLeft.x + w - waveWidth * 1.5f, bottomY, topLeft.x + w - waveWidth * 2f, innerY)
        quadraticTo(topLeft.x + w - waveWidth * 2.5f, bottomY, topLeft.x, innerY)
        lineTo(topLeft.x, topLeft.y + radius)
        close()
    }

    drawPath(path = path, color = DarkContrast, style = Stroke(width = 2.5.dp.toPx()))
    drawPath(path = path, color = color)

    // Eyes and mouth
    if (!isScared) {
        val eyeWidth = w * 0.23f
        val eyeHeight = h * 0.28f
        val pupilRadius = eyeWidth * 0.36f

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
        val eyeRadius = w * 0.09f
        val leftEyeCenter = Offset(topLeft.x + w * 0.32f, topLeft.y + h * 0.35f)
        val rightEyeCenter = Offset(topLeft.x + w * 0.68f, topLeft.y + h * 0.35f)

        drawCircle(color = TextPrimary, radius = eyeRadius, center = leftEyeCenter)
        drawCircle(color = TextPrimary, radius = eyeRadius, center = rightEyeCenter)

        val mouthPath = Path().apply {
            val startX = topLeft.x + w * 0.20f
            val endX = topLeft.x + w * 0.80f
            val mouthY = topLeft.y + h * 0.64f
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
            style = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
        )
    }
}
