/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.repository

import kotlinx.coroutines.CancellationException
import paige.navic.database.dao.AlbumDao
import paige.navic.database.dao.ArtistDao
import paige.navic.database.dao.PlaylistDao
import paige.navic.database.dao.SongDao
import paige.navic.database.mapper.toDomainModel
import paige.navic.database.mapper.toEntity
import paige.navic.domain.manager.session.connectivity.ReachabilityFlow
import paige.navic.domain.manager.session.SessionManager
import paige.navic.util.Logger

class SearchRepository(
	private val albumDao: AlbumDao,
	private val artistDao: ArtistDao,
	private val songDao: SongDao,
	private val playlistDao: PlaylistDao,
	private val sessionManager: SessionManager,
	reachability: ReachabilityFlow
) {
	val isReachable = reachability.isReachable

	suspend fun search(query: String): List<Any> {
		return if (isReachable.value) {
			try {
				val data = sessionManager.api.searchID3(query)

				albumDao.insertAlbumsIgnoringConflicts(data.albums.map { it.toEntity() })
				artistDao.insertArtistsIgnoringConflicts(data.artists.map { it.toEntity() })
				songDao.insertSongsIgnoringConflicts(data.songs.map { it.toEntity() })

				val albumIds = data.albums.map { it.id }
				val artistIds = data.artists.map { it.id }
				val songIds = data.songs.map { it.id }

				val albumOrder = albumIds.withIndex().associate { (i, id) -> id to i }
				val artistOrder = artistIds.withIndex().associate { (i, id) -> id to i }
				val songOrder = songIds.withIndex().associate { (i, id) -> id to i }

				val albums = albumDao.getAlbumsByIds(albumIds)
					.map { it.toDomainModel() }
					.sortedBy { albumOrder[it.id] }
				val artists = artistDao.getArtistsByIds(artistIds)
					.map { it.toDomainModel() }
					.sortedBy { artistOrder[it.id] }
				val songs = songDao.getSongsByIds(songIds)
					.map { it.toDomainModel() }
					.sortedBy { songOrder[it.id] }
				val localPlaylists = playlistDao.searchPlaylistsList(query)

				(albums + artists + songs + localPlaylists.map { it.toDomainModel() })
			} catch (e: Exception) {
				if (e is CancellationException) throw e
				Logger.e(
					"SearchRepository",
					"Online search failed despite network connection, falling back to local DB",
					e
				)
				performLocalSearch(query)
			}
		} else {
			Logger.i("SearchRepository", "Device offline, performing local search.")
			performLocalSearch(query)
		}
	}

	private suspend fun performLocalSearch(query: String): List<Any> {
		val localAlbums = albumDao.searchAlbumsList(query).map { it.toDomainModel() }
		val localArtists = artistDao.searchArtistsList(query).map { it.toDomainModel() }
		val localSongs = songDao.searchSongsList(query).map { it.toDomainModel() }
		val localPlaylists = playlistDao.searchPlaylistsList(query).map { it.toDomainModel() }

		return listOf(localAlbums, localArtists, localSongs, localPlaylists).flatten()
	}
}
