package com.cachecache.game.ui.components

import android.graphics.BitmapFactory
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.cachecache.game.R
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
    isFacingRight: Boolean = true,
    isFrozen: Boolean = false,
    capturingGhost: HiddenPlayer? = null,
    hiddenPlayers: List<HiddenPlayer>,
    suspectZone: SuspectZone?,
    pacDotsRoute: List<GeoPoint>,
    isRadarScanning: Boolean = false,
    zonePolygons: List<List<GeoPoint>> = emptyList(), // Zones supplémentaires (partie multijoueur)
    showSeeker: Boolean = true, // false sur l'écran d'un fantôme : pas de Pacman ni de radar
    onMapTap: (GeoPoint) -> Unit = {},
    onPlayerTap: (HiddenPlayer) -> Unit = {}
) {
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    // Preload directional ghost bitmaps and pacman chomp frames
    val ghostBitmaps = remember {
        val ids = mapOf(
            "red_true_front_0" to R.drawable.ghost_red_true_front_0,
            "red_true_front_1" to R.drawable.ghost_red_true_front_1,
            "red_front_0" to R.drawable.ghost_red_true_front_0,
            "red_front_1" to R.drawable.ghost_red_true_front_1,
            "red_slight_right_0" to R.drawable.ghost_red_front_0,
            "red_slight_right_1" to R.drawable.ghost_red_front_1,
            "red_slight_left_0" to R.drawable.ghost_red_slight_left_0,
            "red_slight_left_1" to R.drawable.ghost_red_slight_left_1,
            "red_left_0" to R.drawable.ghost_red_left_0,
            "red_left_1" to R.drawable.ghost_red_left_1,
            "red_right_0" to R.drawable.ghost_red_right_0,
            "red_right_1" to R.drawable.ghost_red_right_1,
            "red_back_0" to R.drawable.ghost_red_back_0,
            "red_back_1" to R.drawable.ghost_red_back_1,

            "pink_true_front_0" to R.drawable.ghost_pink_true_front_0,
            "pink_true_front_1" to R.drawable.ghost_pink_true_front_1,
            "pink_front_0" to R.drawable.ghost_pink_true_front_0,
            "pink_front_1" to R.drawable.ghost_pink_true_front_1,
            "pink_slight_right_0" to R.drawable.ghost_pink_front_0,
            "pink_slight_right_1" to R.drawable.ghost_pink_front_1,
            "pink_slight_left_0" to R.drawable.ghost_pink_slight_left_0,
            "pink_slight_left_1" to R.drawable.ghost_pink_slight_left_1,
            "pink_left_0" to R.drawable.ghost_pink_left_0,
            "pink_left_1" to R.drawable.ghost_pink_left_1,
            "pink_right_0" to R.drawable.ghost_pink_right_0,
            "pink_right_1" to R.drawable.ghost_pink_right_1,
            "pink_back_0" to R.drawable.ghost_pink_back_0,
            "pink_back_1" to R.drawable.ghost_pink_back_1,

            "cyan_true_front_0" to R.drawable.ghost_cyan_true_front_0,
            "cyan_true_front_1" to R.drawable.ghost_cyan_true_front_1,
            "cyan_front_0" to R.drawable.ghost_cyan_true_front_0,
            "cyan_front_1" to R.drawable.ghost_cyan_true_front_1,
            "cyan_slight_right_0" to R.drawable.ghost_cyan_front_0,
            "cyan_slight_right_1" to R.drawable.ghost_cyan_front_1,
            "cyan_slight_left_0" to R.drawable.ghost_cyan_slight_left_0,
            "cyan_slight_left_1" to R.drawable.ghost_cyan_slight_left_1,
            "cyan_left_0" to R.drawable.ghost_cyan_left_0,
            "cyan_left_1" to R.drawable.ghost_cyan_left_1,
            "cyan_right_0" to R.drawable.ghost_cyan_right_0,
            "cyan_right_1" to R.drawable.ghost_cyan_right_1,
            "cyan_back_0" to R.drawable.ghost_cyan_back_0,
            "cyan_back_1" to R.drawable.ghost_cyan_back_1,

            "orange_true_front_0" to R.drawable.ghost_orange_true_front_0,
            "orange_true_front_1" to R.drawable.ghost_orange_true_front_1,
            "orange_front_0" to R.drawable.ghost_orange_true_front_0,
            "orange_front_1" to R.drawable.ghost_orange_true_front_1,
            "orange_slight_right_0" to R.drawable.ghost_orange_front_0,
            "orange_slight_right_1" to R.drawable.ghost_orange_front_1,
            "orange_slight_left_0" to R.drawable.ghost_orange_slight_left_0,
            "orange_slight_left_1" to R.drawable.ghost_orange_slight_left_1,
            "orange_left_0" to R.drawable.ghost_orange_left_0,
            "orange_left_1" to R.drawable.ghost_orange_left_1,
            "orange_right_0" to R.drawable.ghost_orange_right_0,
            "orange_right_1" to R.drawable.ghost_orange_right_1,
            "orange_back_0" to R.drawable.ghost_orange_back_0,
            "orange_back_1" to R.drawable.ghost_orange_back_1,

            "purple_true_front_0" to R.drawable.ghost_purple_true_front_1,
            "purple_true_front_1" to R.drawable.ghost_purple_true_front_1,
            "purple_front_0" to R.drawable.ghost_purple_true_front_1,
            "purple_front_1" to R.drawable.ghost_purple_true_front_1,
            "purple_slight_right_0" to R.drawable.ghost_purple_front_0,
            "purple_slight_right_1" to R.drawable.ghost_purple_front_1,
            "purple_slight_left_0" to R.drawable.ghost_purple_slight_left_0,
            "purple_slight_left_1" to R.drawable.ghost_purple_slight_left_1,
            "purple_left_0" to R.drawable.ghost_purple_left_0,
            "purple_left_1" to R.drawable.ghost_purple_left_1,
            "purple_right_0" to R.drawable.ghost_purple_right_0,
            "purple_right_1" to R.drawable.ghost_purple_right_1,
            "purple_back_0" to R.drawable.ghost_purple_back_0,
            "purple_back_1" to R.drawable.ghost_purple_back_1,

            "scared_0" to R.drawable.ghost_scared_blue_0,
            "scared_1" to R.drawable.ghost_scared_blue_1
        )
        ids.mapValues { (_, resId) ->
            BitmapFactory.decodeResource(context.resources, resId)?.asImageBitmap()
        }
    }

    val pacmanChompBitmaps = remember {
        listOf(
            R.drawable.pacman_chomp_0,
            R.drawable.pacman_chomp_1,
            R.drawable.pacman_chomp_2,
            R.drawable.pacman_chomp_3,
            R.drawable.pacman_chomp_4,
            R.drawable.pacman_chomp_5,
            R.drawable.pacman_chomp_6,
            R.drawable.pacman_chomp_7,
            R.drawable.pacman_chomp_8,
            R.drawable.pacman_chomp_7,
            R.drawable.pacman_chomp_6,
            R.drawable.pacman_chomp_5,
            R.drawable.pacman_chomp_4,
            R.drawable.pacman_chomp_3,
            R.drawable.pacman_chomp_2,
            R.drawable.pacman_chomp_1
        ).mapNotNull { id ->
            BitmapFactory.decodeResource(context.resources, id)?.asImageBitmap()
        }
    }

    var redrawTrigger by remember { mutableLongStateOf(0L) }

    val smoothLat by animateFloatAsState(
        targetValue = seekerLocation.latitude.toFloat(),
        animationSpec = tween(durationMillis = 35, easing = LinearEasing),
        label = "smoothLat"
    )
    val smoothLon by animateFloatAsState(
        targetValue = seekerLocation.longitude.toFloat(),
        animationSpec = tween(durationMillis = 35, easing = LinearEasing),
        label = "smoothLon"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "mapAnimations")

    // 1. Pacman Chomp Frame Index (slow, authentic arcade cycle ~720ms)
    val pacmanFrameIndex by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 15,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pacmanFrameIndex"
    )

    // 2. Ghost authentic walk feet frame (toggles between 0 and 1 every 180ms)
    val walkFrame by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 1,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 360, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "walkFrame"
    )

    // 3. Scared Ghost panicked horizontal left-right trembling on the spot (NO vertical bounce)
    val scaredTrembleX by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(70, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaredTrembleX"
    )

    // 4. Multi-Ring Radar Wave Animation
    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveProgress"
    )

    // 5. Pac-dots Conveyor Belt Phase Animation
    val pacDotsPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pacDotsPhase"
    )

    // Camera continuously follows seeker centered
    LaunchedEffect(seekerLocation) {
        mapViewRef?.let { map ->
            map.controller.setCenter(OsmGeoPoint(seekerLocation.latitude, seekerLocation.longitude))
            redrawTrigger++
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // OSMDroid MapView with Minimalist No-Labels Tile Source
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

            // 1. Suspect Zone Polygons with Animated Dashed Outline
            val polygons = listOfNotNull(suspectZone?.polygon) + zonePolygons
            for (polygon in polygons) {
                if (polygon.size < 3) continue
                val polyPath = Path()
                polygon.forEachIndexed { index, geo ->
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

            // 2. Animated Pac-Dots Route (Treadmill Conveyor Effect)
            if (pacDotsRoute.size >= 2) {
                val screenPoints = pacDotsRoute.map { geo ->
                    val p = Point()
                    proj.toPixels(OsmGeoPoint(geo.latitude, geo.longitude), p)
                    Offset(p.x.toFloat(), p.y.toFloat())
                }

                val step = 32f
                // Espacement régulier sur tout le chemin, même quand il est fait de petits segments
                var carry = pacDotsPhase % step
                for (i in 0 until screenPoints.size - 1) {
                    val p1 = screenPoints[i]
                    val p2 = screenPoints[i + 1]
                    val dx = p2.x - p1.x
                    val dy = p2.y - p1.y
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                    var curr = carry
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
                    carry = curr - dist
                }
            }

            // 3. Seeker Center Point (smooth 60fps interpolation)
            proj.toPixels(OsmGeoPoint(smoothLat.toDouble(), smoothLon.toDouble()), pt)
            val seekerCenter = Offset(pt.x.toFloat(), pt.y.toFloat())

            // 3-Ring Concentric Radar Wave (Radii 10dp to 70dp, Alpha 0.4f to 0f)
            val minR = 10.dp.toPx()
            val maxR = 70.dp.toPx()

            if (showSeeker) for (ring in 0 until 3) {
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

            // 4. Hidden Ghosts with Directional Gaze Tracking Pacman & Scared Horizontal Tremble
            for (player in hiddenPlayers) {
                if (player.isFound || player.id == capturingGhost?.id) continue

                proj.toPixels(OsmGeoPoint(player.location.latitude, player.location.longitude), pt)
                val ghostSize = 40.dp.toPx()
                // Strictly NO vertical bobbing (up/down) - fixed on ground level
                val ghostCenter = Offset(pt.x.toFloat(), pt.y.toFloat())

                // Ground shadow
                drawOval(
                    color = Color.Black.copy(alpha = 0.40f),
                    topLeft = Offset(ghostCenter.x - ghostSize * 0.4f, ghostCenter.y + ghostSize * 0.32f),
                    size = Size(ghostSize * 0.8f, ghostSize * 0.20f)
                )

                if (player.isScared) {
                    drawCircle(
                        color = ScaredBlue.copy(alpha = 0.30f),
                        radius = ghostSize * 0.90f,
                        center = ghostCenter
                    )
                }

                // Calculate relative position: Ghost turns towards Pacman (including back, true front, slight left, slight right, full left, full right)
                val deltaX = seekerCenter.x - ghostCenter.x
                val deltaY = seekerCenter.y - ghostCenter.y
                val angleDeg = Math.toDegrees(kotlin.math.atan2(deltaX.toDouble(), deltaY.toDouble()))

                val dir = when {
                    kotlin.math.abs(angleDeg) <= 20.0 -> "true_front"
                    angleDeg in 20.0..70.0 -> "slight_right"
                    angleDeg in -70.0..-20.0 -> "slight_left"
                    angleDeg in 70.0..115.0 -> "right"
                    angleDeg in -115.0..-70.0 -> "left"
                    else -> "back"
                }

                val colorPrefix = when (player.name.trim().lowercase()) {
                    "pinky" -> "pink"
                    "inky" -> "cyan"
                    "clyde" -> "orange"
                    "blinky" -> "red"
                    "sue" -> "purple"
                    else -> "purple"
                }

                val spriteKey = if (player.isScared) {
                    "scared_$walkFrame"
                } else {
                    "${colorPrefix}_${dir}_$walkFrame"
                }

                val ghostBmp = ghostBitmaps[spriteKey]

                // Tremble on the spot: purely horizontal (translationX), translationY is strictly 0!
                val offsetX = if (player.isScared) scaredTrembleX else 0f
                val topLeft = Offset(ghostCenter.x - ghostSize / 2f + offsetX, ghostCenter.y - ghostSize / 2f)

                if (ghostBmp != null) {
                    val bmpW = ghostSize
                    val bmpH = bmpW * (ghostBmp.height.toFloat() / ghostBmp.width.toFloat())
                    drawImage(
                        image = ghostBmp,
                        dstOffset = IntOffset(topLeft.x.toInt(), (topLeft.y - (bmpH - bmpW) / 2f).toInt()),
                        dstSize = IntSize(bmpW.toInt(), bmpH.toInt())
                    )
                } else {
                    drawGlossyGhost(
                        topLeft = topLeft,
                        size = Size(ghostSize, ghostSize),
                        color = if (player.isScared) ScaredBlue else player.color,
                        isScared = player.isScared
                    )
                }
            }

            // 5. Pacman Seeker Marker with Authentic Chomp Cycle
            if (!showSeeker) return@Canvas
            val pacmanSize = 44.dp.toPx()
            val pacmanTopLeft = Offset(seekerCenter.x - pacmanSize / 2f, seekerCenter.y - pacmanSize / 2f)

            // Ground shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.40f),
                topLeft = Offset(seekerCenter.x - pacmanSize * 0.45f, seekerCenter.y + pacmanSize * 0.35f),
                size = Size(pacmanSize * 0.9f, pacmanSize * 0.25f)
            )

            // Immobilized halo if in 30s freeze phase
            if (isFrozen) {
                drawCircle(
                    color = ScaredBlue.copy(alpha = 0.50f),
                    radius = pacmanSize * 0.65f,
                    center = seekerCenter,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f.dp.toPx())
                )
            }

            val chompBmp = pacmanChompBitmaps.getOrNull(pacmanFrameIndex.coerceIn(0, pacmanChompBitmaps.size - 1))

            // Pacman strictly facing right or left (horizontal mirror based on movement)
            scale(scaleX = if (isFacingRight) 1f else -1f, scaleY = 1f, pivot = seekerCenter) {
                if (chompBmp != null) {
                    val pW = pacmanSize
                    val pH = pW * (chompBmp.height.toFloat() / chompBmp.width.toFloat())
                    drawImage(
                        image = chompBmp,
                        dstOffset = IntOffset((seekerCenter.x - pW / 2f).toInt(), (seekerCenter.y - pH / 2f).toInt()),
                        dstSize = IntSize(pW.toInt(), pH.toInt())
                    )
                } else {
                    drawChompPacman(
                        topLeft = pacmanTopLeft,
                        size = Size(pacmanSize, pacmanSize),
                        mouthAngle = 35f,
                        color = PacmanYellow
                    )
                }
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
