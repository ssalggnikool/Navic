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
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale


@SuppressLint("UnnecessaryComposedModifier")
actual fun Modifier.backwardsCompatibleBlur(radius: Dp): Modifier  {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		this@backwardsCompatibleBlur then Modifier.blur(radius)
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
				0.2f
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
