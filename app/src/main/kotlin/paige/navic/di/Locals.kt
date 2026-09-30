/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import paige.navic.domain.manager.BottomBarScrollManager

val LocalNavStack = staticCompositionLocalOf<NavBackStack<NavKey>> {
	error("No NavBackStack provided")
}

val LocalSnackBarState = staticCompositionLocalOf<SnackbarHostState> {
	error("No SnackBarHostState provided")
}

val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope> {
	error("No SharedTransitionScope provided")
}

val LocalBottomBarScrollManager = staticCompositionLocalOf<BottomBarScrollManager> {
	error("No BottomBarScrollManager provided")
}

val LocalSizeClass = staticCompositionLocalOf<WindowSizeClass> {
	error("No WindowSizeClass provided")
}

/**
 * this exists because if you use `sheetState.hide()` inside
 * of `onRemove` of an `OverlayScene`, then spam tap the sheet's
 * scrim, the app will become COMPLETELY unresponsive FOREVER
 *
 * why? I don't fucking know
 */
@OptIn(ExperimentalMaterial3Api::class)
val LocalSheetState = staticCompositionLocalOf<SheetState> {
	error("LocalSheetState used outside of a sheet")
}
