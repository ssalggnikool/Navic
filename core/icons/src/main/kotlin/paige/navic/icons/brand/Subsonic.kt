package paige.navic.icons.brand

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import paige.navic.icons.Icons

val Icons.Brand.Subsonic: ImageVector
	get() {
		if (_Subsonic != null) {
			return _Subsonic!!
		}
		_Subsonic = ImageVector.Builder(
			name = "Subsonic",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 24f,
			viewportHeight = 24f
		).apply {
			path(
				fill = SolidColor(Color(0xFFE3E3E3)),
				pathFillType = PathFillType.EvenOdd
			) {
				moveTo(6.797f, 5.243f)
				curveTo(7.185f, 5.243f, 7.5f, 5.712f, 7.5f, 6.291f)
				verticalLineTo(8.875f)
				curveTo(8.104f, 8.3f, 8.856f, 7.784f, 9.75f, 7.501f)
				verticalLineTo(6.289f)
				curveTo(9.75f, 5.713f, 10.368f, 5.25f, 10.95f, 5.25f)
				horizontalLineTo(16.2f)
				curveTo(16.781f, 5.25f, 17.25f, 5.713f, 17.25f, 6.289f)
				verticalLineTo(7.499f)
				curveTo(20.442f, 8.506f, 22.495f, 10.593f, 22.5f, 13.039f)
				curveTo(22.5f, 16.48f, 17.775f, 18.75f, 13.575f, 18.75f)
				curveTo(9.375f, 18.75f, 6.525f, 16.788f, 6f, 15.75f)
				curveTo(5.479f, 16.781f, 2.273f, 17.244f, 2.25f, 17.25f)
				verticalLineTo(15f)
				horizontalLineTo(1.5f)
				verticalLineTo(12f)
				horizontalLineTo(2.25f)
				verticalLineTo(9.375f)
				curveTo(2.25f, 9.375f, 5.475f, 9.837f, 6f, 10.875f)
				curveTo(6.028f, 10.82f, 6.057f, 10.763f, 6.089f, 10.705f)
				curveTo(6.033f, 10.554f, 6f, 10.38f, 6f, 10.195f)
				verticalLineTo(6.291f)
				curveTo(6f, 5.712f, 6.315f, 5.243f, 6.703f, 5.243f)
				horizontalLineTo(6.797f)
				close()
				moveTo(9.75f, 11.25f)
				curveTo(8.922f, 11.25f, 8.25f, 11.922f, 8.25f, 12.75f)
				curveTo(8.25f, 13.578f, 8.922f, 14.25f, 9.75f, 14.25f)
				curveTo(10.578f, 14.25f, 11.25f, 13.578f, 11.25f, 12.75f)
				curveTo(11.25f, 11.922f, 10.578f, 11.25f, 9.75f, 11.25f)
				close()
				moveTo(13.5f, 11.25f)
				curveTo(12.672f, 11.25f, 12f, 11.922f, 12f, 12.75f)
				curveTo(12f, 13.578f, 12.672f, 14.25f, 13.5f, 14.25f)
				curveTo(14.328f, 14.25f, 15f, 13.578f, 15f, 12.75f)
				curveTo(15f, 11.922f, 14.328f, 11.25f, 13.5f, 11.25f)
				close()
				moveTo(17.25f, 11.25f)
				curveTo(16.422f, 11.25f, 15.75f, 11.922f, 15.75f, 12.75f)
				curveTo(15.75f, 13.578f, 16.422f, 14.25f, 17.25f, 14.25f)
				curveTo(18.078f, 14.25f, 18.75f, 13.578f, 18.75f, 12.75f)
				curveTo(18.75f, 11.922f, 18.078f, 11.25f, 17.25f, 11.25f)
				close()
			}
		}.build()

		return _Subsonic!!
	}

@Suppress("ObjectPropertyName")
private var _Subsonic: ImageVector? = null
