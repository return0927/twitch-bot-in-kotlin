package kr.enak.luya.luyasupport.twitch.abc

import org.springframework.stereotype.Component

annotation class Command(
val name: String,
vararg val aliases: String,
)
