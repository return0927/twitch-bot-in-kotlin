package kr.enak.luya.luyasupport.twitch.commands

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import kotlinx.coroutines.channels.Channel
import kr.enak.luya.luyasupport.discord.patch.toCommandDto
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

        private val channels = ArrayDeque<Channel<String?>>()

        lateinit var INSTANCE: CommandClipImpl
    }

    init {
        logger = LoggerFactory.getLogger(this::class.simpleName)
        INSTANCE = this
    }

    override suspend fun execute(dto: IncomingCommandDto): String? {
        logger.info("Await until clip url is detected for user ${dto.user.name}")
        val url = Channel<String?>().also {
            channels.add(it)
        }.receive() ?: return "클립 정보 수신에 실패했습니다."

        logger.info("Fetched url from channel")
        return sendAs(url, dto)
    }

    suspend fun sendAs(url: String, dto: IncomingCommandDto) = super.execute(
        IncomingCommandDto(
            // 📝
            dto.channel, dto.user, dto.message, listOf("\uD83D\uDCDD", url)
        )
    )

    object ClipURLMessageDetector {
        suspend fun onChat(event: ChannelMessageEvent) {
            if (event.user.name != "ssakdook") return
            else if (event.message == "클립 생성을 요청했습니다.") return
            logger.info("Detected message candidate of clip url: ${event.message}")

            val match = REG.find(event.message)
            val url: String? = match?.groupValues?.get(1)

            if (channels.isNotEmpty()) channels.removeFirst().send(url)
            else if (url != null) INSTANCE.sendAs(url, event.toCommandDto())
            else logger.error("Clip URL is null and the array of channels is empty :(")
        }
    }
}