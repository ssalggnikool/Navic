/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback

import android.content.res.Resources
import android.media.MediaCodecList
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.ffmpeg.FfmpegLibrary
import dev.zt64.subsonic.api.model.ClientInfo
import paige.navic.R
import paige.navic.util.Logger

@OptIn(UnstableApi::class)
object PlaybackCapabilities {
	val KNOWN_SUPPORTED_FFMPEG_DECODERS = listOf(
		"flac", "alac"
	)
	val SUPPORTED_PROTOCOLS = listOf(ClientInfo.Protocol.HTTP)

	fun buildSubsonicClientInfo(transcodingProfiles: List<ClientInfo.TranscodingProfile> = emptyList()): ClientInfo {
		val baseClientInfo = ClientInfo(
			name = Resources.getSystem().getString(R.string.app_name), // lol
			platform = "Android/${Build.VERSION.CODENAME}"
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
			},
			codecProfiles = audioLimitations,
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
				)
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

				val name = codecInfo.name
				val supportedTypes = codecInfo.supportedTypes

				for (mimetype in supportedTypes) {
					// if ffmpeg supports it, then it's just irrelevant what MediaCodec returns, so skip it
					if (!mimetype.startsWith("audio/") || FfmpegLibrary.supportsFormat(mimetype)) continue
					try {
						val capabilities = codecInfo.getCapabilitiesForType(mimetype)
						val audioCaps = capabilities.audioCapabilities
						if (audioCaps != null) {
							val maxChannels = audioCaps.maxInputChannelCount
							val sampleRates = audioCaps.supportedSampleRates

							Pair(
								ClientInfo.DirectPlayProfile(
									audioCodecs = listOf(name),
									protocols = SUPPORTED_PROTOCOLS,
									containers = listOf(name),
									maxAudioChannels = maxChannels
								), ClientInfo.CodecProfile(
									name = name,
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
						Logger.e("PlaybackCapabilities", "unable to query codec info for $name", e)
					}
				}
			}
		}
	}
}


fun ClientInfo.DirectPlayProfile.merge(other: ClientInfo.DirectPlayProfile): ClientInfo.DirectPlayProfile {
	return this.copy(
		audioCodecs = (this.audioCodecs) + (other.audioCodecs),
		containers = (this.containers) + (other.containers)
	)
}
