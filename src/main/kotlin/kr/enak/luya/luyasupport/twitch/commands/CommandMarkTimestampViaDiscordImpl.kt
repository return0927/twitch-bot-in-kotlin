package kr.enak.luya.luyasupport.twitch.commands

import club.minnced.discord.webhook.external.JDAWebhookClient
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import com.github.twitch4j.helix.domain.Stream
import kr.enak.luya.luyasupport.discord.DiscordTimestampFormat
import kr.enak.luya.luyasupport.discord.patch.DiscordWebhookMessagePatch
import kr.enak.luya.luyasupport.discord.toDiscordFormat
import kr.enak.luya.luyasupport.twitch.TwitchService
import kr.enak.luya.luyasupport.twitch.abc.AbstractPrefixCommand
import kr.enak.luya.luyasupport.twitch.abc.IncomingCommandDto
import kr.enak.luya.luyasupport.twitch.config.TwitchBotConfiguration
import kr.enak.luya.luyasupport.twitch.token
import kr.enak.luya.luyasupport.twitch.utils.format
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Webhook
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.time.Instant

class CommandMarkTimestampViaDiscordImpl(
    name: String,
    vararg aliases: String,
) : AbstractPrefixCommand(name, aliases), ApplicationContextAware {
    companion object {
        const val COOLDOWN = 5000L

        private val client: WebClient = WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
//                    .wiretap(this::class.java.canonicalName, LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
                )
            )
            .build()

        fun String.formatThumbnailUrl(width: Long = 1920, height: Long = 1080): String =
            this.replace("{width}", "$width")
                .replace("{height}", "$height")
    }

    private val coolDownMap: MutableMap<String, Long> = hashMapOf()

    private val infoThreadIdMap: MutableMap<String, Long> = hashMapOf()

    private lateinit var context: ApplicationContext

    private val twitchConfig: TwitchBotConfiguration
        get() = context.getBean(TwitchBotConfiguration::class.java)

    private val twitchService: TwitchService
        get() = context.getBean(TwitchService::class.java)

    private fun postConstruct() {

    }

    override fun execute(dto: IncomingCommandDto): String? {
        if (!isCoolToRun(dto.channel.name))
            return null

        return rawExecute(dto).also {
            markExecutedNow(dto.channel.name)
        }
    }

    private fun rawExecute(dto: IncomingCommandDto): String? {
        val hookUrl = twitchConfig.timestampHooks[dto.channel.name]
            ?: return "아직 설정되지 않은 기능이에요"

        TwitchService.client.helix.apply {
            val info = getStreams(
                token(), null, null, 1, null, null, null,
                listOf("jumiluya")
            ).execute()

            val stream = info.streams.getOrNull(0)
                ?: return "채널이 아직 방송중이지 않은 것 같네요..!"

            reportUptime(hookUrl, stream)
        }

        return null
    }

    private fun reportUptime(hook: String, stream: Stream) {
        val startedAtInTimestamp = stream.startedAtInstant.toEpochMilli() / 1000
        val client = JDAWebhookClient.withUrl(hook)

        if (!infoThreadIdMap.containsKey(stream.userName)) {
            val embed = EmbedBuilder()
                .setTitle(stream.title, "https://www.twitch.tv/${stream.userName}")
                .appendDescription(
                    "방송 시작: %s (%s)".format(
                        startedAtInTimestamp.toDiscordFormat(DiscordTimestampFormat.LONG),
                        startedAtInTimestamp.toDiscordFormat(DiscordTimestampFormat.RELATIVE),
                    )
                )
                .setImage(stream.thumbnailUrl.formatThumbnailUrl())
                .build()

            val localizedTimestamp = Duration
                .between(Instant.ofEpochMilli(0), stream.startedAtInstant)

            val payload = DiscordWebhookMessagePatch.embeds(WebhookEmbedBuilder.fromJDA(embed).build())
            payload.threadName = localizedTimestamp.format("YYYY-MM-dd HH:mm")

            val message = client
                .send(payload)
                .get()
        }
    }

    private suspend fun sendHookToDiscord(hook: Webhook, content: String, thumbnail: String): Long {
        return 0L
    }

    private fun isCoolToRun(channelName: String): Boolean {
        val lastExecuted = coolDownMap[channelName] ?: 0

        return System.currentTimeMillis() - lastExecuted > COOLDOWN
    }

    private fun markExecutedNow(channelName: String) {
        coolDownMap[channelName] = System.currentTimeMillis()
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
        postConstruct()
    }
}