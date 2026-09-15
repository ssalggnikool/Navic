package paige.navic.ui.screens.stats.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.ArtistDao
import paige.navic.data.database.dao.SongDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainSong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class ArtistStats(
	val artist: DomainArtist,
	val playCount: Int,
	val listeningTime: Duration
)

data class AlbumStats(
	val album: DomainAlbum,
	val playCount: Int,
	val listeningTime: Duration
)

data class SongStats(
	val song: DomainSong,
	val playCount: Int,
	val listeningTime: Duration
)

data class StatisticsState(
	val totalPlays: Int = 0,
	val totalDuration: Duration = Duration.ZERO,
	val topSongs: List<SongStats> = emptyList(),
	val topAlbums: List<AlbumStats> = emptyList(),
	val topArtists: List<ArtistStats> = emptyList(),
	val isLoading: Boolean = true
)

class StatisticsViewModel(
	private val songDao: SongDao,
	private val artistDao: ArtistDao,
	private val albumDao: AlbumDao
) : ViewModel() {

	@OptIn(ExperimentalCoroutinesApi::class)
	val state: StateFlow<StatisticsState> = combine(
		songDao.getTotalPlayCount().map { it ?: 0 },
		songDao.getTotalListeningTime().map { (it ?: 0L).milliseconds },
		songDao.getTopSongs(10).map { songs ->
			songs.map { song ->
				SongStats(
					song = song.toDomainModel(),
					playCount = song.playCount,
					listeningTime = song.duration * song.playCount
				)
			}
		},
		songDao.getTopAlbumIds(10).flatMapLatest { ids ->
			if (ids.isEmpty()) flowOf(emptyList())
			else flow {
				val albums = mutableListOf<AlbumStats>()
				for (id in ids) {
					val entity = albumDao.getAlbumById(id)
					val playCount = songDao.getSongsByAlbumId(id).sumOf { it.playCount }
					val listeningTime = (songDao.getAlbumListeningTime(id).first() ?: 0L).milliseconds
					entity?.toDomainModel()?.let { albums.add(AlbumStats(it, playCount, listeningTime)) }
				}
				emit(albums)
			}
		},
		songDao.getTopArtistIds(10).flatMapLatest { ids ->
			if (ids.isEmpty()) flowOf(emptyList())
			else flow {
				val artists = mutableListOf<ArtistStats>()
				for (id in ids) {
					val entity = artistDao.getArtistById(id)
					val playCount = songDao.getArtistPlayCount(id).first() ?: 0
					val listeningTime = (songDao.getArtistListeningTime(id).first() ?: 0L).milliseconds
					entity?.toDomainModel()?.let { artists.add(ArtistStats(it, playCount, listeningTime)) }
				}
				emit(artists)
			}
		}
	) { total, duration, songs, albums, artists ->
		StatisticsState(
			totalPlays = total,
			totalDuration = duration,
			topSongs = songs,
			topAlbums = albums,
			topArtists = artists,
			isLoading = false
		)
	}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsState())
}
