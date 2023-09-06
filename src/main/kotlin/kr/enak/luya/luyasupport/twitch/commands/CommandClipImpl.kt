package kr.enak.luya.luyasupport.twitch.commands

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import kotlinx.coroutines.CompletableDeferred
import kr.enak.luya.luyasupport.twitch.abc.IncomingCommandDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CommandClipImpl(
    name: String,
    vararg aliases: String,
) : CommandMarkTimestampViaDiscordImpl(
    name,
    aliases = aliases
) {
    companion object {
        private lateinit var logger: Logger

        private val REG = """@[^,]+, 클립이 생성되었습니다 -> (https://vod\.twip\.kr/clip/.+)""".toRegex()

        private val received = CompletableDeferred<String?>()
    }

    init {
        logger = LoggerFactory.getLogger(this::class.simpleName)
    }

    override suspend fun execute(dto: IncomingCommandDto): String? {
        logger.info("Await until clip url is detected for user ${dto.user.name}")
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
            if (event.user.id != "ssakdook") return
            else if (event.message == "클립 생성을 요청했습니다.") return
            logger.info("Detected message candidate of clip url: ${event.message}")

            val match = REG.find(event.message)
            val url: String? = match?.groupValues?.get(1)
            received.complete(url)
        }
    }
}