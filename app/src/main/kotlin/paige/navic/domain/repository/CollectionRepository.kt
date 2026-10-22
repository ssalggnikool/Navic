/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import paige.navic.database.dao.AlbumDao
import paige.navic.database.dao.PlaylistDao
import paige.navic.database.dao.SongDao
import paige.navic.database.mapper.toDomainModel
import paige.navic.database.mapper.toEntity
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongCollection
import dev.zt64.subsonic.api.model.AlbumInfo as ApiAlbumInfo

class CollectionRepository(
	private val albumDao: AlbumDao,
	private val playlistDao: PlaylistDao,
	private val songDao: SongDao,
	private val dbRepository: DbRepository,
	private val sessionManager: SessionManager
) {
	suspend fun getLocalData(collectionId: String): DomainSongCollection {
		return albumDao.getAlbumById(collectionId)?.toDomainModel()
			?: playlistDao.getPlaylistById(collectionId)?.toDomainModel()
			?: error("Collection ID $collectionId is neither a known album or playlist")
	}

	private suspend fun refreshLocalData(collectionId: String): DomainSongCollection {
		when (val collection = getLocalData(collectionId)) {
			is DomainAlbum -> {
				val album = sessionManager.api.getAlbum(collection.id)
				songDao.updateSongsByAlbumId(album.id, album.songs.map { it.toEntity() })
				albumDao.insertAlbum(album.toEntity())
				albumDao.getAlbumById(collectionId)
			}

			is DomainPlaylist -> {
				val playlist = sessionManager.api.getPlaylist(collection.id)
				playlistDao.insertPlaylist(playlist.toEntity())
				dbRepository.syncPlaylistSongs(collection.id)
				playlistDao.getPlaylistById(collectionId)
			}
		}
		return getLocalData(collectionId)
	}

	suspend fun getCollection(
		fullRefresh: Boolean,
		collectionId: String
	): DomainSongCollection {
		val localData = getLocalData(collectionId)
		return if (fullRefresh) {
			refreshLocalData(collectionId)
		} else {
			localData
		}
	}

	fun getOtherAlbums(artistId: String, albumId: String) = albumDao
		.getAlbumsByArtistExcluding(artistId, albumId)
		.map { it.map { album -> album.toDomainModel() } }

	suspend fun getSongById(songId: String) = songDao
		.getSongById(songId)
		?.toDomainModel()

	suspend fun getAlbumInfo(albumId: String): ApiAlbumInfo {
		return sessionManager.api.getAlbumInfo(albumId)
	}

	suspend fun removeSongFromPlaylist(playlistId: String, position: Int) {
		sessionManager.api.updatePlaylist(
			id = playlistId,
			songIndicesToRemove = listOf(position)
		)
	}

	fun getPagedPlaylistSongs(playlistId: String): Flow<PagingData<DomainSong>> {
		return Pager(
			config = PagingConfig(
				pageSize = 20,
				prefetchDistance = 10,
				maxSize = 100
			),
			pagingSourceFactory = {
				songDao.getPagedSongsByPlaylistId(playlistId)
			}
		).flow.map { pagingData -> pagingData.map { it.toDomainModel() } }
	}
}
