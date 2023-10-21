package kr.enak.luya.luyasupport.twitch.commands

import com.github.twitch4j.helix.domain.InboundFollow
import kr.enak.luya.luyasupport.twitch.TwitchService
import kr.enak.luya.luyasupport.twitch.abc.AbstractPrefixCommand
import kr.enak.luya.luyasupport.twitch.abc.IncomingCommandDto
import kr.enak.luya.luyasupport.twitch.token
import kr.enak.luya.luyasupport.twitch.utils.format
import kr.enak.luya.luyasupport.twitch.utils.utcNow
import java.time.Duration
import java.util.concurrent.TimeUnit

class CommandFollowingImpl(
    name: String,
    vararg aliases: String
) : AbstractPrefixCommand(name, aliases) {

    class NotFollowingError : RuntimeException()

    class ResponseTimeoutError : RuntimeException()

    private fun getFollowsDate(userId: String, channelId: String): InboundFollow {
        TwitchService.client.helix.apply {
            val followsReq = getChannelFollowers(token(), channelId, userId, 1, null)
                .queue()

            val follows = try {
                followsReq.get(1L, TimeUnit.SECONDS)
            } catch (e: Throwable) {
                throw ResponseTimeoutError()
            }

            return follows.follows?.firstOrNull() ?: throw NotFollowingError()
        }
    }

    override suspend fun execute(dto: IncomingCommandDto): String {
        val following = try {
            getFollowsDate(dto.user.id, dto.channel.id)
        } catch (e: NotFollowingError) {
            return "아직 팔로우하고 있지 않네요..."
        } catch (e: ResponseTimeoutError) {
            return "서버에서 정보를 받아오지 못했어요."
        }

        val duration = Duration.between(following.followedAt, utcNow())
        return "팔로우한지 ${duration.format()}"
    }
}
