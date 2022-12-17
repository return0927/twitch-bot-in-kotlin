package kr.enak.luya.luyasupport.twitch.abc

import com.github.twitch4j.common.events.domain.EventChannel
import com.github.twitch4j.common.events.domain.EventUser

interface ICommand {
    fun execute(dto: IncomingCommandDto): String?
}