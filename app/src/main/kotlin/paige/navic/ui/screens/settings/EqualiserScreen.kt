package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.domain.manager.EqualizerManager
import paige.navic.domain.models.settings.EqualizerMode
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Refresh
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.components.common.VerticalSlider
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.screens.settings.components.SettingsChoiceItem
import paige.navic.ui.screens.settings.components.SettingsGroup
import paige.navic.ui.screens.settings.components.SettingsGroupDefaults

@Composable
fun SettingsEqualizerScreen() {
	val equalizerManager = koinInject<EqualizerManager>()
	val config by equalizerManager.config.collectAsStateWithLifecycle()
	val scope = rememberCoroutineScope()

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(R.string.option_equalizer)) },
				actions = {
					TopBarButton(
						onClick = {
							scope.launch {
								equalizerManager.setConfig(
									config.copy(bandLevels = emptyMap())
								)
							}
						},
						enabled = config.bandLevels.isNotEmpty() && config.mode == EqualizerMode.BuiltIn
					) {
						Icon(
							imageVector = Icons.Outlined.Refresh,
							contentDescription = stringResource(R.string.action_reset)
						)
					}
				}
			)
		}
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				modifier = Modifier
					.padding(innerPadding)
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 16.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(SettingsGroupDefaults.GapBetweenGroups)
			) {
				SettingsGroup(modifier = Modifier.widthIn(max = 600.dp)) {
					SettingsChoiceItem(
						choices = EqualizerMode.entries.toImmutableList(),
						selectedChoice = config.mode,
						onChoiceSelected = { mode ->
							scope.launch {
								equalizerManager.setConfig(config.copy(mode = mode))
							}
						},
						content = { Text(stringResource(R.string.option_equalizer_mode)) },
						label = { stringResource(it.displayName) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 1)
					)
				}

				if (config.mode != EqualizerMode.BuiltIn) {
					Text(stringResource(R.string.info_equalizer_mode_not_builtin))
					return@Column
				}

				if (config.bandCount == 0) {
					Text(stringResource(R.string.info_equalizer_unsupported))
					return@Column
				}

				Row(Modifier.widthIn(max = 600.dp)) {
					repeat(config.bandCount) { band ->
						EqualizerBand(
							level = config.bandLevels[band] ?: 0f,
							onLevelChange = { level ->
								val newLevels = config.bandLevels.toMutableMap().apply {
									set(band, level)
								}
								val newConfig = config.copy(bandLevels = newLevels)
								scope.launch {
									equalizerManager.setConfig(newConfig)
								}
							},
							levelRange = config.bandLowerRange..config.bandUpperRange
						)
					}
				}
			}
		}
	}
}

@Composable
private fun RowScope.EqualizerBand(
	level: Float,
	onLevelChange: (level: Float) -> Unit,
	levelRange: ClosedFloatingPointRange<Float>
) {
	val textStyle = MaterialTheme.typography.bodySmall.copy(
		fontFamily = FontFamily.Monospace,
		fontWeight = FontWeight.Medium
	)
	Column(
		modifier = Modifier.height(400.dp).weight(1f),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text("${levelRange.endInclusive.toInt()}mB", style = textStyle, maxLines = 1)
		VerticalSlider(
			modifier = Modifier.weight(1f),
			value = level,
			onValueChange = onLevelChange,
			valueRange = levelRange
		)
		Text("${levelRange.start.toInt()}mB", style = textStyle, maxLines = 1)
	}
}
