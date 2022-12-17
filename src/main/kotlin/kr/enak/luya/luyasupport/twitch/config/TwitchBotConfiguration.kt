package kr.enak.luya.luyasupport.twitch.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "bot.twitch")
data class TwitchBotConfiguration(
    val username: String,
    val authToken: String,
    val channels: List<String> = listOf("eunhaklee", "jumiluya"),
    val commandPrefix: String = "!",
)
