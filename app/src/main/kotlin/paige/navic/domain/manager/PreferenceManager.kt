/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager

import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import paige.navic.discord.ExoDiscordIntegration
import paige.navic.domain.manager.base.BasePreferenceManager
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainArtistListType
import paige.navic.domain.model.DomainPlaylistListType
import paige.navic.domain.model.DomainSongListType
import paige.navic.domain.model.settings.AnimationStyle
import paige.navic.domain.model.settings.AppIconVariant
import paige.navic.domain.model.settings.ArtistAlbumViewMode
import paige.navic.domain.model.settings.BottomBarCollapseMode
import paige.navic.domain.model.settings.BottomBarVisibilityMode
import paige.navic.domain.model.settings.CoverArtQuality
import paige.navic.domain.model.settings.CoverArtShape
import paige.navic.domain.model.settings.CoverArtTapAction
import paige.navic.domain.model.settings.ExplicitContentPlayback
import paige.navic.domain.model.settings.FontOption
import paige.navic.domain.model.settings.GridSize
import paige.navic.domain.model.settings.ListViewMode
import paige.navic.domain.model.settings.MarqueeSpeed
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
import com.russhwolf.settings.Settings as KmpSettings

class PreferenceManager(
	settings: KmpSettings
) : BasePreferenceManager(settings) {
    var queueInfoType by (preference(QueueInfoType.Full))
    var appIconVariant by preference(AppIconVariant.Default)
	var font by preference(FontOption.GoogleSans)
	var fontPath by preference("")
	var animationStyle by preference(AnimationStyle.Expressive)
	var nowPlayingBackgroundStyle by preference(NowPlayingBackgroundStyle.Dynamic)
	var swipeToSkip by preference(true)
	var hideIfIdle by preference(false)
	var enablePredictiveBackAnimations by preference(true)
	var gridSize by preference(GridSize.TwoByTwo)
	var coverArtShape by preference(CoverArtShape.Soft)
	var artistImageShape by preference(CoverArtShape.Soft)
	var coverArtQuality by preference(CoverArtQuality.High)
	var artGridItemSize by preference(150f)
	var marqueeSpeed by preference(MarqueeSpeed.Slow)
	var alphabeticalScroll by preference(false)
	var enableRatings by preference(true)
	var lyricsAutoscroll by preference(true)
	var lyricsBeatByBeat by preference(true)
	var lyricsFakeBeatByBeat by preference(false)
	var lyricsKeepAlive by preference(true)
	var lyricsBlur by preference(false)
	var lyricsBrightInactive by preference(false)
	var enableScrobbling by preference(true)
	var scrobblePercentage by preference(.5f)
	var minDurationToScrobble by preference(30f)
	var replayGainMode by preference(ReplayGainMode.Off)
	var rgAmpGain by preference(0f)
	var ampGain by preference(0f)
	var gaplessPlayback by preference(true)
	var audioOffload by preference(false)

	// TODO: better names and strings for these transcoding settings
	var streamingQualityWifi by preference(StreamingQuality.Lossless)
	var streamingQualityCellular by preference(StreamingQuality.Lossless)
	var isAdvancedTranscodingActive by preference(false)
	var customMaxBitrateWifi by preference(0)
	var customMaxBitrateCellular by preference(0)
	var customFormatWifi by preference("")
	var customFormatCellular by preference("")

	var downloadQualityWifi by preference(StreamingQuality.Lossless)
	var downloadQualityCellular by preference(StreamingQuality.Lossless)
	var isAdvancedDownloadTranscodingActive by preference(false)
	var customDownloadMaxBitrateWifi by preference(0)
	var customDownloadMaxBitrateCellular by preference(0)
	var customDownloadFormatWifi by preference("")
	var customDownloadFormatCellular by preference("")

	var nowPlayingToolbarPosition by preference(ToolbarPosition.Bottom)
	var nowPlayingSongInfo by preference(true)
	var nowPlayingSliderStyle by preference(NowPlayingSliderStyle.Squiggly)
	var nowPlayingCoverArtAction by preference(CoverArtTapAction.ShowLyrics)
	var customHeaders by preference("")
	var checkForUpdates by preference(true)
	var explicitContentPlayback by preference(ExplicitContentPlayback.Allowed)
	var autoFillQueue by preference(false)
	var shushQueueDuplicateDialog by preference(false)

	// navigation bar settings
	var bottomBarCollapseMode by preference(BottomBarCollapseMode.OnScroll)
	var bottomBarVisibilityMode by preference(BottomBarVisibilityMode.AllScreens)
	var miniPlayerStyle by preference(MiniPlayerStyle.Detached)
	var miniPlayerProgressStyle by preference(MiniPlayerProgressStyle.Seekable)

	// theme related settings
	var theme by preference(Theme.Dynamic)
	var themeMode by preference(ThemeMode.System)
	var amoled by preference(false)
	var dynamicTheming by preference(false)
	var paletteStyle by preference(PaletteStyle.TonalSpot)
	var paletteSpec by preference(ColorSpec.SpecVersion.SPEC_2025)
	var paletteAccentH by preference(0f)
	var paletteAccentS by preference(1f)
	var paletteAccentV by preference(1f)

	// sync related settings
	var lastFullSyncTime by preference(0L)

	// sorting/view mode preferences
	var albumListViewMode by preference(ListViewMode.Grid)
	var playlistListViewMode by preference(ListViewMode.List)
	var artistListViewMode by preference(ListViewMode.List)
	var artistAlbumViewMode by preference(ArtistAlbumViewMode.Grid)

	var albumSortType by jsonPreference<DomainAlbumListType>(DomainAlbumListType.AlphabeticalByArtist)
	var albumSortReversed by preference(false)

	var songSortType by jsonPreference<DomainSongListType>(DomainSongListType.FrequentlyPlayed)
	var songSortReversed by preference(false)

	var artistSortType by preference(DomainArtistListType.AlphabeticalByName)

	var playlistSortType by preference(DomainPlaylistListType.Name)
	var playlistSortReversed by preference(false)

	// these values are bitmasks of `DomainFilter`
	var albumFilters by preference(0)
	var songFilters by preference(0)
	var artistFilters by preference(0)
	var playlistFilters by preference(0)

	fun customHeadersMap(): Map<String, String> = buildMap {
		for (line in customHeaders.lines()) {
			val parts = line.split(":", limit = 2)
			if (parts.size < 2) continue

			val rawKey = parts[0]
			val rawValue = parts[1]

			val key = rawKey.trim()
			val value = rawValue.trim()
			if (key.isNotEmpty() && value.isNotEmpty()) put(key, value)
		}
	}

	var offlineMode by preference(OfflineMode.Auto)

	var enableDiscordIntegration by preference(false)
	var discordAppId by preference(ExoDiscordIntegration.DISCORD_APPLICATION_ID)
	var proxyUrl by preference("")
	var dangerousSslNoopEnabled by preference(false)
}
