package paige.navic.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.capsule.ContinuousCapsule
import kotlinx.collections.immutable.toImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.count_plays
import navic.composeapp.generated.resources.info_total_listening_time
import navic.composeapp.generated.resources.info_total_plays
import navic.composeapp.generated.resources.title_statistics
import navic.composeapp.generated.resources.title_top_albums
import navic.composeapp.generated.resources.title_top_artists
import navic.composeapp.generated.resources.title_top_songs
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.di.LocalBottomBarScrollManager
import paige.navic.di.LocalNavStack
import paige.navic.di.LocalPlatformContext
import paige.navic.di.isLandscape
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.DomainSongListType
import paige.navic.domain.models.settings.BottomBarVisibilityMode
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.layouts.ArtCarousel
import paige.navic.ui.components.layouts.ArtCarouselItem
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.PullToRefreshBox
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.stats.viewmodels.ArtistStats
import paige.navic.ui.screens.stats.viewmodels.StatisticsViewModel
import paige.navic.ui.theme.NavicTheme
import paige.navic.ui.theme.defaultFont
import paige.navic.ui.util.rememberColorSchemeFromCoverArt
import paige.navic.util.toSummaryString

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
	nested: Boolean = false
) {
	val platformContext = LocalPlatformContext.current
	val preferenceManager = koinInject<PreferenceManager>()
	val viewModel = koinViewModel<StatisticsViewModel>()
	val state by viewModel.state.collectAsStateWithLifecycle()
	val backStack = LocalNavStack.current
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	NavicTheme {
		Scaffold(
			topBar = {
				if (!nested) {
					RootTopBar(
						title = { Text(stringResource(Res.string.title_statistics)) },
						scrollBehavior = scrollBehavior
					)
				} else {
					NestedTopBar(title = { Text(stringResource(Res.string.title_statistics)) })
				}
			},
			bottomBar = {
				val scrollManager = LocalBottomBarScrollManager.current
				val preferVisible = preferenceManager.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens
				if (!nested || (!platformContext.isLandscape() && preferVisible)) {
					RootBottomBar(scrolled = scrollManager.isTriggered)
				}
			}
		) { contentPadding ->
			PullToRefreshBox(
				modifier = Modifier
					.padding(top = contentPadding.calculateTopPadding())
					.background(MaterialTheme.colorScheme.surface),
				finished = !state.isLoading,
				onRefresh = {},
				key = state
			) {
				if (state.isLoading) {
					Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
						CircularProgressIndicator()
					}
				} else {
					Column(
						modifier = Modifier
							.fillMaxSize()
							.let {
								if (!nested) it.nestedScroll(scrollBehavior.nestedScrollConnection) else it
							}
							.verticalScroll(rememberScrollState()),
						verticalArrangement = Arrangement.spacedBy(24.dp)
					) {
						// Summary Cards
						Row(
							modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
							horizontalArrangement = Arrangement.spacedBy(12.dp)
						) {
							StatSummaryCard(
								modifier = Modifier.weight(1f),
								label = stringResource(Res.string.info_total_plays),
								value = state.totalPlays.toString()
							)
							StatSummaryCard(
								modifier = Modifier.weight(1f),
								label = stringResource(Res.string.info_total_listening_time),
								value = state.totalDuration.toSummaryString()
							)
						}

						// Top Artists Chart
						if (state.topArtists.isNotEmpty()) {
							Column(
								modifier = Modifier.padding(horizontal = 16.dp),
								verticalArrangement = Arrangement.spacedBy(16.dp)
							) {
								Text(
									text = stringResource(Res.string.title_top_artists),
									style = MaterialTheme.typography.titleLarge,
									fontWeight = FontWeight.SemiBold
								)
								val maxPlays = state.topArtists.first().playCount.toFloat()
								state.topArtists.forEach { stats ->
									ArtistStatsRow(
										stats = stats,
										ratio = stats.playCount / maxPlays,
										onClick = { backStack.add(Screen.ArtistDetail(stats.artist.id)) }
									)
								}
							}
						}

						// Top Albums Carousel
						ArtCarousel(
							title = stringResource(Res.string.title_top_albums),
							items = state.topAlbums.toImmutableList()
						) { album ->
							ArtCarouselItem(
								coverArtId = album.album.coverArtId,
								title = album.album.name,
								subtitle = album.album.artistName,
								playCount = album.playCount,
								duration = album.listeningTime,
								contentDescription = null,
								onClick = {
									backStack.add(Screen.CollectionDetail(album.album.id, "library"))
								}
							)
						}

						// Top Songs List
						ArtCarousel(
							title = stringResource(Res.string.title_top_songs),
							items = state.topSongs.toImmutableList(),
							destination = Screen.SongList(true, DomainSongListType.FrequentlyPlayed)
						) { song ->
							ArtCarouselItem(
								coverArtId = song.song.coverArtId,
								title = song.song.title,
								subtitle = song.song.artistName,
								playCount = song.playCount,
								duration = song.listeningTime,
								contentDescription = null,
								onClick = {
									backStack.add(Screen.SongDetailScreen(song.song.id, song.song.coverArtId))
								}
							)
						}

						Spacer(Modifier.height(contentPadding.calculateBottomPadding()))
					}
				}
			}
		}
	}
}

@Composable
fun StatSummaryCard(
	modifier: Modifier = Modifier,
	label: String,
	value: String
) {
	Surface(
		modifier = modifier,
		color = MaterialTheme.colorScheme.surfaceContainerHigh,
		shape = MaterialTheme.shapes.medium
	) {
		Column(Modifier.padding(16.dp)) {
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.primary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				text = value,
				style = MaterialTheme.typography.headlineMedium,
				fontWeight = FontWeight.Bold,
				fontFamily = defaultFont(round = 100f)
			)
		}
	}
}

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
