package paige.navic.discord.uploader.catbox

import io.ktor.client.HttpClient

class LitterboxUploader(
	client: HttpClient,
	override val endpoint: String = "https://litterbox.catbox.moe/resources/internals/api.php",
	override val time: String? = "72h",
) : CatboxAbstractUploader(client)
