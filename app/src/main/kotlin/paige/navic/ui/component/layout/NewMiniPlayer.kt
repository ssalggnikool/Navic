package paige.navic.ui.component.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.model.settings.MiniPlayerStyle
import paige.navic.playback.MediaPlayer
import paige.navic.ui.component.common.CoverArt
import paige.navic.ui.component.common.MarqueeText
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.filled.Note
import paige.navic.ui.icons.filled.SkipNext
import paige.navic.ui.util.appendArtists
import paige.navic.ui.util.playPauseIconPainter

object MiniPlayerDefaults {
	val InterItemSpacing = 10.dp

	val UnifiedInterButtonSpacing = 8.dp
	val DetachedInterButtonSpacing = 12.dp

	val UnifiedContentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
	val DetachedContentPadding = PaddingValues(10.dp)

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
fun NewMiniPlayer(
	modifier: Modifier = Modifier,
	colors: MiniPlayerColors = MiniPlayerDefaults.colors(),
	shapes: MiniPlayerShapes = MiniPlayerDefaults.shapes()
) {
	val player = koinInject<MediaPlayer>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val song = playerState.currentSong
	val enabled = song != null

	val preferenceManager = koinInject<PreferenceManager>()
	val isDetached = preferenceManager.miniPlayerStyle == MiniPlayerStyle.Detached

	val contentPadding = if (isDetached)
		MiniPlayerDefaults.DetachedContentPadding
	else MiniPlayerDefaults.UnifiedContentPadding
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
			.background(colors.containerColor)
			.padding(contentPadding)
			.clip(shape)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
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
						Modifier.matchParentSize(),
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
						if (enabled) {
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
					enabled = enabled,
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
