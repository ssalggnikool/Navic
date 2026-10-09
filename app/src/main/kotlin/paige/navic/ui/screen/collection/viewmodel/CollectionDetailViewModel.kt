/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.collection.viewmodel

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import paige.navic.R
import paige.navic.data.database.entity.DownloadStatus
import paige.navic.data.database.mapper.toDomainModel
import paige.navic.domain.manager.ConnectivityManager
import paige.navic.domain.manager.DownloadManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.SnackBarManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumInfo
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongCollection
import paige.navic.domain.repository.AlbumRepository
import paige.navic.domain.repository.CollectionRepository
import paige.navic.domain.repository.SongRepository
import paige.navic.ui.viewmodel.AsyncViewModel
import paige.navic.util.Logger

data class CollectionViewState(
	val collection: DomainSongCollection? = null,
	val isAlbum: Boolean = false,
	val starred: Boolean = false,
	val rating: Int = 0,
	val albumInfo: DomainAlbumInfo? = null,
	val selectedSong: DomainSong? = null,
	val selectedAlbum: DomainAlbum? = null
)

class CollectionDetailViewModel(
	private val collectionId: String,
	private val repository: CollectionRepository,
	private val songRepository: SongRepository,
	private val albumRepository: AlbumRepository,
	private val downloadManager: DownloadManager,
	private val sessionManager: SessionManager,
	private val snackBarManager: SnackBarManager,
	connectivityManager: ConnectivityManager
) : AsyncViewModel<CollectionViewState?>() {

	init {
		execute {
			CollectionViewState(
				collection = repository.getLocalData(collectionId)
			)
		}
	}

	val listState = LazyListState()

	val isOnline = connectivityManager.isOnline

	val allDownloads = downloadManager.allDownloads
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.Lazily,
			initialValue = emptyList()
		)

	val otherAlbums = (uiState.value.data?.collection as? DomainAlbum)?.let { album ->
		repository.getOtherAlbums(album.artistId, album.id)
	}?.stateIn(
		scope = viewModelScope,
		started = SharingStarted.Lazily,
		initialValue = emptyList()
	) ?: MutableStateFlow(emptyList())

	init {
		launch {
			sessionManager.isLoggedIn.collect { if (it) refreshCollection(false) }
		}
	}

	fun refreshCollection(fullRefresh: Boolean) = launch {
		repository.getCollectionFlow(fullRefresh, collectionId).collect { collection ->
			if (collection is DomainAlbum) {
				execute {
					val starred = albumRepository.isAlbumStarred(collection)
					val rating = albumRepository.getAlbumRating(collection)
					val albumInfo = repository.getAlbumInfo(collectionId)

					uiState.value.data?.copy(
						starred = starred,
						isAlbum = true,
						albumInfo = albumInfo.toDomainModel(),
						rating = rating
					)
				}
			}
		}
	}

	fun selectSong(song: DomainSong) = execute {
		uiState.value.data?.copy(
			selectedSong = song
		)
	}

	fun selectAlbum(album: DomainAlbum) = execute {
		uiState.value.data?.copy(
			selectedAlbum = album
		)
	}


	fun clearSelection() = execute {
		uiState.value.data?.copy(
			selectedAlbum = null
		)
	}

	fun clearError() {
		// ?
	}

	fun removeFromPlaylist() {
		val song = uiState.value.data?.selectedSong ?: return
		val songs = uiState.value.data?.collection?.songs ?: return
		launch {
			try {
				sessionManager.api.updatePlaylist(
					id = collectionId,
					songIndicesToRemove = listOf(songs.indexOf(song))
				)
				snackBarManager.notify(R.string.notice_removed_from_playlist)
				refreshCollection(true)
			} catch (e: Exception) {
				Logger.e("CollectionDetailViewModel", "Failed to remove song from playlist", e)
			}
		}
		clearSelection()
	}

	fun starSelectedSong() = launch {
		val selection = uiState.value.data?.selectedSong ?: return@launch
		songRepository.starSong(selection)
		refreshCollection(false)
	}

	fun unstarSelectedSong() = launch {
		val selection = uiState.value.data?.selectedSong ?: return@launch
		songRepository.unstarSong(selection)
		refreshCollection(false)
	}

	fun rateSelectedSong(rating: Int) = launch {
		val selection = uiState.value.data?.selectedSong ?: return@launch
		songRepository.rateSong(selection, rating)
	}

	fun rateAlbum(newRating: Int) = launch {
		(uiState.value.data?.collection as? DomainAlbum)?.let { album ->
			albumRepository.rateAlbum(album, newRating)
		}
	}

	fun starAlbum(starred: Boolean) {
		viewModelScope.launch {
			runCatching {
				val collection = uiState.value.data?.collection ?: return@launch
				if (collection !is DomainAlbum) return@launch
				if (starred) {
					albumRepository.starAlbum(collection)
				} else {
					albumRepository.unstarAlbum(collection)
				}
				refreshCollection(false)
			}
		}
	}

	fun rateSelectedAlbum(rating: Int) {
		launch {
			uiState.value.data?.collection?.let { album ->
				albumRepository.rateAlbum(album as DomainAlbum, rating)
			}
		}
	}

	fun starSelectedAlbum(starred: Boolean) {
		launch {
			val album = uiState.value.data?.collection as DomainAlbum

			if (starred) {
				albumRepository.starAlbum(album)
			} else {
				albumRepository.unstarAlbum(album)
			}
		}
	}

	fun downloadSong(song: DomainSong) {
		downloadManager.downloadSong(song)
		snackBarManager.notify(R.string.notice_download_started)
	}

	fun cancelDownload(songId: String) {
		downloadManager.cancelDownload(songId)
	}

	fun deleteDownload(songId: String) {
		downloadManager.deleteDownload(songId)
		snackBarManager.notify(R.string.notice_deleted_download)
	}

	fun downloadAll() {
		val collection = uiState.value.data?.collection ?: return
		viewModelScope.launch {
			downloadManager.downloadCollection(collection)
			snackBarManager.notify(R.string.notice_download_started)
		}
	}

	fun cancelDownloadAll() {
		uiState.value.data?.collection?.songs?.forEach {
			downloadManager.cancelDownload(it.id)
		}
	}

	fun collectionDownloadStatus(): Flow<DownloadStatus> {
		val songs = uiState.value.data?.collection?.songs.orEmpty()
		return downloadManager.getCollectionDownloadStatus(songs.map { it.id })
	}
}
