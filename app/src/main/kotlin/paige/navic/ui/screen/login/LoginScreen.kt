/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.login

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.domain.manager.session.LoginManager
import paige.navic.ui.screen.login.component.HorizontalPagerAnimationSpec
import paige.navic.ui.screen.login.component.NavigableHorizontalPager
import paige.navic.ui.screen.login.page.LoginPage
import paige.navic.ui.screen.login.page.ServerPage
import paige.navic.ui.screen.login.viewmodel.LoginViewModel

private enum class Page {
	Server, Login
}

@Composable
fun LoginScreen() {
	val viewModel = koinViewModel<LoginViewModel>()
	val loginManager = koinInject<LoginManager>()
	val authMethod by viewModel.authMethod.collectAsStateWithLifecycle()

	val pagerState = rememberPagerState(
		initialPage = 0,
		pageCount = { Page.entries.count() }
	)
	val pagerScope = rememberCoroutineScope()

	val focusManager = LocalFocusManager.current

	NavigableHorizontalPager(
		modifier = Modifier.fillMaxSize(),
		state = pagerState,
		userScrollEnabled = authMethod != null
	) { index, pageModifier ->
		val page = Page.entries[index]
		when (page) {
			Page.Server -> ServerPage(
				modifier = pageModifier,
				onAuthMethodChanged = viewModel::selectAuthMethod,
				onNavigateForward = {
					pagerScope.launch {
						pagerState.animateScrollToPage(
							1,
							animationSpec = HorizontalPagerAnimationSpec
						)
					}
				},
				onAccountSelected = { account ->
					loginManager.fillFor(account)
					viewModel.selectAuthMethod(account.authMethod)
					focusManager.clearFocus(true)
					pagerScope.launch {
						pagerState.animateScrollToPage(
							1,
							animationSpec = HorizontalPagerAnimationSpec
						)
					}
				}
			)

			Page.Login -> LoginPage(
				modifier = pageModifier,
				authMethod = authMethod!!,
				onNavigateBack = {
					focusManager.clearFocus(true)
					pagerScope.launch {
						pagerState.animateScrollToPage(
							0,
							animationSpec = HorizontalPagerAnimationSpec
						)
					}
				}
			)
		}
	}
}
