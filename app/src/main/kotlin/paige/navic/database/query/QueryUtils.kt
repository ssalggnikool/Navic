/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.query

import androidx.room3.RoomRawQuery

internal fun String.reverseOrder(): String {
	return this
		.replace(" ASC", " __DESC__")
		.replace(" DESC", " __ASC__")
		.replace(" __DESC__", " DESC")
		.replace(" __ASC__", " ASC")
}

internal fun createRoomRawQuery(sql: String, args: List<Any>): RoomRawQuery {
	return RoomRawQuery(sql) { statement ->
		args.forEachIndexed { index, arg ->
			val bindIndex = index + 1
			when (arg) {
				is String -> statement.bindText(bindIndex, arg)
				is Int -> statement.bindInt(bindIndex, arg)
				is Long -> statement.bindLong(bindIndex, arg)
				is Float -> statement.bindFloat(bindIndex, arg)
				is Double -> statement.bindDouble(bindIndex, arg)
				is Boolean -> statement.bindInt(bindIndex, if (arg) 1 else 0)
			}
		}
	}
}

private val letterFolds = mapOf(
	'A' to "ÁÀÂÄÅÆáàâäåæ",
	'C' to "ÇçĆćČč",
	'D' to "Đđ",
	'E' to "ÉÈÊËéèêëĚě",
	'I' to "ÍÌÎÏíìîï",
	'L' to "Łł",
	'N' to "ÑñŃń",
	'O' to "ÓÒÔÖØóòôöøŐő",
	'R' to "Řř",
	'S' to "ŚśŠšŞş",
	'T' to "Ťť",
	'U' to "ÚÙÛÜúùûüŰű",
	'Y' to "Ýýÿ",
	'Z' to "ŹźŽžŻż",
)

private val foldBranches: String = letterFolds.entries.joinToString("\n") { (letter, chars) ->
	chars.map { "WHEN '$it' THEN '$letter'" }.joinToString("\n")
}

internal fun sqlLetterExpression(columnName: String): String {
	val first = "SUBSTR($columnName, 1, 1)"
	return """
		CASE
			WHEN UPPER($first) BETWEEN 'A' AND 'Z' THEN UPPER($first)
			ELSE CASE $first
				$foldBranches
				ELSE '#'
			END
		END
	""".trimIndent()
}
