package paige.navic.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_starred_songs
import navic.composeapp.generated.resources.info_total_listening_time
import navic.composeapp.generated.resources.info_total_plays
import navic.composeapp.generated.resources.title_statistics
import navic.composeapp.generated.resources.title_top_albums
import navic.composeapp.generated.resources.title_top_artists
import navic.composeapp.generated.resources.title_top_songs
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
import paige.navic.icons.Icons
import paige.navic.icons.filled.Headphones
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Note
import paige.navic.ui.components.layouts.ArtCarousel
import paige.navic.ui.components.layouts.ArtCarouselItem
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.stats.components.StatSummaryCard
import paige.navic.ui.screens.stats.components.TopArtistItem
import paige.navic.ui.screens.stats.viewmodels.StatisticsViewModel
import paige.navic.ui.theme.NavicTheme
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
	val starredSongs by viewModel.starredSongs.collectAsStateWithLifecycle()
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
				val preferVisible =
					preferenceManager.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens
				if (!nested || (!platformContext.isLandscape() && preferVisible)) {
					RootBottomBar(scrolled = scrollManager.isTriggered)
				}
			}
		) { contentPadding ->
			if (state.isLoading) {
				Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
					CircularProgressIndicator()
				}
			} else {
				LazyVerticalGrid(
					modifier = Modifier
						.fillMaxSize()
						.let {
							if (!nested) it.nestedScroll(scrollBehavior.nestedScrollConnection) else it
						},
					contentPadding = contentPadding,
					columns = GridCells.Fixed(2),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					item(span = { GridItemSpan(maxLineSpan) }) {
						val color = MaterialTheme.colorScheme.primary
						val contentColor = MaterialTheme.colorScheme.onPrimary
						StatSummaryCard(
							color = color,
							contentColor = contentColor,
							horizontal = true,
							label = stringResource(Res.string.info_total_listening_time),
							value = state.totalDuration.toSummaryString(),
							icon = {
								Surface(
									color = contentColor,
									contentColor = color,
									shape = MaterialShapes.Cookie9Sided.toShape()
								) {
									Icon(
										imageVector = Icons.Filled.Headphones,
										contentDescription = null,
										modifier = Modifier.padding(16.dp).size(32.dp)
									)
								}
							},
							modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
						)
					}

					item {
						val color = MaterialTheme.colorScheme.primaryContainer
						val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
						StatSummaryCard(
							color = color,
							contentColor = contentColor,
							label = stringResource(Res.string.info_total_plays),
							value = state.totalPlays.toString(),
							icon = {
								Surface(
									color = contentColor,
									contentColor = color,
									shape = MaterialShapes.Clover4Leaf.toShape()
								) {
									Icon(
										imageVector = Icons.Outlined.Note,
										contentDescription = null,
										modifier = Modifier.padding(12.dp)
									)
								}
							},
							modifier = Modifier.padding(start = 16.dp)
						)
					}

					item {
						val color = MaterialTheme.colorScheme.onTertiaryContainer
						val contentColor = MaterialTheme.colorScheme.tertiaryContainer
						StatSummaryCard(
							color = color,
							contentColor = contentColor,
							label = stringResource(Res.string.info_starred_songs)
								.toLowerCase(LocalLocale.current),
							value = starredSongs.count().toString(),
							icon = {
								Surface(
									color = contentColor,
									contentColor = color,
									shape = MaterialShapes.VerySunny.toShape()
								) {
									Icon(
										imageVector = Icons.Filled.Star,
										contentDescription = null,
										modifier = Modifier.padding(12.dp)
									)
								}
							},
							modifier = Modifier.padding(end = 16.dp)
						)
					}

					if (state.topArtists.isNotEmpty()) {
						item(span = { GridItemSpan(maxLineSpan) }) {
							Text(
								text = stringResource(Res.string.title_top_artists),
								style = MaterialTheme.typography.titleMediumEmphasized,
								fontWeight = FontWeight(600),
								modifier = Modifier
									.heightIn(min = 32.dp)
									.padding(top = 12.dp, start = 16.dp)
									.semantics { heading() }
							)
						}
					}
					items(
						items = state.topArtists,
						key = { it.artist.id },
						span = { GridItemSpan(maxLineSpan) }
					) { stats ->
						val maxPlays = state.topArtists.first().playCount.toFloat()
						TopArtistItem(
							stats = stats,
							ratio = stats.playCount / maxPlays,
							onClick = { backStack.add(Screen.ArtistDetail(stats.artist.id)) }
						)
					}

					item(span = { GridItemSpan(maxLineSpan) }) {
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
									backStack.add(Screen.CollectionDetail(album.album.id, "stats"))
								}
							)
						}
					}

					item(span = { GridItemSpan(maxLineSpan) }) {
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
									backStack.add(
										Screen.SongDetailScreen(
											song.song.id,
											song.song.coverArtId
										)
									)
								}
							)
						}
					}
				}
			}
		}
	}
}
