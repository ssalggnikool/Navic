package paige.navic.playback.exoplayer.impl.listeners

import androidx.media3.common.Player
import org.koin.core.component.KoinComponent

interface ExoDiscordIntegration : Player.Listener, KoinComponent {
	fun release() {}

	companion object {
		const val DISCORD_APPLICATION_ID = "1554936646405984446"
	}
}
