package paige.navic.ui.screens.library.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousCapsule
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.count_plays
import navic.composeapp.generated.resources.info_listening_stats_ratio
import navic.composeapp.generated.resources.title_statistics
import navic.composeapp.generated.resources.title_top_albums
import navic.composeapp.generated.resources.title_top_artists
import navic.composeapp.generated.resources.title_top_songs
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.PreferenceManager
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.screens.stats.viewmodels.StatisticsState
import paige.navic.ui.util.rememberColorSchemeFromCoverArt
import paige.navic.util.toSummaryString
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatisticsOverviewCard(
	statsState: StatisticsState,
	onClick: () -> Unit,
) {
	val pagerState = rememberPagerState { 3 }

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
			.clip(MaterialTheme.shapes.large)
			.background(MaterialTheme.colorScheme.surfaceContainerHigh)
			.clickable(onClick = onClick)
			.padding(vertical = 16.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		HorizontalPager(
			state = pagerState,
			contentPadding = PaddingValues(horizontal = 16.dp),
			pageSpacing = 16.dp
		) { page ->
			when (page) {
				0 -> TopArtistPage(statsState)
				1 -> TopAlbumPage(statsState)
				2 -> TopSongPage(statsState)
			}
		}

		Row(
			modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			repeat(pagerState.pageCount) { page ->
				val color by animateColorAsState(
					if (pagerState.currentPage == page)
						MaterialTheme.colorScheme.primary
					else MaterialTheme.colorScheme.surfaceVariant
				)
				Box(
					modifier = Modifier
						.padding(2.dp)
						.clip(CircleShape)
						.background(color)
						.size(6.dp)
				)
			}
		}
	}
}

@Composable
private fun TopArtistPage(statsState: StatisticsState) {
	val topArtist = statsState.topArtists.firstOrNull()
	StatPageContent(
		title = stringResource(Res.string.title_statistics),
		subtitle = stringResource(Res.string.title_top_artists),
		name = topArtist?.artist?.name ?: "No data",
		coverArtId = topArtist?.artist?.coverArtId,
		playCount = topArtist?.playCount ?: 0,
		duration = topArtist?.listeningTime ?: Duration.ZERO,
		totalDuration = statsState.totalDuration
	)
}

@Composable
private fun TopAlbumPage(statsState: StatisticsState) {
	val topAlbum = statsState.topAlbums.firstOrNull()
	StatPageContent(
		title = stringResource(Res.string.title_statistics),
		subtitle = stringResource(Res.string.title_top_albums),
		name = topAlbum?.album?.name ?: "No data",
		coverArtId = topAlbum?.album?.coverArtId,
		playCount = topAlbum?.playCount ?: 0,
		duration = topAlbum?.listeningTime ?: Duration.ZERO,
		totalDuration = statsState.totalDuration
	)
}

@Composable
private fun TopSongPage(statsState: StatisticsState) {
	val topSong = statsState.topSongs.firstOrNull()
	StatPageContent(
		title = stringResource(Res.string.title_statistics),
		subtitle = stringResource(Res.string.title_top_songs),
		name = topSong?.song?.title ?: "No data",
		coverArtId = topSong?.song?.coverArtId,
		playCount = topSong?.playCount ?: 0,
		duration = topSong?.listeningTime ?: Duration.ZERO,
		totalDuration = statsState.totalDuration
	)
}

@Composable
private fun StatPageContent(
	title: String,
	subtitle: String,
	name: String,
	coverArtId: String?,
	playCount: Int,
	duration: Duration,
	totalDuration: Duration
) {
	val colorScheme = rememberColorSchemeFromCoverArt(coverArtId)
	val barColor = colorScheme.primary
	val ratio = when {
		totalDuration > Duration.ZERO -> (duration.inWholeMilliseconds.toDouble() / totalDuration.inWholeMilliseconds.toDouble()).toFloat()
		else -> 0f
	}
	val preferenceManager = koinInject<PreferenceManager>()

	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(16.dp)
	) {
		CoverArt(
			coverArtId = coverArtId,
			modifier = Modifier.size(64.dp),
			shape = preferenceManager.coverArtShape.decreasedShape
		)

		Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
			Text(
				text = "$title • $subtitle",
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.primary,
				fontWeight = FontWeight.SemiBold
			)
			Text(
				text = name,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)

			val playsText = pluralStringResource(Res.plurals.count_plays, playCount, playCount)
			val timeText = duration.toSummaryString()

			Text(
				text = "$playsText • $timeText",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			if (playCount > 0) {
				Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.height(8.dp)
							.clip(ContinuousCapsule)
							.background(MaterialTheme.colorScheme.surfaceVariant)
					) {
						Box(
							modifier = Modifier
								.fillMaxWidth(ratio.coerceIn(0.05f, 1f))
								.fillMaxHeight()
								.clip(ContinuousCapsule)
								.background(barColor)
						)
					}
					Text(
						text = stringResource(
							Res.string.info_listening_stats_ratio,
							(ratio * 100).toInt()
						),
						style = MaterialTheme.typography.labelSmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		}
	}
}
