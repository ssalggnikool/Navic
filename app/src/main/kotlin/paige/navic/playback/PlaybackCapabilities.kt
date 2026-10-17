/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige, Nightdavisao
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.ffmpeg.FfmpegLibrary
import dev.zt64.subsonic.api.model.ClientInfo
import paige.navic.util.Logger

@OptIn(UnstableApi::class)
object PlaybackCapabilities {
	private const val TAG = "PlaybackCapabilities"
	private const val MIMETYPE_PREFIX = "audio/"

	private val KNOWN_SUPPORTED_FFMPEG_DECODERS = listOf(
		"flac", "alac"
	)
	private val CORRECTED_CODEC_NAME = mapOf(
		"mp4a-latm" to "aac",
		"mpeg" to "mp3",
		"raw" to "wav"
	)
	private val CONTAINER_MAPPING = mapOf(
		"mp4a-latm" to "mp4",
		"alac" to "mp4",
		"opus" to "ogg",
		"mpeg" to "mp3",
		"vorbis" to "ogg",
		"raw" to "wav"
	)
	private val WEIRD_CODECS = listOf("g711", "gsm")
	private val SANE_CODECS = listOf("mp3", "flac", "vorbis", "opus")
	private val SUPPORTED_PROTOCOLS = listOf(ClientInfo.Protocol.HTTP)

	private fun getTranscodingProfiles(): List<ClientInfo.TranscodingProfile> {
		return buildList {
			for (codec in SANE_CODECS) {
				ClientInfo.TranscodingProfile(
					audioCodec = codec,
					container = CONTAINER_MAPPING.getOrDefault(codec, codec),
					protocol = SUPPORTED_PROTOCOLS.first()
				).let(::add)
			}
		}
	}

	fun buildClientInfo(
		transcodingProfiles: List<ClientInfo.TranscodingProfile> = getTranscodingProfiles()
	): ClientInfo {
		val baseClientInfo = ClientInfo(
			name = "Navic",
			platform = "Android"
		)

		val nativeProfilesPair = getNativeProfilesWithLimitations()
		val audioLimitations = nativeProfilesPair.mapNotNull { it.second }
		val nativeProfiles = nativeProfilesPair.map { it.first }
		val ffmpegProfiles = getFfmpegDirectPlayProfiles()

		val allProfiles = nativeProfiles + ffmpegProfiles

		val mergedProfiles = allProfiles
			.flatMap { profile ->
				profile.containers.map { container ->
					profile.copy(
						containers = listOf(
							container
						)
					)
				}
			}
			.groupBy { it.containers.first() }
			.values
			.map { group ->
				group.reduce { acc, profile -> acc.merge(profile) }
			}

		return baseClientInfo.copy(
			directPlayProfiles = mergedProfiles,
			codecProfiles = audioLimitations.distinctBy { it.name },
			transcodingProfiles = transcodingProfiles
		)
	}


	private fun getFfmpegDirectPlayProfiles(): List<ClientInfo.DirectPlayProfile> {
		if (!FfmpegLibrary.isAvailable()) {
			Logger.w(TAG, "ffmpeg is currently unavaliable")
			return emptyList()
		}

		return buildList {
			for (codec in KNOWN_SUPPORTED_FFMPEG_DECODERS) {
				if (!FfmpegLibrary.supportsFormat("$MIMETYPE_PREFIX$codec")) {
					continue
				}
				val container = CONTAINER_MAPPING.getOrDefault(codec, codec)

				ClientInfo.DirectPlayProfile(
					audioCodecs = listOf(codec),
					protocols = SUPPORTED_PROTOCOLS,
					containers = listOf(container),
					// "ALAC supports up to 8 channels of audio"
					// https://en.wikipedia.org/wiki/Apple_Lossless_Audio_Codec
					// "FLAC supports from 1 to 8 channels per stream."
					// https://xiph.org/flac/faq.html#general__channels
					maxAudioChannels = when (codec) {
						"flac", "alac" -> 8
						else -> null
					}
				).let(::add)
			}
		}
	}

	// what Android supports for audio codecs
	private fun getNativeProfilesWithLimitations(): List<Pair<ClientInfo.DirectPlayProfile, ClientInfo.CodecProfile?>> {
		val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
		val codecInfos = try {
			codecList.codecInfos
		} catch (e: Exception) {
			Logger.w(TAG, "i don't feel so good...", e)
			emptyArray<MediaCodecInfo>()
		}
		return buildList {
			for (codecInfo in codecInfos) {
				if (codecInfo.isEncoder) continue

				val supportedTypes = codecInfo.supportedTypes

				for (mimetype in supportedTypes) {
					if (!mimetype.startsWith(MIMETYPE_PREFIX)) {
						continue
					}
					
					val strippedMimetype = mimetype.removePrefix(MIMETYPE_PREFIX)
						.lowercase()
					val codec =
						CORRECTED_CODEC_NAME.getOrDefault(strippedMimetype, strippedMimetype)

					if (WEIRD_CODECS.contains(codec)) {
						continue
					}

					val container =
						CONTAINER_MAPPING.getOrDefault(strippedMimetype, strippedMimetype)
							.lowercase()

					try {
						val capabilities = codecInfo.getCapabilitiesForType(mimetype)
						val audioCaps = capabilities.audioCapabilities
						if (audioCaps != null) {
							val maxChannels = audioCaps.maxInputChannelCount
							val sampleRates = audioCaps.supportedSampleRates

							Pair(
								ClientInfo.DirectPlayProfile(
									audioCodecs = listOf(codec),
									protocols = SUPPORTED_PROTOCOLS,
									containers = listOf(container),
									maxAudioChannels = maxChannels
								), ClientInfo.CodecProfile(
									name = codec,
									type = ClientInfo.CodecType.AudioCodec,
									limitations = listOf(
										ClientInfo.CodecProfile.Limitation(
											comparison = ClientInfo.CodecProfile.Limitation.Comparison.Equals,
											name = ClientInfo.CodecProfile.Limitation.Type.AudioSampleRate,
											required = false,
											values = sampleRates.map { it.toString() }),
										ClientInfo.CodecProfile.Limitation(
											comparison = ClientInfo.CodecProfile.Limitation.Comparison.Equals,
											name = ClientInfo.CodecProfile.Limitation.Type.AudioChannels,
											values = (1..maxChannels).toList()
												.map { it.toString() })
									)
								)
							).let(::add)
						}
					} catch (e: Exception) {
						Logger.e(
							"PlaybackCapabilities",
							"unable to query codec info for ${codecInfo.name}, appending bare information to the profile",
							e
						)
						Pair(
							ClientInfo.DirectPlayProfile(
								audioCodecs = listOf(codec),
								protocols = SUPPORTED_PROTOCOLS,
								containers = listOf(container)
							), null
						).let(::add)
					}
				}
			}
		}
	}
}


private fun ClientInfo.DirectPlayProfile.merge(other: ClientInfo.DirectPlayProfile): ClientInfo.DirectPlayProfile {
	return this.copy(
		audioCodecs = ((this.audioCodecs) + (other.audioCodecs)).distinct(),
		containers = ((this.containers) + (other.containers)).distinct()
	)
}
