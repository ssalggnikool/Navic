package paige.navic.ui.screens.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.song.components.SongDetailScreenInfoRow
import paige.navic.ui.screens.song.viewmodels.SongDetailViewModel
import paige.navic.ui.theme.NavicTheme
import paige.navic.ui.util.rememberColorSchemeFromCoverArt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SongDetailScreen(
	songId: String,
	initialCoverArtId: String? = null
) {
	val viewModel = koinViewModel<SongDetailViewModel>(
		key = songId,
		parameters = { parametersOf(songId) }
	)

	val songState by viewModel.songState.collectAsStateWithLifecycle()
	val song = songState.data
	val info by viewModel.info.collectAsStateWithLifecycle()

	val activeCoverArtId = if (initialCoverArtId.isNullOrEmpty()) song?.coverArtId else initialCoverArtId
	val colorScheme = rememberColorSchemeFromCoverArt(activeCoverArtId)

	NavicTheme(colorScheme) {
		Scaffold(
			topBar = { NestedTopBar({ Text(song?.title.orEmpty()) }) }
		) { contentPadding ->
			Column(
				modifier = Modifier
					.verticalScroll(rememberScrollState())
					.padding(
						top = contentPadding.calculateTopPadding() + 12.dp,
						start = 12.dp,
						end = 12.dp
					)
			) {
				Column(
					modifier = Modifier.fillMaxWidth(),
					verticalArrangement = Arrangement.spacedBy(SegmentedListItemDefaults.SegmentedGap)
				) {
					info.forEachIndexed { index, (key, value) ->
						SongDetailScreenInfoRow(
							key = stringResource(key),
							value = value,
							shapes = SegmentedListItemDefaults.segmentedShapes(
								index = index,
								count = info.count()
							)
						)
					}
				}

				Spacer(
					Modifier.padding(
						WindowInsets.systemBars
							.only(WindowInsetsSides.Bottom)
							.asPaddingValues()
					)
				)
			}
		}
	}
}
