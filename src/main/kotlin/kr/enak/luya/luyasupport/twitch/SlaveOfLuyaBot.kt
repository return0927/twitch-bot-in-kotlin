package kr.enak.luya.luyasupport.twitch

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import kr.enak.luya.luyasupport.twitch.abc.AbstractPrefixCommand
import kr.enak.luya.luyasupport.twitch.abc.IncomingCommandDto
import kr.enak.luya.luyasupport.twitch.commands.CommandFollowingImpl
import kr.enak.luya.luyasupport.twitch.config.TwitchBotConfiguration
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class SlaveOfLuyaBot(
    private val config: TwitchBotConfiguration,
    private val twitchService: TwitchService,
) {
    private val commandMap: HashMap<String, AbstractPrefixCommand> = hashMapOf()

    @PostConstruct
    fun init() {
        bindHandlers()
        registerCommands(listOf<AbstractPrefixCommand>(
                CommandFollowingImpl("follow", "팔로우")
        ))
        joinChannels(config.channels)
    }

    fun onChat(event: ChannelMessageEvent) {
        val command = decideCommand(event.message)
        if (command != null) {
            val response = command.execute(IncomingCommandDto(event.channel, event.user, event.message))
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
                onChat(it)
            }
        }
    }

    private fun registerCommands(commands: List<AbstractPrefixCommand>) {
        commands.forEach { command ->
            listOf(command.name, *command.aliases).forEach { name ->
                commandMap["${config.commandPrefix}$name"] = command
            }
        }
    }
}
