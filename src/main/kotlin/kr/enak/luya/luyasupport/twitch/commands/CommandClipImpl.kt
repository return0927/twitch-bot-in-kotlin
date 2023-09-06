package kr.enak.luya.luyasupport.twitch.commands

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import kotlinx.coroutines.CompletableDeferred
import kr.enak.luya.luyasupport.twitch.abc.IncomingCommandDto

class CommandClipImpl(
    name: String,
    vararg aliases: String,
) : CommandMarkTimestampViaDiscordImpl(
    name,
    aliases = aliases
) {
    companion object {
        private val REG = """@[^,]+, 클립이 생성되었습니다 -> (https://vod\.twip\.kr/clip/.+)""".toRegex()

        private val received = CompletableDeferred<String?>()
    }

    override suspend fun execute(dto: IncomingCommandDto): String? {
        val url = received.await() ?: return "클립 정보 수신에 실패했습니다."

        return super.execute(
            IncomingCommandDto(
                // 📝
                dto.channel, dto.user, dto.message, listOf("\uD83D\uDCDD", url)
            )
        )
    }

    object ClipURLMessageDetector {
        fun onChat(event: ChannelMessageEvent) {
            val match = REG.find(event.message)
            val url: String? = match?.groupValues?.get(1)
            received.complete(url)
        }
    }
}