package paige.navic.ui.screens.stats.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.count_plays
import org.jetbrains.compose.resources.pluralStringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.PreferenceManager
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.screens.stats.viewmodels.ArtistStats
import paige.navic.ui.theme.defaultFont
import paige.navic.ui.util.rememberColorSchemeFromCoverArt
import paige.navic.util.toSummaryString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopArtistItem(
	stats: ArtistStats,
	ratio: Float,
	onClick: () -> Unit,
	onLongClick: () -> Unit
) {
	val artist = stats.artist
	val preferenceManager = koinInject<PreferenceManager>()
	val colorScheme = rememberColorSchemeFromCoverArt(artist.coverArtId)
	val barColor = colorScheme.primary

	ListItem(
		onClick = onClick,
		onLongClick = onLongClick,
		leadingContent = {
			CoverArt(
				coverArtId = artist.coverArtId,
				modifier = Modifier.size(56.dp),
				shape = preferenceManager.artistImageShape.decreasedShape
			)
		},
		overlineContent = {
			val listenTime = stats.listeningTime.toSummaryString()
			val plays = pluralStringResource(Res.plurals.count_plays, stats.playCount, stats.playCount)
			Text(
				text = "$listenTime • $plays",
				fontWeight = FontWeight.SemiBold,
				fontFamily = defaultFont(round = 100f)
			)
		},
		supportingContent = {
			LinearProgressIndicator(
				progress = { ratio },
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 4.dp)
					.height(8.dp)
					.clip(MaterialTheme.shapes.extraSmall),
				color = barColor,
				trackColor = barColor.copy(alpha = 0.2f),
				strokeCap = StrokeCap.Round
			)
		},
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(artist.name)
	}
}
