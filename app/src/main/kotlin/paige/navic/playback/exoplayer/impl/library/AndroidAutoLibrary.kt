/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback.exoplayer.impl.library

import android.app.ActivityManager
import android.content.Context
import androidx.core.net.toUri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import paige.navic.R
import paige.navic.database.dao.AlbumDao
import paige.navic.database.dao.ArtistDao
import paige.navic.database.dao.PlaylistDao
import paige.navic.database.dao.SongDao
import paige.navic.database.entity.AlbumEntity
import paige.navic.database.entity.ArtistEntity
import paige.navic.playback.exoplayer.toMediaItem
import kotlin.random.Random

@OptIn(UnstableApi::class)
class AndroidAutoLibrary(
	private val application: Context,
	private val artistDao: ArtistDao,
	private val albumDao: AlbumDao,
	private val playlistDao: PlaylistDao,
	private val songDao: SongDao
) {
	// android auto doesn't support pagination
	private val maxQueueSize: Int by lazy {
		val memoryClass = application.getSystemService(ActivityManager::class.java).memoryClass
		(memoryClass * 2).coerceIn(500, 1000)
	}

	@Volatile
	private var randomAlbumsSeed = Random.nextInt()

	fun getItem(id: String): MediaItem? = runBlocking {
		when {
			id == ROOT_ID -> browsable(ROOT_ID, "Navic")
			id == NEWEST_ALBUMS_ID -> browsable(
				NEWEST_ALBUMS_ID, application.getString(R.string.option_sort_newest)
			)

			id == RANDOM_ALBUMS_ID -> browsable(
				RANDOM_ALBUMS_ID, application.getString(R.string.option_sort_random)
			)

			id == STARRED_ID -> browsable(
				STARRED_ID, application.getString(R.string.option_sort_starred)
			)

			id == FREQUENT_ALBUMS_ID -> browsable(
				FREQUENT_ALBUMS_ID, application.getString(R.string.option_sort_frequent)
			)

			id == STARRED_SONGS_ID -> browsable(
				STARRED_SONGS_ID, application.getString(R.string.title_songs)
			)

			id == STARRED_ALBUMS_ID -> browsable(
				STARRED_ALBUMS_ID, application.getString(R.string.title_albums)
			)

			id == STARRED_ARTISTS_ID -> browsable(
				STARRED_ARTISTS_ID, application.getString(R.string.title_artists)
			)

			id == ARTISTS_ID -> browsable(
				ARTISTS_ID, application.getString(R.string.title_artists)
			)

			id == ALBUMS_ID -> browsable(
				ALBUMS_ID, application.getString(R.string.title_albums)
			)

			id == SONGS_ID -> browsable(
				SONGS_ID, application.getString(R.string.title_songs)
			)

			id == PLAYLISTS_ID -> browsable(
				PLAYLISTS_ID, application.getString(R.string.title_playlists)
			)

			id.hasPrefix(ARTIST_PREFIX) -> artistDao.getArtistById(id.stripPrefix(ARTIST_PREFIX))
				?.let { browsable(id, it.name, it.coverArtId) }

			id.hasPrefix(ALBUM_PREFIX) -> albumDao.getAlbumEntityById(id.stripPrefix(ALBUM_PREFIX))
				?.let { browsable(id, it.name, it.coverArtId, it.artistName) }

			id.hasPrefix(PLAYLIST_PREFIX) -> playlistDao.getPlaylistEntityById(
				id.stripPrefix(PLAYLIST_PREFIX)
			)?.let { browsable(id, it.name, it.coverArtId) }

			else -> songDao.getPlayableSongById(
				id.substringAfterLast(":")
			)
				?.toMediaItem()
		}
	}

	fun getChildren(parentId: String, page: Int, pageSize: Int): ImmutableList<MediaItem> =
		runBlocking {
			val limit = pageSize.coerceIn(1, MAX_PAGE_SIZE)
			val offset = (page.coerceAtLeast(0).toLong() * pageSize.coerceAtLeast(0))
				.coerceAtMost(Int.MAX_VALUE.toLong())
				.toInt()
			val items = when (parentId) {
				ROOT_ID -> if (offset > 0) emptyList() else listOf(
					browsable(
						NEWEST_ALBUMS_ID,
						title = application.getString(R.string.option_sort_newest)
					),
					browsable(
						RANDOM_ALBUMS_ID,
						title = application.getString(R.string.option_sort_random)
					),
					browsable(
						STARRED_ID,
						title = application.getString(R.string.option_sort_starred)
					),
					browsable(
						FREQUENT_ALBUMS_ID,
						title = application.getString(R.string.option_sort_frequent)
					),
					browsable(ARTISTS_ID, title = application.getString(R.string.title_artists)),
					browsable(ALBUMS_ID, title = application.getString(R.string.title_albums)),
					browsable(SONGS_ID, title = application.getString(R.string.title_songs)),
					browsable(PLAYLISTS_ID, title = application.getString(R.string.title_playlists))
				)

				NEWEST_ALBUMS_ID -> albumDao.getNewestAlbumsPage(limit, offset)
					.map { it.toBrowsable() }

				RANDOM_ALBUMS_ID -> {
					if (offset == 0) randomAlbumsSeed = Random.nextInt()
					albumDao.getRandomAlbumsPage(randomAlbumsSeed, limit, offset)
						.map { it.toBrowsable() }
				}

				FREQUENT_ALBUMS_ID -> albumDao.getFrequentAlbumsPage(limit, offset)
					.map { it.toBrowsable() }

				STARRED_ID -> if (offset > 0) emptyList() else listOf(
					browsable(STARRED_SONGS_ID, title = application.getString(R.string.title_songs)),
					browsable(STARRED_ALBUMS_ID, title = application.getString(R.string.title_albums)),
					browsable(
						STARRED_ARTISTS_ID,
						title = application.getString(R.string.title_artists)
					)
				)

				STARRED_SONGS_ID -> songDao.getStarredPlayableSongsPage(limit, offset)
					.map { it.toMediaItem() }

				STARRED_ALBUMS_ID -> albumDao.getStarredAlbumsPage(limit, offset)
					.map { it.toBrowsable() }

				STARRED_ARTISTS_ID -> artistDao.getStarredArtistsPage(limit, offset)
					.map { it.toBrowsable() }

				ARTISTS_ID -> artistDao.getArtistsPage(limit, offset)
					.map { it.toBrowsable() }

				ALBUMS_ID -> albumDao.getAlbumsPage(limit, offset)
					.map { it.toBrowsable() }

				SONGS_ID -> songDao.getPlayableSongsPage(limit, offset)
					.map { it.toMediaItem() }

				PLAYLISTS_ID -> playlistDao.getPlaylistsPage(limit, offset).map {
					browsable(
						id = it.playlistId.prefix(PLAYLIST_PREFIX),
						title = it.name,
						artworkId = it.coverArtId
					)
				}

				// whenever we are inside one of these views
				else -> when {
					parentId.hasPrefix(ARTIST_PREFIX) -> albumDao.getAlbumsByArtistId(
						parentId.stripPrefix(ARTIST_PREFIX),
						limit,
						offset
					).map { it.toBrowsable() }

					parentId.hasPrefix(ALBUM_PREFIX) -> songDao.getAlbumPlayableSongs(
						albumId = parentId.stripPrefix(ALBUM_PREFIX),
						limit,
						offset
					).mapIndexed { index, song ->
						song.toMediaItem(
							prefix = song.songId.entityIndex(
								ALBUM_PREFIX,
								parentEntityId = parentId.stripPrefix(ALBUM_PREFIX),
								index = offset + index
							)
						)
					}

					parentId.hasPrefix(PLAYLIST_PREFIX) -> playlistDao.getPlaylistPlayableSongs(
						playlistId = parentId.stripPrefix(PLAYLIST_PREFIX),
						limit,
						offset
					).mapIndexed { index, song ->
						song.toMediaItem(
							prefix = song.songId.entityIndex(
								PLAYLIST_PREFIX,
								parentEntityId = parentId.stripPrefix(PLAYLIST_PREFIX),
								index = offset + index
							)
						)
					}

					else -> emptyList()
				}
			}

			ImmutableList.copyOf(items)
		}

	fun expand(mediaId: String): List<MediaItem> = runBlocking {
		withContext(Dispatchers.IO) {
			val songs = if (!mediaId.startsWith("$PLAYABLE_PREFIX:")) {
				listOfNotNull(songDao.getPlayableSongById(mediaId))
			} else {
				val parsedEntity = mediaId.parseEntityIndex()

				when (parsedEntity.entityType) {
					ALBUM_PREFIX -> songDao.getAlbumPlayableSongs(
						albumId = parsedEntity.parentEntityId,
						limit = maxQueueSize,
						offset = parsedEntity.windowOffset()
					)

					PLAYLIST_PREFIX -> playlistDao.getPlaylistPlayableSongs(
						playlistId = parsedEntity.parentEntityId,
						limit = maxQueueSize,
						offset = parsedEntity.windowOffset()
					)

					else -> listOfNotNull(songDao.getPlayableSongById(parsedEntity.entityId))
				}
			}

			songs.map { it.toMediaItem() }
		}
	}

	suspend fun search(query: String, page: Int, pageSize: Int): ImmutableList<MediaItem> {
		val limit = pageSize.coerceIn(1, MAX_PAGE_SIZE)
		val offset = (page.coerceAtLeast(0).toLong() * pageSize.coerceAtLeast(0))
			.coerceAtMost(Int.MAX_VALUE.toLong())
			.toInt()

		val items = mutableListOf<MediaItem>()

		suspend fun fill(next: suspend (Int) -> List<MediaItem>) {
			val remaining = limit - items.size
			if (remaining > 0) items += next(remaining)
		}

		fill {
			songDao.searchPlayableSongsPage(query, it, offset).map { song -> song.toMediaItem() }
		}
		fill { albumDao.searchAlbumsPage(query, it, offset).map { album -> album.toBrowsable() } }
		fill {
			artistDao.searchArtistsPage(query, it, offset).map { artist -> artist.toBrowsable() }
		}
		fill {
			playlistDao.searchPlaylistsPage(query, it, offset).map { playlist ->
				browsable(
					id = playlist.playlistId.prefix(PLAYLIST_PREFIX),
					title = playlist.name,
					artworkId = playlist.coverArtId
				)
			}
		}

		return ImmutableList.copyOf(items)
	}

	fun searchCount(query: String): Int = runBlocking {
		songDao.searchSongCount(query) + albumDao.searchAlbumCount(query) + artistDao.searchArtistCount(
			query
		) + playlistDao.searchPlaylistCount(query)
	}

	private fun browsable(
		id: String,
		title: String?,
		artworkId: String? = null,
		subtitle: String? = null
	) = MediaItem.Builder()
		.apply {
			setMediaId(id)
			setMediaMetadata(
				MediaMetadata.Builder()
					.apply {
						setTitle(title)
						setSubtitle(subtitle)
						setArtworkUri(
							artworkId?.let {
								"content://${application.packageName}.cover-art/$it".toUri()
							}
						)
						setIsBrowsable(true)
						setIsPlayable(false)
					}.build()
			)
		}.build()

	private fun AlbumEntity.toBrowsable() = browsable(
		id = albumId.prefix(ALBUM_PREFIX),
		title = name,
		artworkId = coverArtId,
		subtitle = artistName
	)

	private fun ArtistEntity.toBrowsable() = browsable(
		id = artistId.prefix(ARTIST_PREFIX),
		title = name,
		artworkId = coverArtId
	)

	companion object {
		private const val MAX_PAGE_SIZE = 500
		const val ROOT_ID = "root"
		const val ARTISTS_ID = "artists"
		const val ALBUMS_ID = "albums"
		const val SONGS_ID = "songs"
		const val PLAYLISTS_ID = "playlists"
		private const val NEWEST_ALBUMS_ID = "home_newest"
		private const val RANDOM_ALBUMS_ID = "home_random"
		private const val FREQUENT_ALBUMS_ID = "home_frequent"
		private const val STARRED_ID = "home_starred"
		private const val STARRED_SONGS_ID = "home_starred_songs"
		private const val STARRED_ALBUMS_ID = "home_starred_albums"
		private const val STARRED_ARTISTS_ID = "home_starred_artists"
		private const val ARTIST_PREFIX = "artist"
		private const val ALBUM_PREFIX = "album"
		private const val PLAYLIST_PREFIX = "playlist"
		private const val PLAYABLE_PREFIX = "playable"
	}

	private fun String.prefix(prefix: String): String = "$prefix:$this"
	private fun String.hasPrefix(prefix: String): Boolean = this.startsWith("$prefix:")
	private fun String.stripPrefix(prefix: String): String = this.removePrefix("$prefix:")

	private fun String.entityIndex(entity: String, parentEntityId: String, index: Int) =
		"$PLAYABLE_PREFIX:$entity:$parentEntityId:$index:$this"

	private fun String.parseEntityIndex() = this.stripPrefix(PLAYABLE_PREFIX).split(":").let {
		EntityIndex(
			entityType = it[0],
			parentEntityId = it[1],
			index = it[2],
			entityId = it[3]
		)
	}

	private data class EntityIndex(
		val entityType: String,
		val parentEntityId: String,
		val index: String,
		val entityId: String
	)

	private fun EntityIndex.windowOffset(): Int =
		(index.toIntOrNull() ?: 0).minus(maxQueueSize - 1).coerceAtLeast(0)
}
