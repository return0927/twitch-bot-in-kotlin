package kr.enak.luya.luyasupport.discord.patch

import club.minnced.discord.webhook.IOUtil
import club.minnced.discord.webhook.IOUtil.OctetBody
import club.minnced.discord.webhook.send.AllowedMentions
import club.minnced.discord.webhook.send.MessageAttachment
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookMessage
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

class DiscordWebhookMessagePatch(
    username: String?,
    avatarUrl: String?,
    content: String?,
    embeds: MutableList<WebhookEmbed>?,
    isTTS: Boolean,
    files: Array<out MessageAttachment>?,
    allowedMentions: AllowedMentions?,
    flags: Int,
    var threadName: String = "",
) : WebhookMessage(username, avatarUrl, content, embeds, isTTS, files, allowedMentions, flags) {
    companion object {
        fun embeds(first: WebhookEmbed): DiscordWebhookMessagePatch {
            val list: MutableList<WebhookEmbed> = ArrayList(1)
            list.add(first)
            return DiscordWebhookMessagePatch(
                null,
                null,
                null,
                list,
                false,
                null,
                AllowedMentions.all(),
                0
            )
        }
    }

    override fun getBody(): RequestBody {
        val payload = JSONObject()
        payload.put("content", content)
        if (embeds != null && !embeds.isEmpty()) {
            val array = JSONArray()
            for (embed in embeds) {
                array.put(embed.reduced())
            }
            payload.put("embeds", array)
        } else {
            payload.put("embeds", JSONArray())
        }
        if (avatarUrl != null) payload.put("avatar_url", avatarUrl)
        if (username != null) payload.put("username", username)
        payload.put("tts", isTTS)
        payload.put("allowed_mentions", allowedMentions)
        payload.put("flags", flags)

        if (threadName.isNotEmpty())
            payload.put("thread_name", threadName)

        val json = payload.toString()
        if (isFile) {
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            for (i in attachments.indices) {
                val attachment = attachments[i] ?: break
                builder.addFormDataPart("file$i", attachment.name, OctetBody(attachment.data))
            }
            return builder.addFormDataPart("payload_json", json).build()
        }
        return RequestBody.create(IOUtil.JSON, json)
    }
}