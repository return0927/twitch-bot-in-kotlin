package kr.enak.luya.luyasupport.discord.patch

import club.minnced.discord.webhook.IOUtil
import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.AllowedMentions
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.concurrent.CompletableFuture

fun WebhookClient.send(
    content: String,
    username: String? = null,
    avatarUrl: String? = null,
): CompletableFuture<ReadonlyMessagePatch> {
    val execute = WebhookClient::class.java.getDeclaredMethod(
        "execute", RequestBody::class.java
    ).also { it.isAccessible = true }

    val allowedMentions: AllowedMentions = WebhookClient::class.java.getDeclaredField("allowedMentions")
        .also { it.isAccessible = true }
        .get(this) as AllowedMentions

    return execute.invoke(this, RequestBody.create(IOUtil.JSON, JSONObject().apply {
        put("allowed_mentions", allowedMentions)
        put("content", content)
        if (username != null) put("username", username)
        if (avatarUrl != null) put("avatar_url", avatarUrl)
    }.toString())) as CompletableFuture<ReadonlyMessagePatch>
}
