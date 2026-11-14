/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.model.settings.MiniPlayerProgressStyle
import paige.navic.domain.model.settings.MiniPlayerStyle
import paige.navic.playback.PlayerRepository
import paige.navic.ui.component.common.CoverArt
import paige.navic.ui.component.common.MarqueeText
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.filled.Note
import paige.navic.ui.icons.filled.SkipNext
import paige.navic.ui.navigation.Screen
import paige.navic.ui.util.appendArtists
import paige.navic.ui.util.playPauseIconPainter

object MiniPlayerDefaults {
	val DetachedContainerShadowRadius = 10.dp
	val UnifiedContainerShadowRadius = 8.dp

	val InterItemSpacing = 10.dp

	val UnifiedInterButtonSpacing = 12.dp
	val DetachedInterButtonSpacing = 8.dp

	val UnifiedContentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
	val DetachedContentPadding = PaddingValues(10.dp)

	val DetachedContainerPadding = PaddingValues(
		start = 12.dp,
		end = 12.dp,
		bottom = 6.dp
	)
	val UnifiedContainerPadding = PaddingValues(0.dp)

	val UnifiedCoverArtSize = 50.dp
	val DetachedCoverArtSize = 48.dp

	val UnifiedButtonIconSize = 32.dp
	val DetachedButtonIconSize = 24.dp

	@Composable
	fun colors(
		containerColor: Color = Color.Unspecified
	) = MiniPlayerColors(
		containerColor = containerColor.takeOrElse { NavigationBarDefaults.containerColor }
	)

	@Composable
	fun buttonColors() = IconButtonDefaults.iconButtonVibrantColors()

	@Composable
	fun shapes(
		detachedShape: Shape? = null,
		unifiedShape: Shape? = null,
		coverShape: Shape? = null,
		loadingCoverShape: Shape? = null
	) = MiniPlayerShapes(
		detachedShape = detachedShape ?: RoundedCornerShape(16.dp),
		unifiedShape = unifiedShape ?: RectangleShape,
		coverShape = coverShape ?: RoundedCornerShape(8.dp),
		loadingCoverShape = loadingCoverShape ?: RoundedCornerShape(46.dp)
	)
}

data class MiniPlayerColors(
	val containerColor: Color
)

data class MiniPlayerShapes(
	val detachedShape: Shape,
	val unifiedShape: Shape,
	val coverShape: Shape,
	val loadingCoverShape: Shape
)

@Composable
fun MiniPlayer(
	modifier: Modifier = Modifier,
	colors: MiniPlayerColors = MiniPlayerDefaults.colors(),
	shapes: MiniPlayerShapes = MiniPlayerDefaults.shapes(),
	enabled: Boolean = true
) {
	val backStack = LocalNavStack.current

	val player = koinInject<PlayerRepository>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val song = playerState.currentSong
	val isInteractive = enabled && song != null

	val preferenceManager = koinInject<PreferenceManager>()
	val isDetached = preferenceManager.miniPlayerStyle == MiniPlayerStyle.Detached

	val containerShadowRadius = if (isDetached)
		MiniPlayerDefaults.DetachedContainerShadowRadius
	else MiniPlayerDefaults.UnifiedContainerShadowRadius
	val contentPadding = if (isDetached)
		MiniPlayerDefaults.DetachedContentPadding
	else MiniPlayerDefaults.UnifiedContentPadding
	val containerPadding = if (isDetached)
		MiniPlayerDefaults.DetachedContainerPadding
	else MiniPlayerDefaults.UnifiedContainerPadding
	val interButtonSpacing = if (isDetached)
		MiniPlayerDefaults.DetachedInterButtonSpacing
	else MiniPlayerDefaults.UnifiedInterButtonSpacing
	val shape = if (isDetached)
		shapes.detachedShape
	else shapes.unifiedShape
	val coverArtShape = if (playerState.isLoading)
		shapes.loadingCoverShape
	else shapes.coverShape
	val coverArtSize = if (isDetached)
		MiniPlayerDefaults.DetachedCoverArtSize
	else MiniPlayerDefaults.UnifiedCoverArtSize
	val buttonIconSize = if (isDetached)
		MiniPlayerDefaults.DetachedButtonIconSize
	else MiniPlayerDefaults.UnifiedButtonIconSize

	Box(
		modifier = modifier
			.padding(containerPadding)
			.dropShadow(
				shape = shape,
				shadow = Shadow(radius = containerShadowRadius, alpha = 0.25f)
			)
			.fillMaxWidth()
			.clip(shape)
			.background(colors.containerColor)
			.clickable(
				onClick = dropUnlessResumed {
					if (Screen.NowPlaying !in backStack) {
						backStack.add(Screen.NowPlaying)
					}
				},
				enabled = enabled
			)
	) {
		when (preferenceManager.miniPlayerProgressStyle) {
			MiniPlayerProgressStyle.Line -> {
				val alignment = if (isDetached) Alignment.BottomStart else Alignment.TopStart
				LineProgressBar(
					modifier = Modifier.align(alignment),
					contentAlignment = alignment,
					progress = playerState.progress,
					enabled = isInteractive
				)
			}

			MiniPlayerProgressStyle.Overlay -> {
				OverlayProgressBar(
					modifier = Modifier.matchParentSize(),
					progress = playerState.progress
				)
			}

			MiniPlayerProgressStyle.Hidden -> {}
		}

		Row(
			modifier = Modifier.fillMaxWidth().padding(contentPadding),
			horizontalArrangement = Arrangement.spacedBy(MiniPlayerDefaults.InterItemSpacing),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(contentAlignment = Alignment.Center) {
				CoverArt(
					modifier = Modifier
						.size(coverArtSize)
						.padding(if (playerState.isLoading) 8.dp else 0.dp),
					coverArtId = song?.coverArtId,
					shape = coverArtShape
				)
				if (song?.coverArtId == null) {
					Icon(
						imageVector = Icons.Filled.Note,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .38f)
					)
				}
				this@Row.AnimatedVisibility(
					visible = playerState.isLoading,
					modifier = Modifier.matchParentSize(),
					enter = scaleIn(MaterialTheme.motionScheme.defaultSpatialSpec())
						+ fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
					exit = scaleOut(MaterialTheme.motionScheme.defaultSpatialSpec())
						+ fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec())
				) {
					CircularProgressIndicator(
						modifier = Modifier.matchParentSize(),
						trackColor = MaterialTheme.colorScheme.primaryContainer
					)
				}
			}

			Column(modifier = Modifier.weight(1f)) {
				song?.title?.let { title ->
					MarqueeText(
						text = title,
						style = MaterialTheme.typography.bodyMedium
					)
				}
				val notPlayingString = stringResource(R.string.info_not_playing)
				MarqueeText(
					text = buildAnnotatedString {
						if (song != null) {
							appendArtists(song.artists)
						} else {
							append(notPlayingString)
						}
					},
					style = MaterialTheme.typography.bodyMedium.copy(
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				)
			}

			Row(
				horizontalArrangement = Arrangement.spacedBy(interButtonSpacing),
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(
					onClick = player::togglePlay,
					enabled = isInteractive,
					colors = MiniPlayerDefaults.buttonColors()
				) {
					val painter = playPauseIconPainter(playerState.isPaused)
					val description = stringResource(
						if (playerState.isPaused)
							R.string.action_play
						else R.string.action_pause
					)
					Icon(
						painter = painter,
						contentDescription = description,
						modifier = Modifier.size(buttonIconSize)
					)
				}
				IconButton(
					onClick = player::next,
					enabled = isInteractive,
					colors = MiniPlayerDefaults.buttonColors()
				) {
					Icon(
						imageVector = Icons.Filled.SkipNext,
						contentDescription = stringResource(R.string.action_next_song),
						modifier = Modifier.size(buttonIconSize)
					)
				}
			}
		}
	}
}

@Composable
private fun LineProgressBar(
	modifier: Modifier = Modifier,
	contentAlignment: Alignment,
	progress: Float,
	enabled: Boolean
) {
	Box(
		modifier = modifier,
		contentAlignment = contentAlignment
	) {
		Box(
			modifier = Modifier
				.background(MaterialTheme.colorScheme.primary)
				.fillMaxWidth(if (enabled) progress else 0f)
				.height(3.dp)
		)
	}
}

@Composable
private fun OverlayProgressBar(
	modifier: Modifier = Modifier,
	progress: Float
) {
	val fillColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
	Box(
		modifier = modifier
			.background(
				brush = Brush.horizontalGradient(
					0f to fillColor,
					progress to fillColor,
					(progress + 0.001f).coerceIn(0f, 1f) to Color.Transparent,
					1f to Color.Transparent
				)
			)
	)
}
