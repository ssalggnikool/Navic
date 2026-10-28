package paige.navic.discord.uploader.catbox

import io.ktor.client.HttpClient

class CatboxUploader(
	client: HttpClient,
	override val endpoint: String = "https://catbox.moe/user/api.php",
	override val time: String? = null,
) : CatboxAbstractUploader(client)
