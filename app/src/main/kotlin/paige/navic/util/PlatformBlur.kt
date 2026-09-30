/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("DEPRECATION")

package paige.navic.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.renderscript.Element
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import coil3.request.ImageRequest
import coil3.request.allowHardware

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.backwardsCompatibleBlur(radius: Dp): Modifier  {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		return this.then(Modifier.blur(radius))
	}

	return this.composed {
		val context = LocalContext.current

		this.drawWithContent {
			val originalWidth = this.drawContext.size.width.toInt()
			val originalHeight = this.drawContext.size.height.toInt()

			val contentBitmap = createBitmap(originalWidth, originalHeight)
			val offscreenCanvas = androidx.compose.ui.graphics.Canvas(
				Canvas(contentBitmap)
			)

			// render this fuckass fuck offscreen
			drawIntoCanvas {
				drawContext.canvas.let { originalCanvas ->
					drawContext.canvas = offscreenCanvas
					drawContent()
					drawContext.canvas = originalCanvas
				}
			}

			val finalOutput = createBitmap(originalWidth, originalHeight)
			blurBitmapWithRenderShit(
				context, // <-- guess why all of this is in a composed block
				contentBitmap,
				finalOutput,
				radius.value,
				0.1f
			)

			drawIntoCanvas { canvas ->
				canvas.nativeCanvas.drawBitmap(
					finalOutput,
					Rect(0, 0, finalOutput.width, finalOutput.height),
					Rect(0, 0, originalWidth, originalHeight),
					null
				)
			}

			contentBitmap.recycle()
			finalOutput.recycle()
		}
	}
}

@Suppress("DEPRECATION")
fun blurBitmapWithRenderShit(
	context: Context,
	input: Bitmap,
	output: Bitmap,
	radius: Float,
	scale: Float
): Bitmap {
	val clampedScale = scale.coerceIn(0.05f, 1.0f)

	val targetWidth = (input.width * clampedScale).toInt().coerceAtLeast(1)
	val targetHeight = (input.height * clampedScale).toInt().coerceAtLeast(1)

	val scaledInput = input.scale(targetWidth, targetHeight)
	val scaledOutput = createBitmap(targetWidth, targetHeight)

	val renderShit = RenderScript.create(context)
	val inAlloc = Allocation.createFromBitmap(
		renderShit,
		scaledInput,
		Allocation.MipmapControl.MIPMAP_NONE,
		Allocation.USAGE_GRAPHICS_TEXTURE
	)
	val outAlloc = Allocation.createFromBitmap(renderShit, scaledOutput)

	ScriptIntrinsicBlur.create(renderShit, Element.U8_4(renderShit))
		.apply {
			setRadius(radius.coerceIn(1f, 25f)) // android docs: Supported range 0 < radius <= 25
			setInput(inAlloc)
			forEach(outAlloc)
		}

	outAlloc.copyTo(scaledOutput)
	renderShit.destroy()

	val canvas = Canvas(output)
	val paint = Paint(Paint.FILTER_BITMAP_FLAG)
	canvas.drawBitmap(
		scaledOutput, null, Rect(
			0, 0, output.width, output.height
		), paint
	)

	if (scaledInput != input) scaledInput.recycle()
	scaledOutput.recycle()
	return output
}

fun isHardwareRenderingCucked(): Boolean {
	return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.S
}

/*
 * if we're running android < 12, this basically fixes the issue where
 * blend rotation doesn't update in real time (it only did if you interacted with the now playing screen)
 */
@Composable
fun PlatformLayerComposable(
	modifier: Modifier,
	content: @Composable (() -> Unit)
) {
	AndroidView(
		factory = { context ->
			ComposeView(context)
		},
		update = { composeView ->
			composeView.setContent(content)
		},
		modifier = modifier,
	)
}

/*
 * if you don't disable this on the coil builder for the blend background,
 * you'll always be greeted with this error on android 7 > x < 12:
 *  "Software rendering doesn't support hardware bitmaps"
 * ...how many people are on the island again, beatoriche?
 */
fun ImageRequest.Builder.disableHardwareIfCucked(): ImageRequest.Builder {
	return this.allowHardware(!isHardwareRenderingCucked())
}
