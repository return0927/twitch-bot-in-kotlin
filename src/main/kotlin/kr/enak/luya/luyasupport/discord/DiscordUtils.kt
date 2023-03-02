package kr.enak.luya.luyasupport.discord

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import reactor.core.publisher.Mono


enum class DiscordTimestampFormat(val code: String) {
    /*
     t: Short time (e.g 9:41 PM)
     T: Long Time (e.g. 9:41:30 PM)
     d: Short Date (e.g. 30/06/2021)
     D: Long Date (e.g. 30 June 2021)
     f (default): Short Date/Time (e.g. 30 June 2021 9:41 PM)
     F: Long Date/Time (e.g. Wednesday, June, 30, 2021 9:41 PM)
     R: Relative Time (e.g. 2 months ago, in an hour)
     */
    SHORT_TIME("t"), LONG_TIME("T"),
    SHORT_DATE("d"), LONG_DATE("D"),
    SHORT("f"), LONG("F"),
    RELATIVE("R")
}

/*
{
  "type": 1,
  "id": "0123456789012345678",
  "name": "Captain Hook",
  "avatar": null,
  "channel_id": "012345678901234567",
  "guild_id": "012345678901234567",
  "application_id": null,
  "token": "WEBHOOK TOKEN HERE"
}
 */
@Serializable
data class DiscordHookInfoResponse(
    val type: Int,
    val id: String,
    val name: String,
    val avatar: String?,
    @SerialName("channel_id") val channelId: String,
    @SerialName("guild_id") val guildId: String,
    @SerialName("application_id") val applicationId: String?,
    val token: String,
)

fun Long.toDiscordFormat(type: DiscordTimestampFormat = DiscordTimestampFormat.LONG) =
    "<t:$this:${type.code}>"

fun WebClient.getHookInfo(url: String): DiscordHookInfoResponse? = runBlocking {
    return@runBlocking withContext(Dispatchers.IO) {
        get()
            .uri(url)
            .retrieve()
            .onStatus(HttpStatus::isError) {
                Mono.justOrEmpty(null)
            }
            .awaitBodyOrNull<DiscordHookInfoResponse>()
    }
}
