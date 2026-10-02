/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.detailPane
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.listPane
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.NavDisplay.popTransitionSpec
import androidx.navigation3.ui.NavDisplay.predictivePopTransitionSpec
import androidx.navigation3.ui.NavDisplay.transitionSpec
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.koinInject
import paige.navic.di.LocalBottomBarScrollManager
import paige.navic.di.LocalNavStack
import paige.navic.di.LocalSharedTransitionScope
import paige.navic.di.LocalSizeClass
import paige.navic.di.LocalSnackBarState
import paige.navic.domain.manager.BottomBarScrollManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.SnackBarManager
import paige.navic.domain.model.settings.ExplicitContentPlayback
import paige.navic.generated.BuildInfo
import paige.navic.playback.MediaPlayerViewModel
import paige.navic.ui.component.layout.SideBar
import paige.navic.ui.component.sheet.ChangelogSheet
import paige.navic.ui.component.snackbar.NavicSnackBar
import paige.navic.ui.navigation.BottomSheetSceneStrategy
import paige.navic.ui.navigation.NowPlayingSceneStrategy
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.album.AlbumListScreen
import paige.navic.ui.screen.artist.ArtistDetailScreen
import paige.navic.ui.screen.artist.ArtistListScreen
import paige.navic.ui.screen.chat.ChatScreen
import paige.navic.ui.screen.collection.CollectionDetailScreen
import paige.navic.ui.screen.genre.GenreDetailScreen
import paige.navic.ui.screen.genre.GenreListScreen
import paige.navic.ui.screen.imageView.ImageViewScreen
import paige.navic.ui.screen.library.LibraryScreen
import paige.navic.ui.screen.login.LoginScreen
import paige.navic.ui.screen.lyrics.LyricsScreen
import paige.navic.ui.screen.nowPlaying.NowPlayingScreen
import paige.navic.ui.screen.nowPlaying.PlaybackSpeedScreen
import paige.navic.ui.screen.playlist.PlaylistListScreen
import paige.navic.ui.screen.queue.QueueScreen
import paige.navic.ui.screen.radio.RadioListScreen
import paige.navic.ui.screen.search.SearchScreen
import paige.navic.ui.screen.settings.AudioEffectsScreen
import paige.navic.ui.screen.settings.BottomBarScreen
import paige.navic.ui.screen.settings.FontsScreen
import paige.navic.ui.screen.settings.SettingsAboutScreen
import paige.navic.ui.screen.settings.SettingsAppIconScreen
import paige.navic.ui.screen.settings.SettingsAppearanceScreen
import paige.navic.ui.screen.settings.SettingsConnectionOptionsScreen
import paige.navic.ui.screen.settings.SettingsCustomHeadersScreen
import paige.navic.ui.screen.settings.SettingsDataStorageScreen
import paige.navic.ui.screen.settings.SettingsDeveloperScreen
import paige.navic.ui.screen.settings.SettingsDownloadQualityScreen
import paige.navic.ui.screen.settings.SettingsEqualizerScreen
import paige.navic.ui.screen.settings.SettingsLogsScreen
import paige.navic.ui.screen.settings.SettingsNowPlayingScreen
import paige.navic.ui.screen.settings.SettingsPlaybackScreen
import paige.navic.ui.screen.settings.SettingsScreen
import paige.navic.ui.screen.settings.SettingsStreamingQualityScreen
import paige.navic.ui.screen.settings.SettingsThemesScreen
import paige.navic.ui.screen.share.ShareListScreen
import paige.navic.ui.screen.song.SongDetailScreen
import paige.navic.ui.screen.song.SongDetailSheet
import paige.navic.ui.screen.song.SongListScreen
import paige.navic.ui.screen.starred.StarredScreen
import paige.navic.ui.screen.stats.StatisticsScreen
import paige.navic.ui.theme.NavicTheme
import paige.navic.ui.util.Material3Transitions

@OptIn(ExperimentalSerializationApi::class)
private val config = SavedStateConfiguration {
	serializersModule = SerializersModule {
		polymorphic(NavKey::class) {
			subclassesOfSealed<Screen>()
		}
	}
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun App() {
	val density = LocalDensity.current
	val activity = LocalActivity.current!!
	val resources = LocalResources.current

	val sizeClass = calculateWindowSizeClass(activity)
	val sessionManager = koinInject<SessionManager>()
	val preferenceManager = koinInject<PreferenceManager>()

	val isLoggedIn by sessionManager.isLoggedIn.collectAsStateWithLifecycle()
	val backStack = rememberNavBackStack(
		config, if (isLoggedIn) {
			Screen.Library()
		} else {
			Screen.Login
		}
	)
	val snackBarState = remember { SnackbarHostState() }
	val snackBarManager = koinInject<SnackBarManager>()

	LaunchedEffect(Unit) {
		snackBarManager.events.collectLatest { event ->
			snackBarState.showSnackbar(
				resources.getString(event.resource, *event.args.toTypedArray())
			)
		}
	}

	val scrollManager = remember {
		BottomBarScrollManager(with(density) { 50.dp.toPx() })
	}

	var appStarted by rememberSaveable { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		if (!appStarted) {
			appStarted = true
			if (preferenceManager.explicitContentPlayback == ExplicitContentPlayback.SkipForThisSession) {
				preferenceManager.explicitContentPlayback = ExplicitContentPlayback.Allowed
			}
		}
	}

	SharedTransitionLayout {
		CompositionLocalProvider(
			LocalNavStack provides backStack,
			LocalSnackBarState provides snackBarState,
			LocalSharedTransitionScope provides this@SharedTransitionLayout,
			LocalBottomBarScrollManager provides scrollManager,
			LocalSizeClass provides sizeClass
		) {
			NavicTheme {
				Row(modifier = Modifier.fillMaxSize()) {
					if (sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
						&& Screen.Login !in backStack) {
						SideBar()
					}
					@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
					Scaffold(
						modifier = Modifier.nestedScroll(scrollManager.connection),
						snackbarHost = {
							SnackbarHost(hostState = snackBarState) { snackBarData ->
								NavicSnackBar(snackBarData = snackBarData)
							}
						}
					) { _ ->
						NavDisplay(
							modifier = Modifier
								.weight(1f)
								.consumeWindowInsets(
									WindowInsets.safeDrawing.only(WindowInsetsSides.Start)
								),
							backStack = backStack,
							sceneStrategies = listOf(
								remember { NowPlayingSceneStrategy() },
								remember { BottomSheetSceneStrategy() },
								rememberListDetailSceneStrategy()
							),
							entryDecorators = listOf(
								rememberSaveableStateHolderNavEntryDecorator(),

								// makes it so that ViewModels get destroyed if their
								// associated screen is removed from the back stack
								//
								// this might not always be desirable, so the
								// `PersistentViewModelStoreOwner` class is used for
								// certain ViewModels to work around this
								rememberViewModelStoreNavEntryDecorator()
							),
							onBack = {
								if (backStack.size >= 2) {
									backStack.removeLastOrNull()
								}
							},
							entryProvider = entryProvider(backStack),
							sharedTransitionScope = this@SharedTransitionLayout,
							transitionSpec = {
								Material3Transitions.SharedXAxisEnterTransition(
									density
								) togetherWith Material3Transitions.SharedXAxisExitTransition(
									density
								)
							},
							popTransitionSpec = {
								Material3Transitions.SharedXAxisPopEnterTransition(
									density
								) togetherWith Material3Transitions.SharedXAxisPopExitTransition(
									density
								)
							},
							predictivePopTransitionSpec = {
								if (preferenceManager.enablePredictiveBackAnimations) {
									slideInHorizontally(
										animationSpec = tween(300, easing = EaseOutQuart),
										initialOffsetX = { -it }
									) togetherWith slideOutHorizontally(
										animationSpec = tween(300, easing = EaseOutQuart),
										targetOffsetX = { it }
									)
								} else {
									ContentTransform(EnterTransition.None, ExitTransition.None)
								}
							}
						)
					}
				}
				if (preferenceManager.checkForUpdates && !BuildInfo.FDROID) {
					ChangelogSheet()
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
private fun entryProvider(
	backStack: NavBackStack<NavKey>
): (NavKey) -> (NavEntry<NavKey>) {
	val fadeSpec = ContentTransform(fadeIn(), fadeOut())

	val navtabMetadata = if (backStack.size == 1)
		listPane("root")
			.plus(transitionSpec { fadeSpec })
			.plus(popTransitionSpec { fadeSpec })
			.plus(predictivePopTransitionSpec { fadeSpec })
	else listPane("root")
	val imageViewMetadata = transitionSpec { ContentTransform(fadeIn(), ExitTransition.None) }
		.plus(popTransitionSpec { ContentTransform(EnterTransition.None, fadeOut()) })
		.plus(predictivePopTransitionSpec { ContentTransform(EnterTransition.None, fadeOut()) })

	return androidx.navigation3.runtime.entryProvider {
		// tabs
		entry<Screen.Library>(metadata = navtabMetadata) {
			LibraryScreen()
		}
		entry<Screen.Starred>(metadata = navtabMetadata) {
			StarredScreen()
		}
		entry<Screen.AlbumList>(metadata = navtabMetadata) { key ->
			AlbumListScreen(key.nested, key.listType)
		}
		entry<Screen.PlaylistList>(metadata = navtabMetadata) { key ->
			PlaylistListScreen(key.nested)
		}
		entry<Screen.ArtistList>(metadata = navtabMetadata) { key ->
			ArtistListScreen(key.nested, key.listType)
		}
		entry<Screen.GenreList>(metadata = navtabMetadata) { key ->
			GenreListScreen(key.nested)
		}
		entry<Screen.GenreDetail> { key ->
			GenreDetailScreen(key.genreName)
		}
		entry<Screen.SongList>(metadata = navtabMetadata) { key ->
			SongListScreen(key.nested, key.listType)
		}

		entry<Screen.RadioList>(metadata = navtabMetadata) { key ->
			RadioListScreen(key.nested)
		}

		// misc
		entry<Screen.Login> {
			LoginScreen()
		}
		entry<Screen.ImageView>(metadata = imageViewMetadata) { key ->
			ImageViewScreen(
				coverArtId = key.coverArtId,
				title = key.title,
				sharedTransitionKey = key.sharedTransitionKey
			)
		}
		entry<Screen.Chat> {
			ChatScreen()
		}
		entry<Screen.NowPlaying>(
			metadata = NowPlayingSceneStrategy.bottomSheet(maxWidth = Dp.Unspecified)
		) {
			NowPlayingScreen()
		}
		entry<Screen.Lyrics>(metadata = NowPlayingSceneStrategy.bottomSheet(isTransparent = true)) {
			val player = koinInject<MediaPlayerViewModel>()
			val playerState by player.uiState.collectAsState()
			val song = playerState.currentSong
			LyricsScreen(song)
		}
		entry<Screen.Queue>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
			QueueScreen()
		}
		entry<Screen.PlaybackSpeed>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
			PlaybackSpeedScreen()
		}
		entry<Screen.CollectionDetail>(metadata = detailPane("root")) { key ->
			CollectionDetailScreen(key.collectionId, key.tab)
		}
		entry<Screen.SongDetailScreen> { key ->
			SongDetailScreen(
				songId = key.songId,
				initialCoverArtId = key.coverArtId
			)
		}
		entry<Screen.SongDetailSheet>(
			metadata = { key ->
				BottomSheetSceneStrategy.bottomSheet(coverArtId = key.coverArtId)
			}
		) { key ->
			SongDetailSheet(
				songId = key.songId,
				initialCoverArtId = key.coverArtId
			)
		}
		entry<Screen.Search>(metadata = navtabMetadata) { key ->
			SearchScreen(key.nested)
		}
		entry<Screen.ShareList> {
			ShareListScreen()
		}
		entry<Screen.ArtistDetail> { key ->
			ArtistDetailScreen(key.artist)
		}

		entry<Screen.Statistics>(metadata = navtabMetadata) { key ->
			StatisticsScreen(key.nested)
		}

		// settings
		entry<Screen.Settings.Root>(metadata = listPane("settings")) {
			SettingsScreen()
		}
		entry<Screen.Settings.Appearance>(metadata = detailPane("settings")) {
			SettingsAppearanceScreen()
		}
		entry<Screen.Settings.BottomAppBar>(metadata = detailPane("settings")) {
			BottomBarScreen()
		}
		entry<Screen.Settings.NowPlaying>(metadata = detailPane("settings")) {
			SettingsNowPlayingScreen()
		}
		entry<Screen.Settings.Playback>(metadata = detailPane("settings")) {
			SettingsPlaybackScreen()
		}
		entry<Screen.Settings.Effects>(metadata = detailPane("settings")) {
			AudioEffectsScreen()
		}
		entry<Screen.Settings.Developer>(metadata = detailPane("settings")) {
			SettingsDeveloperScreen()
		}
		entry<Screen.Settings.About>(metadata = detailPane("settings")) {
			SettingsAboutScreen()
		}
		entry<Screen.Settings.DataStorage>(metadata = detailPane("settings")) {
			SettingsDataStorageScreen()
		}
		entry<Screen.Settings.Fonts> {
			FontsScreen()
		}
		entry<Screen.Settings.Themes> {
			SettingsThemesScreen()
		}
		entry<Screen.Settings.ConnectionOptions> {
			SettingsConnectionOptionsScreen()
		}
		entry<Screen.Settings.CustomHeaders> {
			SettingsCustomHeadersScreen()
		}
		entry<Screen.Settings.StreamingQuality> {
			SettingsStreamingQualityScreen()
		}
		entry<Screen.Settings.DownloadQuality> {
			SettingsDownloadQualityScreen()
		}
		entry<Screen.Settings.Logs> {
			SettingsLogsScreen()
		}
		entry<Screen.Settings.AppIcon>(metadata = detailPane("settings")) {
			SettingsAppIconScreen()
		}
		entry<Screen.Settings.Equalizer> {
			SettingsEqualizerScreen()
		}
	}
}
