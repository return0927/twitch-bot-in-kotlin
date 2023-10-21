package kr.enak.luya.luyasupport.twitch.commands

import club.minnced.discord.webhook.external.JDAWebhookClient
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import com.github.twitch4j.helix.domain.Stream
import com.github.twitch4j.helix.domain.User
import io.netty.handler.logging.LogLevel
import kr.enak.luya.luyasupport.discord.DiscordTimestampFormat
import kr.enak.luya.luyasupport.discord.patch.DiscordWebhookMessagePatch
import kr.enak.luya.luyasupport.discord.patch.send
import kr.enak.luya.luyasupport.discord.toDiscordFormat
import kr.enak.luya.luyasupport.twitch.TwitchService
import kr.enak.luya.luyasupport.twitch.abc.AbstractPrefixCommand
import kr.enak.luya.luyasupport.twitch.abc.IncomingCommandDto
import kr.enak.luya.luyasupport.twitch.config.TwitchBotConfiguration
import kr.enak.luya.luyasupport.twitch.token
import kr.enak.luya.luyasupport.twitch.utils.format
import net.dv8tion.jda.api.EmbedBuilder
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat
import java.time.Duration
import java.time.Instant

open class CommandMarkTimestampViaDiscordImpl(
    name: String,
    vararg aliases: String,
) : AbstractPrefixCommand(name, aliases), ApplicationContextAware {
    companion object {
        const val COOLDOWN = 5000L

        private val client: WebClient = WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
                        .wiretap(this::class.java.canonicalName, LogLevel.TRACE, AdvancedByteBufFormat.TEXTUAL)
                )
            )
            .build()

        fun String.formatThumbnailUrl(width: Long = 1920, height: Long = 1080): String =
            this.replace("{width}", "$width")
                .replace("{height}", "$height")

        private val coolDownMap: MutableMap<String, Long> = hashMapOf()

        private val infoThreadIdMap: MutableMap<String, Long> = hashMapOf()

        private val userInfoCacheMap: MutableMap<String, User> = hashMapOf()

        private fun isCoolToRun(channelName: String): Boolean {
            val lastExecuted = coolDownMap[channelName] ?: 0

            return System.currentTimeMillis() - lastExecuted > COOLDOWN
        }

        private fun markExecutedNow(channelName: String) {
            coolDownMap[channelName] = System.currentTimeMillis()
        }
    }

    private lateinit var context: ApplicationContext

    private val twitchConfig: TwitchBotConfiguration
        get() = context.getBean(TwitchBotConfiguration::class.java)

    private val twitchService: TwitchService
        get() = context.getBean(TwitchService::class.java)

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private fun postConstruct() {

    }

    override suspend fun execute(dto: IncomingCommandDto): String? {
        if (!isCoolToRun(dto.channel.name))
            return null

        return rawExecute(dto).also {
            markExecutedNow(dto.channel.name)
        }
    }

    private fun rawExecute(dto: IncomingCommandDto): String? {
        val hookUrl = twitchConfig.timestampHooks[dto.channel.name]
            ?: return "아직 설정되지 않은 기능이에요"

        val description = dto.args.joinToString(" ")

        TwitchService.client.helix.apply {
            val infoReq = getStreams(
                token(), null, null, 1, null, null, null,
                listOf(dto.channel.name)
            ).queue()

            val info = try {
                infoReq.get(1L, TimeUnit.SECONDS)
            } catch (e: Throwable) {
                null
            }

            val stream = info?.streams?.getOrNull(0)
                ?: return "채널이 아직 방송중이지 않은 것 같네요..!"

            val userListToFind: List<String> = listOf(stream.userId, dto.user.id)
                .filterNot { userInfoCacheMap.containsKey(it) }

            if (userListToFind.isNotEmpty()) {
                val userInfoList = getUsers(
                    token(), userListToFind, null
                ).execute()

                userInfoList.users.forEach {
                    userInfoCacheMap[it.login] = it
                }
            }

            val streamerInfo = userInfoCacheMap[stream.userLogin]
                ?: return "채널 정보를 찾을 수 없어요.."
            val chatterInfo = userInfoCacheMap[dto.user.name]
                ?: return "요청하신 분의 정보를 찾을 수 없어요.."

            return try {
                reportUptime(hookUrl, stream, chatterInfo, description)
            } catch (ex: RuntimeException) {
                logger.warn(
                    "Error on performing a Discord webhook for Twitch channel ${dto.channel.name} on request of ${dto.user.name}",
                    ex
                )
                "얼레.. 오류가 났어요.. 미안해요ㅠ"
            }
        }
    }

    private fun reportUptime(hook: String, stream: Stream, requesterInfo: User, description: String): String? {
        val startedAtInTimestamp = stream.startedAtInstant.toEpochMilli() / 1000
        val client = JDAWebhookClient.withUrl(hook)

        if (!infoThreadIdMap.containsKey(stream.id)) {
            val embed = EmbedBuilder()
                .setTitle(stream.title.ifEmpty { "NO_TITLE" }, "https://www.twitch.tv/${stream.userLogin}")
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
                .plusHours(9)

            val payload = DiscordWebhookMessagePatch.embeds(
                WebhookEmbedBuilder.fromJDA(embed).build(),
                username = "방송 - " + stream.userName,
                avatarUrl = userInfoCacheMap[stream.userLogin]!!.profileImageUrl.formatThumbnailUrl()
            )
            payload.threadName = localizedTimestamp.format("YYYY-MM-dd HH:mm")

            val message = client
                .send(payload)
                .get()

            infoThreadIdMap[stream.id] = message.id
        }

        if (infoThreadIdMap.containsKey(stream.id)) {
            val threadId = infoThreadIdMap[stream.id] ?: throw RuntimeException("채널 스레드를 만들 수 없어요")
            val now = Instant.now().epochSecond

            client
                .onThread(threadId)
                .send(
                    content = "%s (%s)%s".format(
                        stream.uptime.format(),
                        now.toDiscordFormat(DiscordTimestampFormat.RELATIVE),
                        if (description.isNotEmpty()) " - $description" else ""
                    ),
                    username = requesterInfo.displayName + (
                            if (requesterInfo.displayName != requesterInfo.login) " (${requesterInfo.login})"
                            else ""
                            ),
                    avatarUrl = requesterInfo.profileImageUrl.formatThumbnailUrl()
                )
                .get()

            return "\uD83D\uDC99"
        }

        return "디스코드에 새 글을 만들지 못했어요..!"
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
        postConstruct()
    }
}