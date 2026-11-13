/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import org.koin.compose.koinInject
import paige.navic.BuildConfig
import paige.navic.R
import paige.navic.di.LocalSizeClass
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.ui.component.dialog.LinkConfirmationDialog
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.component.layout.NestedTopBarDefaults
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Code
import paige.navic.ui.icons.outlined.Globe
import paige.navic.ui.screen.settings.component.SettingsGroup
import paige.navic.ui.screen.settings.component.SettingsGroupDefaults
import paige.navic.ui.screen.settings.component.SettingsLinkItem
import paige.navic.ui.screen.settings.component.SettingsToggleItem
import paige.navic.ui.theme.defaultFont

private const val CODEBERG_BASE_URL = "https://codeberg.org/paige/Navic/"

@Composable
fun SettingsAboutScreen() {
	val preferenceManager = koinInject<PreferenceManager>()

	val sizeClass = LocalSizeClass.current
	val hideBack = sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium

	var linkToOpen by rememberSaveable { mutableStateOf<String?>(null) }

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(R.string.title_about)) },
				navigationAction = {
					if (!hideBack) {
						NestedTopBarDefaults.NavigationAction()
					}
				}
			)
		}
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp),
			verticalArrangement = Arrangement.spacedBy(SettingsGroupDefaults.GapBetweenGroups)
		) {
			AboutHeader()

			if (!BuildConfig.FDROID) {
				SettingsGroup {
					SettingsToggleItem(
						content = { Text(stringResource(R.string.option_check_for_updates)) },
						supportingContent = { Text(stringResource(R.string.subtitle_check_for_updates)) },
						checked = preferenceManager.checkForUpdates,
						onCheckedChange = { preferenceManager.checkForUpdates = it }
					)
				}
			}

			SettingsGroup(title = { Text(stringResource(R.string.title_links)) }) {
				SettingsLinkItem(
					link = "${CODEBERG_BASE_URL}wiki",
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 3),
					content = { Text(stringResource(R.string.title_wiki)) }
				)
				SettingsLinkItem(
					link = "https://matrix.to/#/#navic:suicide.diy",
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 3),
					content = { Text(stringResource(R.string.title_matrix_space)) }
				)
				SettingsLinkItem(
					link = "https://discord.gg/TBcnNX66PH",
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 2, count = 3),
					content = { Text(stringResource(R.string.title_discord_server)) }
				)
			}

			// TODO: make this less ass
			if (BuildConfig.GIT_COMMIT_HASH != "unknown") {
				val commitLink = "${CODEBERG_BASE_URL}commit/${BuildConfig.GIT_COMMIT_HASH}"
				val branchName = BuildConfig.GIT_BRANCH_NAME
				val commitAuthor = BuildConfig.GIT_COMMIT_AUTHOR

				SettingsGroup(title = { Text(stringResource(R.string.title_build_info)) }) {
					SettingsLinkItem(
						link = commitLink,
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 4),
						content = { Text(stringResource(R.string.title_commit_hash)) },
						supportingContent = { Text(BuildConfig.GIT_COMMIT_HASH.slice(0..6)) }
					)
					SettingsLinkItem(
						link = commitLink,
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 4),
						content = { Text(stringResource(R.string.title_commit_message)) },
						supportingContent = {
							Text(
								text = BuildConfig.GIT_COMMIT_MESSAGE.split("\n").firstOrNull()
									?: "unknown",
								maxLines = 1,
								overflow = TextOverflow.Ellipsis
							)
						}
					)
					SettingsLinkItem(
						link = "${CODEBERG_BASE_URL}src/branch/${branchName}",
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 2, count = 4),
						content = { Text(stringResource(R.string.title_branch_name)) },
						supportingContent = { Text(BuildConfig.GIT_BRANCH_NAME) }
					)
					SettingsLinkItem(
						link = "${CODEBERG_BASE_URL}commits/${branchName}/?author=${commitAuthor}",
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 3, count = 4),
						content = { Text(stringResource(R.string.title_commit_author)) },
						supportingContent = { Text(commitAuthor) }
					)
				}
			}
		}
	}

	LinkConfirmationDialog(
		linkToOpen = linkToOpen,
		onDismissRequest = { linkToOpen = null }
	)
}

@Composable
private fun AboutHeader() {
	val context = LocalContext.current
	val packageInfo = remember {
		context.packageManager.getPackageInfo(context.packageName, 0)
	}

	Column(
		modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		AppIcon(Modifier.size(80.dp).clip(CircleShape))

		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				text = stringResource(R.string.app_name),
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Medium
			)
			Text(
				text = packageInfo.versionName.toString(),
				style = MaterialTheme.typography.labelLarge,
				color = MaterialTheme.colorScheme.primary,
				fontFamily = defaultFont(round = 100f),
				fontWeight = FontWeight.Medium
			)
		}

		Row(
			horizontalArrangement = Arrangement.SpaceEvenly,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			IconLinkButton(
				icon = Icons.Outlined.Code,
				contentDescription = stringResource(R.string.title_codeberg),
				link = CODEBERG_BASE_URL
			)
			IconLinkButton(
				icon = Icons.Outlined.Globe,
				contentDescription = stringResource(R.string.title_website),
				link = "https://navic.app/"
			)
		}
	}
}

@Composable
private fun AppIcon(
	modifier: Modifier = Modifier
) {
	val context = LocalContext.current
	val bitmap = remember {
		context.applicationInfo.loadIcon(context.packageManager)
			.toBitmap(500, 500, Bitmap.Config.ARGB_8888)
			.asImageBitmap()
	}

	Image(
		bitmap = bitmap,
		contentDescription = null,
		modifier = modifier
	)
}

@Composable
private fun IconLinkButton(
	link: String,
	icon: ImageVector,
	contentDescription: String
) {
	var linkToOpen by rememberSaveable { mutableStateOf<String?>(null) }

	FilledTonalIconButton(
		modifier = Modifier
			.size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide)),
		shapes = IconButtonDefaults.shapes(),
		onClick = { linkToOpen = link }
	) {
		Icon(
			imageVector = icon,
			contentDescription = contentDescription
		)
	}

	LinkConfirmationDialog(
		linkToOpen = linkToOpen,
		onDismissRequest = { linkToOpen = null }
	)
}
