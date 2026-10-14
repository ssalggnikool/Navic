/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import coil3.ImageLoader
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import paige.navic.domain.manager.SessionManager
import paige.navic.util.PlatformLayerComposable
import paige.navic.util.backwardsCompatibleBlur
import paige.navic.util.disableHardwareIfCucked
import kotlin.math.roundToInt
import coil3.compose.LocalPlatformContext as LocalCoilPlatformContext

private const val FRAME_PERIOD_NS = 24_000_000_000L
private const val TOP_LEFT_PERIOD_NS = 12_000_000_000L
private const val BOT_RIGHT_PERIOD_NS = 20_000_000_000L
private const val LOOP_NS = 120_000_000_000L

private const val ART_SIZE_PX = 256
private const val RENDER_SCALE = 0.25f
private val BLUR_RADIUS = 80.dp

@Composable
fun BlendBackground(
	coverArtId: String?,
	modifier: Modifier = Modifier,
	isPaused: Boolean = false
) {
	var elapsedNanos by remember { mutableLongStateOf(0L) }

	val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
	val isActive = !isPaused && lifecycleState.isAtLeast(Lifecycle.State.STARTED)

	LaunchedEffect(isActive) {
		if (!isActive) return@LaunchedEffect
		var last = androidx.compose.runtime.withFrameNanos { it }
		while (true) {
			androidx.compose.runtime.withFrameNanos { now ->
				elapsedNanos = (elapsedNanos + (now - last)) % LOOP_NS
				last = now
			}
		}
	}

	val saturation = remember {
		ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(1.5f) })
	}

	val coilPlatformContext = LocalCoilPlatformContext.current
	val staticImageLoader = koinInject<ImageLoader>(named("static"))

	val sessionManager = koinInject<SessionManager>()

	val model = remember(coverArtId, coilPlatformContext) {
		ImageRequest.Builder(coilPlatformContext)
			.disableHardwareIfCucked()
			.data(coverArtId?.let { sessionManager.getCoverArtUrl(it) })
			.size(ART_SIZE_PX, ART_SIZE_PX)
			.memoryCacheKey(coverArtId?.let { "${it}_static_$ART_SIZE_PX" })
			.diskCacheKey(coverArtId)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.build()
	}

	val painter = rememberAsyncImagePainter(
		model = model,
		imageLoader = staticImageLoader,
		contentScale = ContentScale.Crop
	)

	PlatformLayerComposable(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.renderAtScale(RENDER_SCALE)
				.backwardsCompatibleBlur(BLUR_RADIUS * RENDER_SCALE)
				.graphicsLayer {
					compositingStrategy = CompositingStrategy.Offscreen
					colorFilter = saturation
				}
		) {
			Image(
				painter = painter,
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxSize()
			)
			Box(
				modifier = Modifier
					.fillMaxSize()
					.graphicsLayer {
						rotationZ = -angle(elapsedNanos, FRAME_PERIOD_NS)
					}
			) {
				Box(
					modifier = Modifier
						.fillMaxSize(0.5f)
						.align(Alignment.TopStart)
				) {
					Image(
						painter = painter,
						contentDescription = null,
						contentScale = ContentScale.Crop,
						alignment = Alignment.TopStart,
						modifier = Modifier
							.fillMaxSize()
							.graphicsLayer {
								rotationZ = angle(elapsedNanos, TOP_LEFT_PERIOD_NS)
							}
					)
				}
				Box(
					modifier = Modifier
						.fillMaxSize(0.5f)
						.align(Alignment.BottomEnd)
				) {
					Image(
						painter = painter,
						contentDescription = null,
						contentScale = ContentScale.Crop,
						alignment = Alignment.BottomEnd,
						modifier = Modifier
							.fillMaxSize()
							.graphicsLayer {
								rotationZ = angle(elapsedNanos, BOT_RIGHT_PERIOD_NS)
							}
					)
				}
			}
		}
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(Color.Black.copy(alpha = 0.4f))
		)
	}
}

private fun angle(elapsedNanos: Long, periodNanos: Long): Float =
	(elapsedNanos % periodNanos).toFloat() / periodNanos * 360f

private fun Modifier.renderAtScale(scale: Float): Modifier = layout { measurable, constraints ->
	if (!constraints.hasBoundedWidth || !constraints.hasBoundedHeight) {
		val placeable = measurable.measure(constraints)
		return@layout layout(placeable.width, placeable.height) { placeable.place(0, 0) }
	}
	val width = constraints.maxWidth
	val height = constraints.maxHeight
	val placeable = measurable.measure(
		Constraints.fixed(
			(width * scale).roundToInt().coerceAtLeast(1),
			(height * scale).roundToInt().coerceAtLeast(1)
		)
	)
	layout(width, height) {
		placeable.placeWithLayer(0, 0) {
			scaleX = 1f / scale
			scaleY = 1f / scale
			transformOrigin = TransformOrigin(0f, 0f)
		}
	}
}
