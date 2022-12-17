package kr.enak.luya.luyasupport.twitch

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.philippheuer.events4j.core.EventManager
import com.github.philippheuer.events4j.reactor.ReactorEventHandler
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.chat.TwitchChat
import com.github.twitch4j.helix.TwitchHelix
import kr.enak.luya.luyasupport.twitch.config.TwitchBotConfiguration
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct


var token: String = ""

@Service
class TwitchService(
    private val twitchConfig: TwitchBotConfiguration,
) {
    init {
        token = twitchConfig.authToken
    }
    companion object {
        lateinit var client: TwitchClient
    }
    val eventManager: EventManager
        get() = client.eventManager

    val chat: TwitchChat
        get() = client.chat

    @PostConstruct
    fun init() {
        initClient()
        initEventManager()
    }

    private fun initEventManager() {
        eventManager.autoDiscovery()
    }

    private fun initClient(): TwitchClient {
        val builder = TwitchClientBuilder.builder()
                .withChatAccount(OAuth2Credential("twitch", twitchConfig.authToken))
                .withDefaultEventHandler(ReactorEventHandler::class.java)
                .withEnableHelix(true)
                .withEnableChat(true)

        client = builder.build()
        return client
    }
}

fun TwitchHelix.token() = kr.enak.luya.luyasupport.twitch.token
