/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige, Nightdavisao
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
object PlaybackUtils {
	private const val TAG = "PlaybackCapabilities"
	private const val MIMETYPE_PREFIX = "audio/"

	// "ALAC supports up to 8 channels of audio"
	// https://en.wikipedia.org/wiki/Apple_Lossless_Audio_Codec
	// "FLAC supports from 1 to 8 channels per stream."
	// https://xiph.org/flac/faq.html#general__channels
	private const val FFMPEG_MAX_CHANNELS = 8

	private val PROTOCOL = ClientInfo.Protocol.HTTP
	private val SUPPORTED_PROTOCOLS = listOf(PROTOCOL)

	private val KNOWN_SUPPORTED_FFMPEG_DECODERS = listOf("flac", "alac")

	private val CORRECTED_CODEC_NAME = mapOf(
		"mp4a-latm" to "aac",
		"mpeg" to "mp3",
		"raw" to "wav"
	)

	private val CODEC_CONTAINERS = mapOf(
		"mp3" to listOf("mp3"),
		"aac" to listOf("mp4", "m4a", "aac"),
		"alac" to listOf("mp4", "m4a"),
		"flac" to listOf("flac"),
		"vorbis" to listOf("ogg"),
		"opus" to listOf("ogg", "opus"),
		"wav" to listOf("wav")
	)
	private val SUPPORTED_CODECS = CODEC_CONTAINERS.keys

	private val TRANSCODING_CONTAINERS = linkedMapOf(
		"opus" to "ogg",
		"mp3" to "mp3"
	)

	private data class CodecCaps(
		val maxChannels: Int = 0,
		val sampleRates: Set<Int> = sortedSetOf()
	)

	private val codecCapabilities: Map<String, CodecCaps> by lazy {
		buildMap {
			addNativeDecoders(this)
			addFfmpegDecoders(this)
		}
	}

	fun getPrettyFormatName(mimeType: String): String {
		val name = mimeType.lowercase().removePrefix(MIMETYPE_PREFIX)
		return CORRECTED_CODEC_NAME.getOrDefault(name, name)
	}

	private fun getTranscodingProfiles(): List<ClientInfo.TranscodingProfile> =
		TRANSCODING_CONTAINERS.map { (codec, container) ->
			ClientInfo.TranscodingProfile(
				audioCodec = codec,
				container = container,
				protocol = PROTOCOL
			)
		}

	fun buildClientInfo(
		transcodingProfiles: List<ClientInfo.TranscodingProfile> = getTranscodingProfiles()
	): ClientInfo {
		val capabilities = codecCapabilities

		val directPlayProfiles = capabilities.entries
			.flatMap { (codec, caps) ->
				val channels = caps.maxChannels.takeIf { it > 0 }
				CODEC_CONTAINERS.getValue(codec).map { container ->
					Triple(container, channels, codec)
				}
			}
			.groupBy({ it.first to it.second }, { it.third })
			.map { (key, codecs) ->
				ClientInfo.DirectPlayProfile(
					audioCodecs = codecs,
					protocols = SUPPORTED_PROTOCOLS,
					containers = listOf(key.first),
					maxAudioChannels = key.second
				)
			}

		val codecProfiles = capabilities.mapNotNull { (codec, caps) ->
			val limitations = buildList {
				if (caps.sampleRates.isNotEmpty()) {
					add(
						ClientInfo.CodecProfile.Limitation(
							comparison = ClientInfo.CodecProfile.Limitation.Comparison.Equals,
							name = ClientInfo.CodecProfile.Limitation.Type.AudioSampleRate,
							required = false,
							values = caps.sampleRates.map { it.toString() }
						)
					)
				}
				if (caps.maxChannels > 0) {
					add(
						ClientInfo.CodecProfile.Limitation(
							comparison = ClientInfo.CodecProfile.Limitation.Comparison.Equals,
							name = ClientInfo.CodecProfile.Limitation.Type.AudioChannels,
							values = (1..caps.maxChannels).map { it.toString() }
						)
					)
				}
			}
			if (limitations.isEmpty()) null
			else ClientInfo.CodecProfile(
				name = codec,
				type = ClientInfo.CodecType.AudioCodec,
				limitations = limitations
			)
		}

		return ClientInfo(
			name = "Navic",
			platform = "Android",
			directPlayProfiles = directPlayProfiles,
			codecProfiles = codecProfiles,
			transcodingProfiles = transcodingProfiles
		)
	}

	private fun addFfmpegDecoders(out: MutableMap<String, CodecCaps>) {
		if (!FfmpegLibrary.isAvailable()) {
			Logger.w(TAG, "ffmpeg is currently unavailable")
			return
		}
		for (codec in KNOWN_SUPPORTED_FFMPEG_DECODERS) {
			if (!FfmpegLibrary.supportsFormat("$MIMETYPE_PREFIX$codec")) {
				continue
			}

			out.getOrPut(codec) { CodecCaps() }.let { caps ->
				out[codec] = caps.copy(
					maxChannels = maxOf(caps.maxChannels, FFMPEG_MAX_CHANNELS)
				)
			}
		}
	}

	private fun addNativeDecoders(out: MutableMap<String, CodecCaps>) {
		val codecInfos = try {
			MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
		} catch (e: Exception) {
			Logger.w(TAG, "unable to list media codecs, relying on ffmpeg only", e)
			return
		}

		for (info in codecInfos) {
			if (info.isEncoder) {
				continue
			}

			for (mimetype in info.supportedTypes) {
				val lower = mimetype.lowercase()
				if (!lower.startsWith(MIMETYPE_PREFIX)) {
					continue
				}

				val stripped = lower.removePrefix(MIMETYPE_PREFIX)
				val codec = CORRECTED_CODEC_NAME.getOrDefault(stripped, stripped)
				if (codec !in SUPPORTED_CODECS) {
					continue
				}
				val caps = out.getOrPut(codec) { CodecCaps() }

				try {
					val audioCaps = info.getCapabilitiesForType(mimetype).audioCapabilities
						?: continue

					out[codec] = caps.copy(
						maxChannels = maxOf(caps.maxChannels, audioCaps.maxInputChannelCount),
						sampleRates = audioCaps.supportedSampleRates.toSortedSet()
					)
				} catch (e: Exception) {
					Logger.e(
						TAG,
						"unable to query $mimetype limits for ${info.name}, advertising $codec without limits",
						e
					)
				}
			}
		}
	}
}
