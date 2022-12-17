package kr.enak.luya.luyasupport.twitch.abc

import com.github.twitch4j.common.events.domain.EventChannel
import com.github.twitch4j.common.events.domain.EventUser

data class IncomingCommandDto(
    val channel: EventChannel,
    val user: EventUser,
    val message: String,
    val args: List<String> = extractArguments(message),
) {
    companion object {
        fun extractArguments(message: String): List<String> = message
                .split(" ")
                .drop(1)
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IncomingCommandDto) return false

        if (channel != other.channel) return false
        if (user != other.user) return false
        if (message != other.message) return false
        if (args != other.args) return false

        return true
    }

    override fun hashCode(): Int {
        var result = channel.hashCode()
        result = 31 * result + user.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + args.hashCode()
        return result
    }
}
