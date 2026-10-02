/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.common

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import org.intellij.lang.annotations.Language
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import paige.navic.domain.manager.SessionManager
import paige.navic.ui.util.rememberColorSchemeForCurrentSong
import paige.navic.util.PlatformLayerComposable
import paige.navic.util.backwardsCompatibleBlur
import paige.navic.util.disableHardwareIfCucked
import kotlin.time.TimeSource
import coil3.compose.LocalPlatformContext as LocalCoilPlatformContext

@Language("AGSL")
val CUSTOM_SHADER = """
// https://github.com/claration wrote this as a metal shader and i converted to agsl (ty)
uniform float time;
uniform float2 resolution;
uniform float3 color1;// = float3(0.1, 0.0, 0.2);	// dark purple
uniform float3 color2;// = float3(0.7, 0.2, 0.8);	// main purple
uniform float3 color3;// = float3(1.0, 0.5, 1.0);	// highlight pink

// Constants
const float SPEED = 0.2;

float rand(float2 p) {
	return fract(sin(dot(p, float2(12.99, 78.233))) * 43758.545);
}

float noise(float2 p) {
	float2 f = fract(p);
	f = f * f * (3.0 - 2.0 * f);
	float2 i = floor(p);
	return mix(mix(rand(i + float2(0, 0)),
				   rand(i + float2(1, 0)), f.x),
			   mix(rand(i + float2(0, 1)),
				   rand(i + float2(1, 1)), f.x), f.y);
}

float fbm_streaks(float2 p) {
	float v = 0.1;
	float a = 0.5;
	float2x2 rot = float2x2(0.6, 0.6, -0.8, 0.8);
	for (int i = 0; i < 7; ++i) {
		v += a * noise(p);
		p = (rot * p) * float2(1.5, 0.7);
		a *= 0.5;
	}
	return v;
}

half4 main(float2 fragCoord) {
	float2 uv = fragCoord / resolution;
	float2 p = (2.0 * uv - 1.0) * float2(resolution.x/resolution.y, 1.0);

	float t = time * SPEED;

	float f = fbm_streaks(p * 1.0 + t);
	float f2 = fbm_streaks(p * 0.2 - t * 1.5 + f);

	float2 q = float2(f, f2);
	float final = fbm_streaks(p * 1.0 + q * 2.0);

	float3 col = mix(color1, color2, smoothstep(0.2, 0.9, final));
	col = mix(col, color3, smoothstep(0.6, 1.0, final));

	col = pow(col, float3(1.5));
	col = mix(col, col * col * (3.0 - 2.0 * col), 0.15);

	return half4(col, 1.0);
}
""".trimIndent()

@Composable
fun BlendBackground(
	coverArtId: String?,
	modifier: Modifier = Modifier,
	isPaused: Boolean = false
) {
	var frameRotation by remember { mutableFloatStateOf(0f) }
	var topLeftRotation by remember { mutableFloatStateOf(0f) }
	var botRightRotation by remember { mutableFloatStateOf(0f) }

	var t by remember { mutableFloatStateOf(0f) }

	val colorMatrix = remember {
		ColorMatrix().apply { setToSaturation(1.5f) }
	}

	val coilPlatformContext = LocalCoilPlatformContext.current
	val staticImageLoader = koinInject<ImageLoader>(named("static"))

	val sessionManager = koinInject<SessionManager>()
	val model = remember(coverArtId) {
		ImageRequest.Builder(coilPlatformContext)
			.disableHardwareIfCucked()
			.data(coverArtId?.let { sessionManager.getCoverArtUrl(it) })
			.memoryCacheKey(coverArtId?.let { "${it}_static" })
			.diskCacheKey(coverArtId)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.build()
	}

	val colorScheme = rememberColorSchemeForCurrentSong()

	LaunchedEffect(isPaused) {
		if (!isPaused) {
			val timeSource = TimeSource.Monotonic
			var lastFrameMark = timeSource.markNow()

			while (true) {
				withFrameNanos { _ ->
					val now = timeSource.markNow()
					val elapsed = now - lastFrameMark
					val elapsedMillis =
						elapsed.toDouble(kotlin.time.DurationUnit.MILLISECONDS).toFloat()
					lastFrameMark = now

					frameRotation -= (360f / 24000f) * elapsedMillis
					topLeftRotation += (360f / 12000f) * elapsedMillis
					botRightRotation += (360f / 20000f) * elapsedMillis
					t += elapsedMillis * 0.0006f
				}
			}
		}
	}

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TODO
		Box(
			modifier = Modifier
				.drawWithCache {
					val shader = RuntimeShader(CUSTOM_SHADER)
					val shaderBrush = ShaderBrush(shader)
					shader.setFloatUniform("time", t)
					shader.setFloatUniform("resolution", size.width, size.height)
					val color1 = colorScheme.surfaceContainer //dark
					val color2 = colorScheme.primary //main
					val color3 = colorScheme.primaryContainer //highlight
					onDrawBehind {
						shader.setFloatUniform(
							"color1",
							/* value1 = */ color1.red,
							/* value2 = */ color1.green,
							/* value3 = */ color1.blue
						)
						shader.setFloatUniform(
							"color2",
							/* value1 = */ color2.red,
							/* value2 = */ color2.green,
							/* value3 = */ color2.blue
						)
						shader.setFloatUniform(
							"color3",
							/* value1 = */ color3.red,
							/* value2 = */ color3.green,
							/* value3 = */ color3.blue
						)
						drawRect(shaderBrush)
					}
				}
				.fillMaxSize()
		)
	} else {
		PlatformLayerComposable(
			modifier = modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background)
				.backwardsCompatibleBlur(80.dp)
		) {
			AsyncImage(
				model = model,
				imageLoader = staticImageLoader,
				contentDescription = null,
				contentScale = ContentScale.Crop,
				colorFilter = ColorFilter.colorMatrix(colorMatrix),
				modifier = Modifier.fillMaxSize()
			)
			Box(
				modifier = Modifier
					.fillMaxSize()
					.rotate(frameRotation)
			) {
				Box(
					modifier = Modifier
						.fillMaxWidth(0.5f)
						.fillMaxHeight(0.5f)
						.align(Alignment.TopStart)
				) {
					AsyncImage(
						model = model,
						imageLoader = staticImageLoader,
						contentDescription = null,
						contentScale = ContentScale.Crop,
						alignment = Alignment.TopStart,
						colorFilter = ColorFilter.colorMatrix(colorMatrix),
						modifier = Modifier
							.fillMaxSize()
							.rotate(topLeftRotation)
					)
				}
				Box(
					modifier = Modifier
						.fillMaxWidth(0.5f)
						.fillMaxHeight(0.5f)
						.align(Alignment.BottomEnd)
				) {
					AsyncImage(
						model = model,
						imageLoader = staticImageLoader,
						contentDescription = null,
						contentScale = ContentScale.Crop,
						alignment = Alignment.BottomEnd,
						colorFilter = ColorFilter.colorMatrix(colorMatrix),
						modifier = Modifier
							.fillMaxSize()
							.rotate(botRightRotation)
					)
				}
			}
			Spacer(
				modifier = Modifier
					.fillMaxSize()
					.drawWithContent {
						drawContent()
						drawRect(color = Color.Black.copy(alpha = 0.4f))
					}
			)
		}
	}
}
