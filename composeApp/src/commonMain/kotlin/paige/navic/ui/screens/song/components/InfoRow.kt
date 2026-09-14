package paige.navic.ui.screens.song.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_unknown
import org.jetbrains.compose.resources.stringResource
import paige.navic.ui.components.common.SegmentedListItem

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SongDetailScreenInfoRow(
	shapes: ListItemShapes,
	key: String,
	value: String?
) {
	@Suppress("DEPRECATION")
	val clipboard = LocalClipboardManager.current

	SegmentedListItem(
		shapes = shapes,
		contentPadding = PaddingValues(14.dp),
		overlineContent = {
			Text(
				text = key,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.primary
			)
		},
		content = {
			Text(
				text = value ?: stringResource(Res.string.info_unknown),
				style = MaterialTheme.typography.bodyLarge
			)
		},
		onClick = {
			if (value == null) return@SegmentedListItem
			clipboard.setText(AnnotatedString(value))
		}
	)
}
