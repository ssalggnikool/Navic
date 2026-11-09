/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server

open class ServerException(message: String, cause: Throwable? = null) : Exception(message, cause)

class InvalidCredentialsException(message: String, cause: Throwable? = null) :
	ServerException(message, cause)
