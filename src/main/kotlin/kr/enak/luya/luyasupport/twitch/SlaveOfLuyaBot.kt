package kr.enak.luya.luyasupport.twitch

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import kotlinx.coroutines.async
import kr.enak.luya.luyasupport.discord.patch.toCommandDto
import kr.enak.luya.luyasupport.twitch.abc.AbstractPrefixCommand
import kr.enak.luya.luyasupport.twitch.commands.CommandClipImpl
import kr.enak.luya.luyasupport.twitch.commands.CommandFollowingImpl
import kr.enak.luya.luyasupport.twitch.commands.CommandMarkTimestampViaDiscordImpl
import kr.enak.luya.luyasupport.twitch.config.TwitchBotConfiguration
import kr.enak.luya.luyasupport.twitch.patch.Coroutines
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class SlaveOfLuyaBot(
    private val config: TwitchBotConfiguration,
    private val twitchService: TwitchService,
) {
    @Autowired
    private lateinit var context: ApplicationContext

    private val commandMap: HashMap<String, AbstractPrefixCommand> = hashMapOf()

    @PostConstruct
    fun init() {
        bindHandlers()
        registerCommands(
            listOf<AbstractPrefixCommand>(
                CommandFollowingImpl("follow", "팔로우"),
                CommandMarkTimestampViaDiscordImpl("mark", "마커"),
                CommandClipImpl("clip", "클립"),
            )
        )
        joinChannels(config.channels)
    }

    suspend fun onChat(event: ChannelMessageEvent) {
        val command = decideCommand(event.message)
        if (command != null) {
            val response = command.execute(event.toCommandDto())
            if (response != null)
                event.reply(event.twitchChat, response)
        }
    }

    private fun decideCommand(message: String): AbstractPrefixCommand? {
        val rawCommand = message.split(" ").firstOrNull()
        return commandMap[rawCommand]
    }

    private fun joinChannels(channels: List<String>) {
        channels.forEach(twitchService.chat::joinChannel)
    }

    private fun bindHandlers() {
        twitchService.eventManager.apply {
            onEvent(ChannelMessageEvent::class.java) {
                Coroutines.ioScope.async {
                    onChat(it)
                }
            }
            onEvent(ChannelMessageEvent::class.java) {
                Coroutines.ioScope.async {
                    CommandClipImpl.ClipURLMessageDetector.onChat(it)
                }
            }
        }
    }

    private fun registerCommands(commands: List<AbstractPrefixCommand>) {
        commands.forEach { command ->
            listOf(command.name, *command.aliases).forEach { name ->
                commandMap["${config.commandPrefix}$name"] = command
            }

            if (command is ApplicationContextAware)
                command.setApplicationContext(context)
        }
    }
}
