package kr.enak.luya.luyasupport.discord.patch

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import kr.enak.luya.luyasupport.twitch.abc.IncomingCommandDto

fun ChannelMessageEvent.toCommandDto() = IncomingCommandDto(
    channel, user, message
)
