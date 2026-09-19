package paige.navic.util

fun isLocalNetworkHost(host: String): Boolean {
	val cleanHost = host.trim()
		.removePrefix("https://")
		.removePrefix("http://")
		.split(":")
		.firstOrNull()
		?.split("/")
		?.firstOrNull() ?: return false

	if (cleanHost == "localhost" || cleanHost == "127.0.0.1") return true
	if (cleanHost.endsWith(".local") || cleanHost.endsWith(".lan")) return true

	val parts = cleanHost.split(".")
	// IPv4
	if (parts.size == 4) {
		val p0 = parts[0].toIntOrNull() ?: return false
		val p1 = parts[1].toIntOrNull() ?: return false

		// Multicast (224.0.0.0/4 -> 224.0.0.0 to 239.255.255.255)
		if (p0 in 224..239) return true

		// Broadcast address
		if (cleanHost == "255.255.255.255") return true

		// RFC1918 Private Ranges
		if (p0 == 10) return true
		if (p0 == 192 && p1 == 168) return true
		if (p0 == 172 && p1 in 16..31) return true

		// Link Local (169.254.0.0/16)
		if (p0 == 169 && p1 == 254) return true

		// Carrier-Grade NAT (100.64.0.0/10 -> 100.64.0.0 to 100.127.255.255)
		if (p0 == 100 && p1 in 64..127) return true
	}

	// IPv6
	if (cleanHost.contains(":")) {
		val normalizedHost = cleanHost.lowercase()
		if (normalizedHost == "::" || normalizedHost == "::1") return true
		// fe80::/10 (Link-local), ff00::/8 (Multicast), fc00::/7 (Unique Local Address)
		if (normalizedHost.startsWith("fe80:") ||
			normalizedHost.startsWith("ff") ||
			normalizedHost.startsWith("fc") ||
			normalizedHost.startsWith("fd")) return true
	}

	return false
}
