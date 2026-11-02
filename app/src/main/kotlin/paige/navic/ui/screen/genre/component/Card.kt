/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.genre.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.materialkolor.MaterialKolors
import com.materialkolor.ktx.DynamicScheme
import dev.zt64.compose.pipette.HsvColor
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.model.DomainGenre
import paige.navic.domain.model.settings.ThemeMode
import paige.navic.ui.component.common.CoverArt
import paige.navic.ui.navigation.Screen
import paige.navic.ui.theme.defaultFont
import kotlin.math.abs

@Composable
fun GenreListScreenCard(
	modifier: Modifier = Modifier,
	genre: DomainGenre
) {
	val backStack = LocalNavStack.current
	val inDarkTheme = isSystemInDarkTheme()
	val preferenceManager = koinInject<PreferenceManager>()
	val isDark = remember(preferenceManager.themeMode) {
		when (preferenceManager.themeMode) {
			ThemeMode.System -> inDarkTheme
			ThemeMode.Dark -> true
			ThemeMode.Light -> false
		}
	}
	val seedColor = remember(genre.name) {
		HsvColor(
			hue = abs(genre.name.hashCode() % 360).toFloat(),
			saturation = 0.6f,
			value = 0.5f
		).toColor()
	}
	val (backgroundColor, contentColor) = remember(seedColor, isDark) {
		val colors = MaterialKolors(
			DynamicScheme(seedColor = seedColor, isDark = isDark)
		)
		colors.primary() to colors.onPrimary()
	}
	val firstAlbumCoverArt = genre.coverArtIds.firstOrNull()
	val secondAlbumCoverArt = genre.coverArtIds.getOrNull(1)
	var coverLoads by remember { mutableIntStateOf(0) }
	val coverLayer = rememberGraphicsLayer()
	val lastRecordedLoads = remember { intArrayOf(-1) }
	Surface(
		modifier = modifier,
		color = backgroundColor,
		contentColor = contentColor,
		shape = MaterialTheme.shapes.medium,
		shadowElevation = 0.dp,
		onClick = dropUnlessResumed {
			backStack.add(Screen.GenreDetail(genreName = genre.name))
		}
	) {
		Box {
			Box(
				Modifier
					.align(Alignment.CenterEnd)
					.drawWithContent {
						if (lastRecordedLoads[0] != coverLoads) {
							coverLayer.record { this@drawWithContent.drawContent() }
							lastRecordedLoads[0] = coverLoads
						}
						drawLayer(coverLayer)
					}
			) {
				if (firstAlbumCoverArt != null) {
					CoverArt(
						coverArtId = firstAlbumCoverArt,
						modifier = Modifier
							.padding(top = 10.dp)
							.size(90.dp)
							.offset(x = 5.dp, y = 5.dp),
						shape = MaterialTheme.shapes.medium,
						shadowElevation = 0.dp,
						rotationZ = 10f,
						square = false,
						crossfadeMs = 0,
						onLoad = { coverLoads++ }
					)
					if (secondAlbumCoverArt != null) {
						CoverArt(
							coverArtId = secondAlbumCoverArt,
							modifier = Modifier
								.padding(top = 13.dp)
								.size(90.dp)
								.offset(x = 25.dp, y = 15.dp),
							shape = MaterialTheme.shapes.medium,
							shadowElevation = 0.dp,
							fakeShadowElevation = 10.dp,
							rotationZ = 25f,
							square = false,
							crossfadeMs = 0,
							onLoad = { coverLoads++ }
						)
					}
				}
			}
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(
						start = 8.dp,
						end = 85.dp,
						top = 10.dp,
						bottom = 10.dp
					)
					.align(Alignment.TopStart)
			) {
				Text(
					genre.name,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight(600),
					fontFamily = defaultFont(round = 100f),
					overflow = TextOverflow.Ellipsis,
					maxLines = 2
				)
				Text(
					pluralStringResource(
						R.plurals.count_albums,
						genre.albumCount,
						genre.albumCount
					),
					style = MaterialTheme.typography.titleSmall,
				)
			}
		}
	}
}
