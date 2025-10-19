package com.github.twitch4j.chatbot.kotlin

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.chatbot.kotlin.Blampbot.MINIMUM_MESSAGES_BETWEEN_BLAMPS
import com.github.twitch4j.chatbot.kotlin.Blampbot.channel
import com.github.twitch4j.chatbot.kotlin.Blampbot.lastMessage
import com.github.twitch4j.chatbot.kotlin.Blampbot.lastUser
import com.github.twitch4j.chatbot.kotlin.Blampbot.messagesSinceLastBlamp
import java.time.Instant

object BlampbotActions {
    fun bePraised(event: ChannelMessageEvent) {
        Messaging.green("\tTHEY LOVE ME!")
        event.twitchChat.sendMessage(channel, ":D")
    }

    fun beScolded(event: ChannelMessageEvent) {
        Messaging.info("THEY NO LIKE ME :(")
        event.twitchChat.sendMessage(channel, ":(")
    }

    fun beWhyd(event: ChannelMessageEvent) {
        Messaging.info("THEY NOT SURE ABOUT ME !?!!!?")
        event.twitchChat.sendMessage(channel, "O_o")
    }

    fun doHelloBackWithTurkey(event: ChannelMessageEvent) =
        with("you tryna BLAMP wit' me?") {
            Messaging.purple("\t$this")
            event.twitchChat.sendMessage(channel, this)
        }

    fun doJerbRequest(event: ChannelMessageEvent) {
        val request = event.message.substringAfter("blampbot, ")
        val response = AIHelper.request(request)
        event.twitchChat.sendMessage(channel, response)
    }

    fun doHelloBack(event: ChannelMessageEvent) {
        // val response = "Hey there, ${event.user.name}!"
        var response = AIHelper.helloBack(event.user.name)
        val maxRetryAttempts = 5
        var retryAttempt = 0
        // validate response
        while (!validateHelloBackResponse(event.user.name, response) && retryAttempt < maxRetryAttempts) {
            response = AIHelper.helloBack(event.user.name)
            retryAttempt++
        }

        Messaging.green("\tHELLO BACK - $response")
        event.twitchChat.sendMessage(channel, response)
    }

    fun validateHelloBackResponse(sender: String, response: String) =
        if (!response.contains(sender, ignoreCase = true)) {
            false
        } else {
            validateResponse(response)
        }

    fun validateResponse(response: String) =
        // TODO: actually validate the response
        true

    fun doWisdom(event: ChannelMessageEvent) {
        val wisdom = AIHelper.getWisdom()
        Messaging.cyan("\t$wisdom")
        event.twitchChat.sendMessage(channel, wisdom)
    }

    fun doRequest(event: ChannelMessageEvent) {
        val response = AIHelper.request(event.message.substringAfter("blampbot, "))
        Messaging.requestResponse(response)
        event.twitchChat.sendMessage(channel, response)
    }

    private fun getCallout(lowerCaseString: String) =
        Phrases.calloutWords.firstOrNull { lowerCaseString.contains(it) }

    fun doCallout(event: ChannelMessageEvent) {
        if (!Cooldowns.isCalloutInCooldown()) {
            getCallout(event.message.lowercase())?.let {
                val message = when (it) {
                    "durrrr key" -> "DOIKEY!"
                    "turkey",
                    "name something people take with them to the beach",
                    "the first thing you buy at a supermarket",
                    "a food often stuffed" -> "TOIKEY!"

                    "turkey tits" -> "TOIKEY TITS!"

                    // "doing good" -> "well dat's good, BLAMP"

                    else -> it.uppercase() + "!"
                }
                event.twitchChat.sendMessage(channel, message)
                Messaging.callout(message)
                Cooldowns.lastCalloutTime = Instant.now()
            }
        }
    }

    fun doBlamp(event: ChannelMessageEvent) {
        val blamped = BlampedMessage.fromString(
            original = event.message,
            fromLastBlampedUser = lastUser == event.user.name,
            notEnoughMessages = messagesSinceLastBlamp < MINIMUM_MESSAGES_BETWEEN_BLAMPS
        )

        blamped.printStatus(event)

        if (blamped.isValid) {
            Cooldowns.resetBlampCooldown()
            messagesSinceLastBlamp = 0
            lastMessage = blamped.blamped
            lastUser = event.user.name

            event.twitchChat.sendMessage(channel, blamped.blamped)
        }
    }

    fun doRetry(event: ChannelMessageEvent) =
        with(AIHelper.blampify(lastMessage)) {
            event.twitchChat.sendMessage("jerbtrundles", this)
            event.twitchChat.sendMessage(channel, this)
        }
}