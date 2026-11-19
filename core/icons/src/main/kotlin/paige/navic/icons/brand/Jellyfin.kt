package paige.navic.icons.brand

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import paige.navic.icons.Icons

val Icons.Brand.Jellyfin: ImageVector
	get() {
		if (_Jellyfin != null) {
			return _Jellyfin!!
		}
		_Jellyfin = ImageVector.Builder(
			name = "Jellyfin",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 24f,
			viewportHeight = 24f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(12f, 9.45f)
				curveTo(11.044f, 9.45f, 7.959f, 15.042f, 8.428f, 15.984f)
				curveTo(8.897f, 16.927f, 15.108f, 16.917f, 15.572f, 15.984f)
				curveTo(16.036f, 15.052f, 12.961f, 9.45f, 12f, 9.45f)
				close()
			}
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(12f, 1.092f)
				curveTo(9.113f, 1.092f, -0.178f, 17.939f, 1.238f, 20.785f)
				curveTo(2.653f, 23.63f, 21.361f, 23.597f, 22.763f, 20.785f)
				curveTo(24.164f, 17.972f, 14.888f, 1.092f, 12f, 1.092f)
				close()
				moveTo(19.055f, 18.319f)
				curveTo(18.136f, 20.161f, 5.878f, 20.184f, 4.95f, 18.319f)
				curveTo(4.022f, 16.453f, 10.111f, 5.414f, 12f, 5.414f)
				curveTo(13.889f, 5.414f, 19.973f, 16.472f, 19.055f, 18.319f)
				close()
			}
		}.build()

		return _Jellyfin!!
	}

@Suppress("ObjectPropertyName")
private var _Jellyfin: ImageVector? = null
