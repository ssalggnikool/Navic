package paige.navic.ui.screens.collection.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import paige.navic.R
import paige.navic.domain.models.DomainSongCollection
import paige.navic.ui.theme.defaultFont

@Composable
fun CollectionDetailScreenFooterRow(
	collection: DomainSongCollection
) {
	Text(
		buildString {
			append(
				pluralStringResource(
					R.plurals.count_songs,
					collection.songCount,
					collection.songCount
				)
			)
			append(" • ")
			append(collection.duration.toString())
		},
		style = MaterialTheme.typography.titleSmall,
		fontFamily = defaultFont(round = 100f),
		color = MaterialTheme.colorScheme.onSurfaceVariant,
		modifier = Modifier.fillMaxWidth().padding(
			horizontal = 16.dp,
			vertical = 8.dp
		)
	)
}
