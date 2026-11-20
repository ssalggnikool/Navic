/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.subsonic.mapper

import dev.zt64.subsonic.api.model.ChatMessage as ApiChatMessage
import paige.navic.domain.model.DomainChatMessage

fun ApiChatMessage.toDomainModel() = DomainChatMessage(
	username = username,
	time = time,
	message = message
)
