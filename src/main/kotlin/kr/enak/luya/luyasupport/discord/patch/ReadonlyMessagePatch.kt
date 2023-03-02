package kr.enak.luya.luyasupport.discord.patch

import club.minnced.discord.webhook.receive.ReadonlyAttachment
import club.minnced.discord.webhook.receive.ReadonlyEmbed
import club.minnced.discord.webhook.receive.ReadonlyMessage
import club.minnced.discord.webhook.receive.ReadonlyUser

class ReadonlyMessagePatch(
    id: Long,
    channelId: Long,
    mentionsEveryone: Boolean,
    tts: Boolean,
    flags: Int,
    author: ReadonlyUser,
    content: String,
    embeds: MutableList<ReadonlyEmbed>,
    attachments: MutableList<ReadonlyAttachment>,
    mentionedUsers: MutableList<ReadonlyUser>,
    mentionedRoles: MutableList<Long>,
) : ReadonlyMessage(
    id,
    channelId,
    mentionsEveryone,
    tts,
    flags,
    author,
    content,
    embeds,
    attachments,
    mentionedUsers,
    mentionedRoles
) {

}