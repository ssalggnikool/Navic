/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util

import com.appmattus.crypto.Algorithm
import kotlin.math.log10
import kotlin.math.pow
import kotlin.time.Duration

/**
 * Formats a `Duration` as a `String` in HH:MM:SS
 * format. If there is no hours, the 00: at the
 * beginning will be omitted
 */
fun Duration.toHoursMinutesSeconds(): String {
	val totalSeconds = inWholeSeconds

	val hours = totalSeconds / 3600
	val minutes = (totalSeconds % 3600) / 60
	val seconds = totalSeconds % 60

	fun Long.twoDigits() = toString().padStart(2, '0')

	return if (hours > 0) {
		"${hours.twoDigits()}:${minutes.twoDigits()}:${seconds.twoDigits()}"
	} else {
		"${minutes.twoDigits()}:${seconds.twoDigits()}"
	}
}

/**
 * Formats a `Duration` as a summary string, ex. 12h 30m
 */
fun Duration.toSummaryString(): String {
	val hours = inWholeHours
	val minutes = inWholeMinutes % 60
	return if (hours > 0) {
		"${hours}h ${minutes}m"
	} else {
		"${minutes}m"
	}
}

/**
 * Formats a `Long` as a human-readable file size
 * string, e.g. 1 GB
 */
fun Long.toFileSize(): String {
	if (this <= 0) return "0 B"
	val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
	val digitGroups = (log10(this.toDouble()) / log10(1000.0)).toInt()

	val size = this / 1000.0.pow(digitGroups.toDouble())
	val roundedSize = (size * 100).toInt() / 100.0

	return "$roundedSize ${units[digitGroups]}"
}

/**
 * Hash `String` using a non-cryptographic and non-expensive algorithm (xxHash), returns a formatted hex string.
 */
fun String.xxHash32(): String {
	val digest = Algorithm.XXHash32().createDigest()
	return digest.digest(this.toByteArray(Charsets.UTF_8))
		.joinToString("") { byte -> "%02x".format(byte) }
}
