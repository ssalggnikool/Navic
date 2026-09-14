package paige.navic.domain.manager

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import dev.zt64.subsonic.api.model.Role
import dev.zt64.subsonic.api.model.User
import dev.zt64.subsonic.client.SubsonicAuth
import dev.zt64.subsonic.client.SubsonicClient
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.http
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class SessionManager(
	private val settings: Settings,
	private val preferenceManager: PreferenceManager
) {
	private companion object {
		val PROXY_URL_REGEX = Regex("socks[4-5]?://(.+):(\\d+)")
	}

	val isLoggedIn: StateFlow<Boolean>
		field = MutableStateFlow(false)

	private var currentUser: User? = null
	private val mutex = Mutex()
	private val scope = CoroutineScope(Dispatchers.IO)

	var api: SubsonicClient = createClient(
		instanceUrl = settings.getString("instanceUrl", ""),
		username = settings.getString("username", ""),
		password = settings.getString("password", ""),
	)
		private set

	init {
		isLoggedIn.value = settings.getStringOrNull("username") != null
		if (isLoggedIn.value) getCachedUser()
	}

	private fun createClient(
		instanceUrl: String,
		username: String,
		password: String,
	) = SubsonicClient.Companion(
		baseUrl = instanceUrl,
		auth = SubsonicAuth.Token(
			username = username,
			password = password,
		),
		client = "Navic",
		clientConfig = {
			install(UserAgent) {
				agent = "Navic"
			}

			/*
			 	this validator shouldn't care if the status code is 2xx or 3xx, we'll be validating against the response anyway
			 	this is a workaround for the stream endpoint returning 200 with an error body
			 */
			HttpResponseValidator {
				validateResponse { response ->
					val contentType = response.headers["content-type"]

					if (contentType == "application/json") {
						try {
							val objectResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject

							val subsonicResponse = objectResponse["subsonic-response"]?.jsonObject

							if (subsonicResponse != null) {
								val errorObject = subsonicResponse["error"]?.jsonObject

								if (errorObject != null) {
									// something has gone wrong with request, throw an error immediately
									throw UnhandledSubsonicException(
										"we got an error that isn't being handled by subsonic-kotlin, bug zt about it",
										errorObject,
										response
									)
								}
							}
						} catch (_: Exception) {
							// probably not our business, let something else handle the exception
						}
					}
				}
			}

			val proxyUrl = preferenceManager.proxyUrl

			if (proxyUrl.isNotBlank()) {
				engine {
					// socks is not tested but the parsing here should just work... hopefully...
					if (proxyUrl.startsWith("http")) {
						proxy = ProxyBuilder.http(proxyUrl)
					} else if (proxyUrl.startsWith("socks")) {
						val match = PROXY_URL_REGEX.matchEntire(proxyUrl)

						if (match?.groupValues?.isNotEmpty() == true) {
							val port = match.groupValues.getOrNull(1)?.toIntOrNull()

							proxy = ProxyBuilder.socks(
								match.groupValues[0],
								port ?: 1080
							)
						}
					}
				}
			}

			val customHeaders = preferenceManager.customHeadersMap()
			if (customHeaders.isNotEmpty()) {
				defaultRequest {
					customHeaders.forEach { (key, value) -> header(key, value) }
				}
			}
		}
	)

	suspend fun login(
		instanceUrl: String,
		username: String,
		password: String
	) {
		val client = createClient(instanceUrl, username, password)

		try {
			client.ping()
			fetchCurrentUser(username, client)
		} catch (e: Exception) {
			// TODO: custom exception instead of the generic "Exception"
			throw Exception(
				"Failed to connect to the instance. Please check your credentials and try again.",
				e
			)
		}

		settings["instanceUrl"] = instanceUrl
		settings["username"] = username
		settings["password"] = password

		api = client
		isLoggedIn.value = true
	}

	fun logout() {
		settings["username"] = null
		settings["password"] = null
		isLoggedIn.value = false
		currentUser = null
	}

	fun refreshClient() {
		api = createClient(
			instanceUrl = settings.getString("instanceUrl", ""),
			username = settings.getString("username", ""),
			password = settings.getString("password", ""),
		)
	}

	fun getCoverArtUrl(coverArtId: String) = api.getCoverArtUrl(
		coverArtId,
		auth = true,
		size = "${preferenceManager.coverArtQuality.value}"
	)


	private suspend fun fetchCurrentUser(
		username: String = settings.getString("username", ""),
		client: SubsonicClient = api
	): User? {
		mutex.withLock {
			if (username.isNotBlank()) {
				currentUser = client.getUser(username)
				return currentUser
			}
		}

		// TODO: custom exception instead of the generic "Exception"
		throw Exception("Failed to get current user because the username is blank")
	}

	fun getCachedUser(): User? {
		if (currentUser != null) {
			return currentUser
		}

		if (currentUser == null) {
			scope.launch {
				fetchCurrentUser()
			}
		}

		return currentUser
	}
}

fun User.hasRole(role: Role): Boolean {
	return this.roles.contains(role)
}

fun User.canShare(): Boolean {
	return this.hasRole(Role.SHARE)
}

fun SessionManager.canUserShare(): Boolean {
	return this.getCachedUser()?.canShare() ?: false
}


class UnhandledSubsonicException(
	message: String,
	jsonError: JsonObject,
	response: HttpResponse
): Exception("$message (status code ${response.status}): $jsonError")
