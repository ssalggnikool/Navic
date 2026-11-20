package paige.navic.ui.screen.login.page

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.designsystem.theme.defaultFont
import paige.navic.domain.manager.session.Account
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.server.AuthMethod
import paige.navic.icons.Icons
import paige.navic.icons.brand.Jellyfin
import paige.navic.icons.brand.Subsonic
import paige.navic.icons.outlined.Album
import paige.navic.icons.outlined.ChevronForward
import paige.navic.ui.component.common.SegmentedListItem
import paige.navic.ui.component.common.SegmentedListItemDefaults

@Composable
fun ServerPage(
	modifier: Modifier = Modifier,
	onAuthMethodChanged: (AuthMethod) -> Unit,
	onNavigateForward: () -> Unit,
	onAccountSelected: (Account) -> Unit
) {
	Scaffold(
		modifier = modifier
	) { innerPadding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
				.consumeWindowInsets(innerPadding)
				.verticalScroll(rememberScrollState()),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(SegmentedListItemDefaults.SegmentedGap)
		) {
			Header(
				modifier = Modifier.weight(1.5f)
			)
			SavedServers(onAccountSelected = onAccountSelected)
			ServerChoices(
				modifier = Modifier.weight(1f),
				onAuthMethodChanged = {
					onAuthMethodChanged(it)
					onNavigateForward()
				}
			)
		}
	}
}

@Composable
private fun Header(
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
	) {
		Spacer(Modifier.weight(1f))
		Logo()
		Spacer(Modifier.weight(0.1f))
		Text(
			text = stringResource(R.string.title_welcome),
			style = MaterialTheme.typography.headlineMedium,
			fontFamily = defaultFont(round = 100f)
		)
		Text(
			text = stringResource(R.string.subtitle_welcome)
		)
		Spacer(Modifier.weight(0.5f))
	}
}

@Composable
private fun SavedServers(
	onAccountSelected: (Account) -> Unit
) {
	val sessionManager = koinInject<SessionManager>()
	val accounts by sessionManager.accounts.collectAsStateWithLifecycle()

	if (accounts.isEmpty()) return

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp),
		verticalArrangement = Arrangement.spacedBy(SegmentedListItemDefaults.SegmentedGap)
	) {
		Text(
			text = stringResource(R.string.title_saved_servers),
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.padding(start = 4.dp)
		)
		accounts.forEachIndexed { index, account ->
			val icon = when (account.authMethod) {
				AuthMethod.Jellyfin -> Icons.Brand.Jellyfin
				else -> Icons.Brand.Subsonic
			}

			SegmentedListItem(
				onClick = { onAccountSelected(account) },
				shapes = SegmentedListItemDefaults.segmentedShapes(
					index = index,
					count = accounts.size
				),
				leadingContent = { Icon(icon, null) },
				trailingContent = { Icon(Icons.Outlined.ChevronForward, null) },
				supportingContent = { Text(account.baseUrl) },
				content = { Text(account.username.ifBlank { account.baseUrl }) }
			)
		}
	}
}

@Composable
private fun Logo() {
	var atEnd by rememberSaveable { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		atEnd = true
	}

	var pressedDown by remember { mutableStateOf(false) }
	val scale by animateFloatAsState(if (pressedDown) 0.8f else 1f)
	val spinny = remember { Animatable(0f) }
	var spinTrigger by remember { mutableIntStateOf(0) }
	val animationSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
	val haptics = LocalHapticFeedback.current

	LaunchedEffect(spinTrigger) {
		if (spinTrigger != 0) {
			spinny.animateTo(360f, animationSpec)
			spinny.snapTo(0f)
		}
	}

	Icon(
		painter = rememberAnimatedVectorPainter(
			AnimatedImageVector.animatedVectorResource(R.drawable.splash),
			atEnd = atEnd
		),
		contentDescription = null,
		modifier = Modifier
			.size(160.dp)
			.rotate(spinny.value)
			.pointerInput(Unit) {
				detectTapGestures(
					onPress = {
						try {
							pressedDown = true
							haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
							awaitRelease()
							spinTrigger++
							haptics.performHapticFeedback(HapticFeedbackType.GestureEnd)
						} finally {
							pressedDown = false
						}
					},
				)
			}
			.scale(scale)
	)
}

@Composable
private fun ServerChoices(
	modifier: Modifier = Modifier,
	onAuthMethodChanged: (AuthMethod) -> Unit
) {
	val context = LocalContext.current
	val packageInfo = remember {
		context.packageManager.getPackageInfo(context.packageName, 0)
	}

	// "navidrome" is just the same as subsonic, it's just there for people who don't know what subsonic is
	val methods = listOf(AuthMethod.Token, "navidrome", AuthMethod.Jellyfin)

	Column(
		modifier = modifier.padding(horizontal = 16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(SegmentedListItemDefaults.SegmentedGap)
	) {
		Spacer(Modifier.weight(1f))
		methods.forEachIndexed { index, method ->
			val icon = when (method) {
				AuthMethod.Jellyfin -> Icons.Brand.Jellyfin
				"navidrome" -> Icons.Outlined.Album
				else -> Icons.Brand.Subsonic
			}
			val label = when (method) {
				AuthMethod.Jellyfin -> stringResource(R.string.option_account_jellyfin)
				"navidrome" -> "Navidrome"
				else -> stringResource(R.string.option_account_subsonic)
			}

			SegmentedListItem(
				leadingContent = { Icon(icon, null) },
				trailingContent = { Icon(Icons.Outlined.ChevronForward, null) },
				content = { Text(label) },
				onClick = {
					if (method is AuthMethod) {
						onAuthMethodChanged(method)
					} else {
						onAuthMethodChanged(AuthMethod.Token)
					}
				},
				shapes = SegmentedListItemDefaults.segmentedShapes(
					index = index,
					count = methods.count()
				)
			)
		}
		Spacer(Modifier.weight(1.5f))
		Text(
			text = packageInfo.versionName!!,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			style = MaterialTheme.typography.labelSmall
		)
		Spacer(Modifier.height(12.dp))
	}
}
