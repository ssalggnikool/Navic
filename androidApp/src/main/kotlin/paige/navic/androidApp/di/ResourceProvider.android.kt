package paige.navic.androidApp.di

import paige.navic.androidApp.R
import paige.navic.di.ResourceProvider

class AndroidResourceProvider(
	override val appIconDefault: Int = R.mipmap.ic_launcher,
	override val appIconInverted: Int = R.mipmap.ic_launcher_inverted,
	override val icNavic: Int = R.drawable.ic_navic,
	override val animPause: Int = R.drawable.anim_pause
) : ResourceProvider
