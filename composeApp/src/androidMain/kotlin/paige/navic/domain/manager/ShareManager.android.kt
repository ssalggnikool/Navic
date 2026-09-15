package paige.navic.domain.manager

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

actual class ShareManager(
	private val context: Context
) {
	private val dispatcher = Dispatchers.IO

	actual suspend fun shareImage(bitmap: ImageBitmap, fileName: String) {
		val androidBitmap = bitmap.asAndroidBitmap()

		val imageFolder = File(context.cacheDir, "shared_images")
		imageFolder.mkdirs()
		val file = File(imageFolder, fileName)

		try {
			withContext(dispatcher) {
				FileOutputStream(file).use { out ->
					androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
			return
		}

		val contentUri = FileProvider.getUriForFile(
			context,
			"${context.packageName}.fileprovider",
			file
		)

		val intent = Intent(Intent.ACTION_SEND).apply {
			type = "image/png"
			putExtra(Intent.EXTRA_STREAM, contentUri)
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		val chooser = Intent.createChooser(intent, "Share Image")
		chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		context.startActivity(chooser)
	}

	actual suspend fun saveImage(bitmap: ImageBitmap, fileName: String) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			val androidBitmap = bitmap.asAndroidBitmap()
			val resolver = context.contentResolver
			val contentValues = ContentValues().apply {
				put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
				put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
				put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
			}
			val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)!!
			val out = resolver.openOutputStream(uri)!!
			withContext(dispatcher) {
				androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
			}
		} else {
			// older sdks needs file management perms (I don't feel like accounting for that)
			shareImage(bitmap, fileName)
		}
	}

	actual suspend fun shareString(string: String) {
		val intent = Intent(Intent.ACTION_SEND).apply {
			type = "text/plain"
			putExtra(Intent.EXTRA_TEXT, string)
		}

		val chooser = Intent.createChooser(intent, "Share")
		chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		context.startActivity(chooser)
	}
}
