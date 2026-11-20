/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.kmpalette.rememberDominantColorState
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import org.koin.compose.koinInject
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.model.settings.ThemeMode
import paige.navic.playback.PlayerRepository
import paige.navic.preferences.PreferenceManager
import coil3.compose.LocalPlatformContext as LocalCoilPlatformContext

@Composable
fun rememberColorSchemeFromCoverArt(
	coverArtId: String?,
	forceDark: Boolean = false,
	style: PaletteStyle = PaletteStyle.Content,
	specVersion: ColorSpec.SpecVersion = ColorSpec.SpecVersion.SPEC_2021
): ColorScheme {
	val sessionManager = koinInject<SessionManager>()
	val coverArtUri = remember(coverArtId) {
		coverArtId?.let { sessionManager.getCoverArtUrl(it) }
	}

	val coilPlatformContext = LocalCoilPlatformContext.current
	val loader = koinInject<ImageLoader>()
	val model = remember(coverArtUri) {
		ImageRequest.Builder(coilPlatformContext)
			.data(coverArtUri)
			.memoryCacheKey(coverArtId)
			.diskCacheKey(coverArtId)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.build()
	}
	val dominantColorState = rememberDominantColorState(cacheSize = 4)

	LaunchedEffect(model) {
		val result = loader.execute(model)
		result.image?.toImageBitmap()?.let { imageBitmap ->
			dominantColorState.updateFrom(imageBitmap)
		}
	}

	val preferenceManager = koinInject<PreferenceManager>()
	val inDarkTheme = isSystemInDarkTheme()
	val isDark = remember(forceDark, preferenceManager.themeMode) {
		forceDark || when (preferenceManager.themeMode) {
			ThemeMode.System -> inDarkTheme
			ThemeMode.Dark -> true
			ThemeMode.Light -> false
		}
	}
	val scheme = rememberDynamicColorScheme(
		seedColor = dominantColorState.color,
		isDark = isDark,
		style = style,
		specVersion = specVersion,
		isAmoled = preferenceManager.amoled
	)

	return scheme
}

@Composable
fun rememberColorSchemeForCurrentSong(forceDark: Boolean = true): ColorScheme {
	val player = koinInject<PlayerRepository>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val coverArtId = playerState.currentSong?.coverArtId
	return rememberColorSchemeFromCoverArt(
		coverArtId = coverArtId,
		forceDark = forceDark,
		style = if (coverArtId != null) PaletteStyle.Content else PaletteStyle.Monochrome
	)
}
