/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import paige.navic.R
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainSongListType
import paige.navic.domain.model.lyrics.LyricsProvider
import paige.navic.domain.model.settings.AnimationStyle
import paige.navic.domain.model.settings.ArtistAlbumViewMode
import paige.navic.domain.model.settings.BottomBarCollapseMode
import paige.navic.domain.model.settings.BottomBarVisibilityMode
import paige.navic.domain.model.settings.CoverArtQuality
import paige.navic.domain.model.settings.CoverArtTapAction
import paige.navic.domain.model.settings.DiscordStatusType
import paige.navic.domain.model.settings.DownloadStorage
import paige.navic.domain.model.settings.EqualizerMode
import paige.navic.domain.model.settings.ExplicitContentPlayback
import paige.navic.domain.model.settings.ListViewMode
import paige.navic.domain.model.settings.MiniPlayerProgressStyle
import paige.navic.domain.model.settings.MiniPlayerStyle
import paige.navic.domain.model.settings.NowPlayingBackgroundStyle
import paige.navic.domain.model.settings.NowPlayingSliderStyle
import paige.navic.domain.model.settings.OfflineMode
import paige.navic.domain.model.settings.QueueInfoType
import paige.navic.domain.model.settings.ReplayGainMode
import paige.navic.domain.model.settings.StreamingQuality
import paige.navic.domain.model.settings.Theme
import paige.navic.domain.model.settings.ThemeMode
import paige.navic.domain.model.settings.ToolbarPosition
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@Composable
fun Duration.label(): String {
	val hours = inWholeHours.toInt()
	val minutes = (this - hours.hours).inWholeMinutes.toInt()

	return when {
		hours > 0 && minutes > 0 ->
			"${pluralStringResource(R.plurals.count_hours, hours, hours)} ${
				pluralStringResource(
					R.plurals.count_minutes,
					minutes,
					minutes
				)
			}"

		hours > 0 ->
			pluralStringResource(R.plurals.count_hours, hours, hours)

		else ->
			pluralStringResource(R.plurals.count_minutes, max(1, minutes), max(1, minutes))
	}
}

@Composable
fun DomainAlbumListType.label() = when (this) {
	DomainAlbumListType.Random -> stringResource(R.string.option_sort_random)
	DomainAlbumListType.Newest -> stringResource(R.string.option_sort_newest)
	DomainAlbumListType.Frequent -> stringResource(R.string.option_sort_frequent)
	DomainAlbumListType.Recent -> stringResource(R.string.option_sort_recent)
	DomainAlbumListType.AlphabeticalByName -> stringResource(R.string.option_sort_alphabetical_by_name)
	DomainAlbumListType.AlphabeticalByArtist -> stringResource(R.string.option_sort_alphabetical_by_artist)
	DomainAlbumListType.Highest -> stringResource(R.string.option_sort_rating)
	DomainAlbumListType.Year -> stringResource(R.string.option_sort_year)
	is DomainAlbumListType.ByGenre -> stringResource(R.string.option_sort_by_genre)
	is DomainAlbumListType.ByYear -> stringResource(R.string.option_sort_by_year)
}

@Composable
fun DomainSongListType.label() = when (this) {
	is DomainSongListType.ByArtist -> stringResource(R.string.option_sort_by_genre)
	is DomainSongListType.ByGenre -> stringResource(R.string.option_sort_by_genre)
	DomainSongListType.FrequentlyPlayed -> stringResource(R.string.option_sort_frequent)
	DomainSongListType.Newest -> stringResource(R.string.option_sort_newest)
	DomainSongListType.Random -> stringResource(R.string.option_sort_random)
	DomainSongListType.Rating -> stringResource(R.string.option_sort_rating)
	DomainSongListType.Year -> stringResource(R.string.option_sort_by_year)
}

fun PaletteStyle.label(): String = when (this) {
	PaletteStyle.TonalSpot -> "Tonal Spot"
	PaletteStyle.Neutral -> "Neutral"
	PaletteStyle.Vibrant -> "Vibrant"
	PaletteStyle.Expressive -> "Expressive"
	PaletteStyle.Rainbow -> "Rainbow"
	PaletteStyle.FruitSalad -> "Fruit Salad"
	PaletteStyle.Monochrome -> "Monochrome"
	PaletteStyle.Fidelity -> "Fidelity"
	PaletteStyle.Content -> "Content"
}

fun ColorSpec.SpecVersion.label() = when (this) {
	ColorSpec.SpecVersion.SPEC_2021 -> "Material 3 (2021)"
	ColorSpec.SpecVersion.SPEC_2025 -> "Expressive (2025)"
}

fun LyricsProvider.Id.label() = when (this) {
	LyricsProvider.Id.SUBSONIC -> "Subsonic"
	LyricsProvider.Id.LYRICS_PLUS -> "LyricsPlus"
	LyricsProvider.Id.LRCLIB -> "LRCLIB"
}

@Composable
fun ThemeMode.label() = when (this) {
	ThemeMode.System -> stringResource(R.string.theme_mode_system)
	ThemeMode.Dark -> stringResource(R.string.theme_mode_dark)
	ThemeMode.Light -> stringResource(R.string.theme_mode_light)
}

@Composable
fun AnimationStyle.label() = when (this) {
	AnimationStyle.Expressive -> stringResource(R.string.option_animation_style_expressive)
	AnimationStyle.Standard -> stringResource(R.string.option_animation_style_standard)
}

@Composable
fun ArtistAlbumViewMode.label() = when (this) {
	ArtistAlbumViewMode.List -> stringResource(R.string.option_list_view_mode_list)
	ArtistAlbumViewMode.Grid -> stringResource(R.string.option_list_view_mode_grid)
}

@Composable
fun BottomBarCollapseMode.label() = when (this) {
	BottomBarCollapseMode.Never -> stringResource(R.string.option_bottom_bar_collapse_mode_never)
	BottomBarCollapseMode.OnScroll -> stringResource(R.string.option_bottom_bar_collapse_mode_on_scroll)
}

@Composable
fun BottomBarVisibilityMode.label() = when (this) {
	BottomBarVisibilityMode.Default -> stringResource(R.string.option_bottom_bar_visibility_mode_default)
	BottomBarVisibilityMode.AllScreens -> stringResource(R.string.option_bottom_bar_visibility_mode_all_screens)
}

@Composable
fun CoverArtQuality.label() = when (this) {
	CoverArtQuality.Low -> stringResource(R.string.option_quality_low)
	CoverArtQuality.Medium -> stringResource(R.string.option_quality_medium)
	CoverArtQuality.High -> stringResource(R.string.option_quality_high)
}

@Composable
fun DiscordStatusType.label() = when (this) {
	DiscordStatusType.ArtistName -> stringResource(R.string.option_discord_rpc_status_type_artist_name)
	DiscordStatusType.SongName -> stringResource(R.string.option_discord_rpc_status_type_song_name)
	DiscordStatusType.AppName -> stringResource(R.string.option_discord_rpc_status_type_app_name)
}

@Composable
fun DownloadStorage.label() = when (this) {
	DownloadStorage.Internal -> stringResource(R.string.option_download_storage_internal)
	DownloadStorage.External -> stringResource(R.string.option_download_storage_external)
}

@Composable
fun EqualizerMode.label() = when (this) {
	EqualizerMode.Disabled -> stringResource(R.string.option_equalizer_mode_disabled)
	EqualizerMode.BuiltIn -> stringResource(R.string.option_equalizer_mode_builtin)
	EqualizerMode.External -> stringResource(R.string.option_equalizer_mode_external)
}

@Composable
fun ExplicitContentPlayback.label() = when (this) {
	ExplicitContentPlayback.Allowed -> stringResource(R.string.option_explicit_playback_allowed)
	ExplicitContentPlayback.Skip -> stringResource(R.string.option_explicit_playback_skip)
	ExplicitContentPlayback.SkipForThisSession -> stringResource(R.string.option_explicit_playback_skip_session)
}

@Composable
fun ListViewMode.label() = when (this) {
	ListViewMode.Grid -> stringResource(R.string.option_list_view_mode_grid)
	ListViewMode.List -> stringResource(R.string.option_list_view_mode_list)
}

@Composable
fun MiniPlayerProgressStyle.label() = when (this) {
	MiniPlayerProgressStyle.Hidden -> stringResource(R.string.option_mini_player_progress_style_hidden)
	MiniPlayerProgressStyle.Line -> stringResource(R.string.option_mini_player_progress_style_line)
	MiniPlayerProgressStyle.Overlay -> stringResource(R.string.option_mini_player_progress_style_overlay)
}

@Composable
fun MiniPlayerStyle.label() = when (this) {
	MiniPlayerStyle.Unified -> stringResource(R.string.option_mini_player_style_unified)
	MiniPlayerStyle.Detached -> stringResource(R.string.option_mini_player_style_detached)
}

@Composable
fun NowPlayingBackgroundStyle.label() = when (this) {
	NowPlayingBackgroundStyle.Static -> stringResource(R.string.option_now_playing_background_style_static)
	NowPlayingBackgroundStyle.Dynamic -> stringResource(R.string.option_now_playing_background_style_dynamic)
}

@Composable
fun NowPlayingSliderStyle.label() = when (this) {
	NowPlayingSliderStyle.Flat -> stringResource(R.string.option_now_playing_slider_style_flat)
	NowPlayingSliderStyle.Squiggly -> stringResource(R.string.option_now_playing_slider_style_squiggly)
	NowPlayingSliderStyle.Slim -> stringResource(R.string.option_now_playing_slider_style_slim)
	NowPlayingSliderStyle.Yoyo -> stringResource(R.string.option_now_playing_slider_style_yoyo)
}

@Composable
fun OfflineMode.label() = when (this) {
	OfflineMode.Auto -> stringResource(R.string.option_offline_mode_auto)
	OfflineMode.Forced -> stringResource(R.string.option_offline_mode_forced)
	OfflineMode.NoWiFi -> stringResource(R.string.option_offline_mode_no_wifi)
}

@Composable
fun QueueInfoType.label() = when (this) {
	QueueInfoType.Full -> stringResource(R.string.option_queue_info_type_full)
	QueueInfoType.Remaining -> stringResource(R.string.option_queue_info_type_remining)
}

@Composable
fun ReplayGainMode.label() = when (this) {
	ReplayGainMode.Off -> stringResource(R.string.option_off)
	ReplayGainMode.Track -> stringResource(R.string.info_track_replay_gain)
	ReplayGainMode.Album -> stringResource(R.string.info_album_replay_gain)
	ReplayGainMode.Dynamic -> stringResource(R.string.info_dynamic_replay_gain)
}

@Composable
fun StreamingQuality.label() = when (this) {
	StreamingQuality.Low -> stringResource(R.string.option_quality_low)
	StreamingQuality.Medium -> stringResource(R.string.option_quality_medium)
	StreamingQuality.High -> stringResource(R.string.option_quality_high)
	StreamingQuality.Lossless -> stringResource(R.string.option_quality_lossless)
}

@Composable
fun Theme.label() = when (this) {
	Theme.AppleMusic -> stringResource(R.string.theme_apple_music)
	Theme.iOS -> stringResource(R.string.theme_ios)
	Theme.Seeded -> stringResource(R.string.theme_seeded)
	Theme.Dynamic -> stringResource(R.string.theme_dynamic)
	Theme.Spotify -> stringResource(R.string.theme_spotify)
}

@Composable
fun ToolbarPosition.label() = when(this) {
	ToolbarPosition.Top -> stringResource(R.string.option_position_top)
	ToolbarPosition.Bottom -> stringResource(R.string.option_position_bottom)
}

@Composable
fun CoverArtTapAction.label() = when (this) {
	CoverArtTapAction.Disabled -> stringResource(R.string.option_cover_art_action_disabled)
	CoverArtTapAction.ShowLyrics -> stringResource(R.string.option_cover_art_action_show_lyrics)
}
