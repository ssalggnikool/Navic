package paige.navic.ui.screens.song.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import paige.navic.R
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.DomainSong
import paige.navic.domain.repositories.CollectionRepository
import paige.navic.ui.core.UiState
import paige.navic.util.effectiveGain
import paige.navic.util.toFileSize
import paige.navic.util.toHoursMinutesSeconds

class SongDetailViewModel(
	songId: String,
	private val repository: CollectionRepository,
	private val preferenceManager: PreferenceManager
) : ViewModel() {
	val songState: StateFlow<UiState<DomainSong>>
		field = MutableStateFlow<UiState<DomainSong>>(UiState.Loading())

	val info: StateFlow<List<Pair<Int, String?>>>
		field = MutableStateFlow(emptyList())

	init {
		viewModelScope.launch {
			val song = repository.getSongById(songId)
			if (song != null) {
				songState.value = UiState.Success(song)
				info.value = getInfo(song)
			} else {
				songState.value = UiState.Error(Exception("Unknown song"))
			}
		}
	}

	fun getInfo(song: DomainSong): PersistentList<Pair<Int, String?>> =
		persistentListOf(
			R.string.info_track_name to song.title,
			R.string.info_track_artist to song.artistName,
			R.string.info_track_album to song.albumTitle,

			R.string.info_track_number to song.trackNumber.toString(),
			R.string.info_track_disc_number to song.discNumber.toString(),
			R.string.info_track_year to song.year.toString(),
			R.string.info_track_genre to song.genre,

			R.string.info_track_duration to song.duration.toHoursMinutesSeconds(),
			R.string.info_track_format to song.mimeType,
			R.string.info_track_bitrate to song.bitRate?.let { "$it kbps" },
			R.string.info_track_bit_depth to song.bitDepth?.toString(),
			R.string.info_track_sampling_rate to song.sampleRate?.let { "$it Hz" },
			R.string.info_track_channel_count to song.audioChannelCount?.toString(),

			R.string.info_track_file_size to song.fileSize.toFileSize(),
			R.string.info_track_path to song.filePath,

			R.string.info_track_replay_gain to song.replayGain?.trackGain?.let { "$it dB" },
			R.string.info_album_replay_gain to song.replayGain?.albumGain?.let { "$it dB" },
			R.string.info_track_replay_gain_effective to song.replayGain?.effectiveGain(
				preferenceManager.replayGainMode
			)?.let { "$it dB" },
			R.string.title_statistics to song.playCount.toString()
		)
}
