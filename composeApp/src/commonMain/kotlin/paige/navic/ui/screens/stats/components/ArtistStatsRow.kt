package paige.navic.ui.screens.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousCapsule
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.count_plays
import org.jetbrains.compose.resources.pluralStringResource
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.screens.stats.viewmodels.ArtistStats
import paige.navic.ui.util.rememberColorSchemeFromCoverArt
import paige.navic.util.toSummaryString

@Composable
fun ArtistStatsRow(
	stats: ArtistStats,
	ratio: Float,
	onClick: () -> Unit
) {
	val artist = stats.artist
	val colorScheme = rememberColorSchemeFromCoverArt(artist.coverArtId)
	val barColor = colorScheme.primary

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.medium)
			.clickable(onClick = onClick)
			.padding(vertical = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(12.dp)
	) {
		CoverArt(
			coverArtId = artist.coverArtId,
			modifier = Modifier.size(56.dp),
			shape = RoundedCornerShape(12.dp)
		)

		Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.Bottom
			) {
				Text(
					text = artist.name,
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.Medium,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.weight(1f)
				)
				Column(horizontalAlignment = Alignment.End) {
					Text(
						text = stats.listeningTime.toSummaryString(),
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold,
						color = barColor
					)
					Text(
						text = pluralStringResource(Res.plurals.count_plays, stats.playCount, stats.playCount),
						style = MaterialTheme.typography.labelSmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(12.dp)
					.clip(ContinuousCapsule)
					.background(MaterialTheme.colorScheme.surfaceVariant)
			) {
				Box(
					modifier = Modifier
						.fillMaxWidth(ratio)
						.fillMaxHeight()
						.clip(ContinuousCapsule)
						.background(barColor)
				)
			}
		}
	}
}
