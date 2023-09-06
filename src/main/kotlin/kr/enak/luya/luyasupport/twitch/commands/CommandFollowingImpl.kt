package kr.enak.luya.luyasupport.twitch.commands

import com.github.twitch4j.helix.domain.Follow
import kr.enak.luya.luyasupport.twitch.TwitchService
import kr.enak.luya.luyasupport.twitch.abc.AbstractPrefixCommand
import kr.enak.luya.luyasupport.twitch.abc.IncomingCommandDto
import kr.enak.luya.luyasupport.twitch.token
import kr.enak.luya.luyasupport.twitch.utils.format
import kr.enak.luya.luyasupport.twitch.utils.utcNow
import java.time.Duration

class CommandFollowingImpl(
    name: String,
    vararg aliases: String
) : AbstractPrefixCommand(name, aliases) {

    private fun getFollowsDate(userId: String, channelId: String): Follow? {
        TwitchService.client.helix.apply {
            val follows = getFollowers(token(), userId, channelId, null, 100)
                .execute()

            return follows.follows.firstOrNull()
        }
    }

    override suspend fun execute(dto: IncomingCommandDto): String {
        val following = getFollowsDate(dto.user.id, dto.channel.id)
            ?: return "아직 팔로우하고 있지 않네요..."

        val duration = Duration.between(following.followedAtInstant, utcNow())
        return "팔로우한지 ${duration.format()}"
    }
}
