/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback

import android.media.MediaCodecList
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.ffmpeg.FfmpegLibrary
import dev.zt64.subsonic.api.model.ClientInfo
import paige.navic.util.Logger

@OptIn(UnstableApi::class)
object PlaybackCapabilities {
	val KNOWN_SUPPORTED_FFMPEG_DECODERS = listOf(
		"flac", "alac"
	)
	val CORRECTED_CODEC_NAME = mapOf(
		"mp4a-latm" to "aac"
	)
	val CONTAINER_MAPPING = mapOf(
		"mp4a-latm" to "mp4"
	)
	val SUPPORTED_PROTOCOLS = listOf(ClientInfo.Protocol.HTTP)

	fun buildSubsonicClientInfo(transcodingProfiles: List<ClientInfo.TranscodingProfile> = emptyList()): ClientInfo {
		val baseClientInfo = ClientInfo(
			name = "Navic",
			platform = "Android"
		)

		val nativeProfilesPair = getNativeDirectPlayProfiles()
		val audioLimitations = nativeProfilesPair.map { it.second }
		val nativeProfiles = nativeProfilesPair.map { it.first }
		val ffmpegProfiles = getFfmpegDirectPlayProfiles()

		return baseClientInfo.copy(
			directPlayProfiles = (nativeProfiles + ffmpegProfiles).groupBy { it.containers }.values.map {
				it.reduce { acc, profile ->
					acc.merge(
						profile
					)
				}
			}.distinctBy { it.audioCodecs },
			codecProfiles = audioLimitations.distinctBy { it.name },
			transcodingProfiles = transcodingProfiles
		)
	}


	fun getFfmpegDirectPlayProfiles(): List<ClientInfo.DirectPlayProfile> {
		if (!FfmpegLibrary.isAvailable()) error("ffmpeg is currently unavaliable")

		return buildList {
			for (codec in KNOWN_SUPPORTED_FFMPEG_DECODERS) {
				if (!FfmpegLibrary.supportsFormat("audio/$codec")) continue

				if (codec == "alac") {
					ClientInfo.DirectPlayProfile(
						audioCodecs = listOf(codec),
						protocols = SUPPORTED_PROTOCOLS,
						containers = listOf("mp4"),
						// "ALAC supports up to 8 channels of audio"
						// https://en.wikipedia.org/wiki/Apple_Lossless_Audio_Codec
						maxAudioChannels = 8
					).let(::add)
					continue
				}

				ClientInfo.DirectPlayProfile(
					audioCodecs = listOf(codec),
					protocols = SUPPORTED_PROTOCOLS,
					containers = listOf(codec),
					// "FLAC supports from 1 to 8 channels per stream."
					// https://xiph.org/flac/faq.html#general__channels
					maxAudioChannels = when (codec) {
						"flac" -> 8
						else -> null
					}
				).let(::add)
			}
		}
	}

	// what Android supports for audio codecs
	fun getNativeDirectPlayProfiles(): List<Pair<ClientInfo.DirectPlayProfile, ClientInfo.CodecProfile>> {
		val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
		val codecInfos = codecList.codecInfos
		return buildList {
			for (codecInfo in codecInfos) {
				if (codecInfo.isEncoder) continue

				val supportedTypes = codecInfo.supportedTypes

				for (mimetype in supportedTypes) {
					if (!mimetype.startsWith("audio/")) {
						continue
					}
					val strippedMimetype = mimetype.removePrefix("audio/")
					val codec = CORRECTED_CODEC_NAME.getOrDefault(strippedMimetype, strippedMimetype)
					val container = CONTAINER_MAPPING.getOrDefault(strippedMimetype, strippedMimetype)

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
						Logger.e("PlaybackCapabilities", "unable to query codec info for ${codecInfo.name}", e)
					}
				}
			}
		}
	}
}


fun ClientInfo.DirectPlayProfile.merge(other: ClientInfo.DirectPlayProfile): ClientInfo.DirectPlayProfile {
	return this.copy(
		audioCodecs = ((this.audioCodecs) + (other.audioCodecs)).distinct(),
		containers = ((this.containers) + (other.containers)).distinct()
	)
}
