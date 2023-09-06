package kr.enak.luya.luyasupport.twitch.commands

import kr.enak.luya.luyasupport.twitch.abc.IncomingCommandDto

class CommandClipImpl(
    name: String,
    vararg aliases: String,
) : CommandMarkTimestampViaDiscordImpl(
    name,
    aliases = aliases
) {
    override fun execute(dto: IncomingCommandDto): String? = super.execute(
        IncomingCommandDto(
            // 📝
            dto.channel, dto.user, dto.message, listOf("\uD83D\uDCDD", "클립 생성 시도")
        )
    )
}