package kr.enak.luya.luyasupport.discord.patch

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookMessage
import java.util.concurrent.CompletableFuture

fun WebhookClient.send(message: WebhookMessage): CompletableFuture<ReadonlyMessagePatch> {
    TODO()
}
