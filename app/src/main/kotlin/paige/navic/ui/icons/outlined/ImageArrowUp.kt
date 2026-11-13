package paige.navic.ui.icons.outlined

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import paige.navic.ui.icons.Icons

@Suppress("CheckReturnValue")
val Icons.Outlined.ImageArrowUp: ImageVector
	get() {
		if (_ImageArrowUp != null) {
			return _ImageArrowUp!!
		}
		_ImageArrowUp =
			ImageVector.Builder(
				name = "Outlined.ImageArrowUp",
				defaultWidth = 24.dp,
				defaultHeight = 24.dp,
				viewportWidth = 24f,
				viewportHeight = 24f,
			)
				.apply {
					path(
						fill = SolidColor(Color(0xFFE3E3E3))
					) {
						moveTo(12f, 12f)
						close()
						moveTo(5f, 21f)
						quadTo(4.18f, 21f, 3.59f, 20.41f)
						reflectiveQuadTo(3f, 19f)
						verticalLineTo(5f)
						quadTo(3f, 4.17f, 3.59f, 3.59f)
						reflectiveQuadTo(5f, 3f)
						horizontalLineToRelative(8f)
						quadToRelative(0f, 0.45f, 0f, 0.95f)
						reflectiveQuadTo(13f, 5f)
						horizontalLineTo(5f)
						verticalLineTo(19f)
						horizontalLineTo(19f)
						verticalLineTo(12f)
						quadToRelative(0.53f, 0f, 1.03f, 0f)
						reflectiveQuadTo(21f, 12f)
						verticalLineToRelative(7f)
						quadToRelative(0f, 0.82f, -0.59f, 1.41f)
						reflectiveQuadTo(19f, 21f)
						horizontalLineTo(5f)
						close()
						moveTo(6f, 17f)
						horizontalLineTo(18f)
						lineTo(14.25f, 12f)
						lineToRelative(-3f, 4f)
						lineTo(9f, 13f)
						lineTo(6f, 17f)
						close()
						moveTo(18f, 10f)
						verticalLineTo(5.82f)
						lineTo(16.4f, 7.4f)
						lineTo(15f, 6f)
						lineTo(19f, 2f)
						lineToRelative(4f, 4f)
						lineTo(21.6f, 7.4f)
						lineTo(20f, 5.82f)
						verticalLineTo(10f)
						horizontalLineTo(18f)
						close()
					}
				}
				.build()
		return _ImageArrowUp!!
	}

@Suppress("ObjectPropertyName")
private var _ImageArrowUp: ImageVector? = null
