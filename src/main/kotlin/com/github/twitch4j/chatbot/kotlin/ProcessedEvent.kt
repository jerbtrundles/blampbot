package com.github.twitch4j.chatbot.kotlin

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.chatbot.kotlin.Blampbot.MAXIMUM_MESSAGE_LENGTH
import com.github.twitch4j.chatbot.kotlin.Blampbot.MINIMUM_MESSAGE_LENGTH

class ProcessedEvent private constructor(
    val event: ChannelMessageEvent,
    val calloutWordsList: List<String>,
    val emojiList: List<String>,
    val forbiddenWordsList: List<String>,
    val userIsExcluded: Boolean,
    val containsSpace: Boolean,
    val isSomeoneSayingHello: Boolean,
    val isJerbRequest: Boolean,
    val isSomeoneSayingHelloAndTurkey: Boolean,
    val isBlampbotNo: Boolean,
    val isBlampbotYes: Boolean,
    val isBlampbotWhy: Boolean,
    val isFromLastBlampedUser: Boolean,
    val blampCooldown: Long,
    val wisdomCooldown: Long,
    val calloutCooldown: Long,
    val retryCooldown: Long,
    val requestCooldown: Long,
) {
    val containsCallout = calloutWordsList.isNotEmpty()
    val containsEmoji = emojiList.isNotEmpty()
    val containsForbiddenPhrase = forbiddenWordsList.isNotEmpty()
    val messageTooSmall = event.message.length < Blampbot.MINIMUM_MESSAGE_LENGTH
    val messageTooLarge = event.message.length > Blampbot.MAXIMUM_MESSAGE_LENGTH

    val isValid: Boolean = when {
        isBlampbotYes || isBlampbotNo || isBlampbotWhy -> true
        isSomeoneSayingHello -> true
        containsCallout -> true
        !containsSpace && !containsCallout -> false
        userIsExcluded -> false
        messageTooSmall || messageTooLarge -> false
        containsEmoji -> false
        containsForbiddenPhrase -> false
        else -> true
    }

    fun printStatus() {
        if (!containsSpace && !containsCallout) {
            Messaging.error("MESSAGE MUST CONTAIN AT LEAST ONE SPACE")
        }

        if (userIsExcluded) {
            Messaging.error("MESSAGE IS FROM EXCLUDED USER - ", addNewLine = false)
            Messaging.purple(event.user.name)
        }

        if (messageTooSmall) {
            Messaging.error("MESSAGE TOO SHORT - ${event.message.length} (minimum: $MINIMUM_MESSAGE_LENGTH)")
        }

        if (messageTooLarge) {
            Messaging.error("MESSAGE TOO LONG - ${event.message.length} (maximum: $MAXIMUM_MESSAGE_LENGTH)")
        }

        if (forbiddenWordsList.isNotEmpty()) {
            Messaging.error("MESSAGE CONTAINS FORBIDDEN PHRASING - ", addNewLine = false)
            forbiddenWordsList.forEach {
                Messaging.purple("$it ", addNewLine = false)
            }
            Messaging.newLine()
        }

        if (calloutWordsList.isNotEmpty()) {
            Messaging.error("MESSAGE CONTAINS CALLOUT - ", addNewLine = false)
            calloutWordsList.forEach {
                Messaging.green("$it ", addNewLine = false)
            }
            Messaging.newLine()
        }

        if (emojiList.isNotEmpty()) {
            Messaging.error("MESSAGE CONTAINS EMOJI - ", addNewLine = false)
            emojiList.forEach {
                Messaging.purple("$it ", addNewLine = false)
            }
            Messaging.newLine()
        }
    }

    companion object {
        internal fun fromEvent(
            event: ChannelMessageEvent,
            lastBlampedUser: String,
            lastMessage: String,
            blampCooldown: Long,
            wisdomCooldown: Long,
            calloutCooldown: Long,
            retryCooldown: Long,
            requestCooldown: Long,
        ): ProcessedEvent {
            val message = event.message.lowercase()

            return ProcessedEvent(
                event = event,
                calloutWordsList = Phrases.getCalloutWordsList(message),
                emojiList = Phrases.getEmojis(event.message),
                forbiddenWordsList = Phrases.getForbiddenWordsList(message),
                userIsExcluded = Users.isExcluded(event.user.name.lowercase()),
                containsSpace = event.message.contains(" "),
                isSomeoneSayingHello = isSomeoneSayingHello(event),
                isJerbRequest = isJerbRequest(event),
                isSomeoneSayingHelloAndTurkey = isSomeoneSayingHelloAndTurkey(event),
                isBlampbotYes = message == "blampbot yes",
                isBlampbotNo = message == "blampbot no",
                isBlampbotWhy = message == "blampbot why",
                isFromLastBlampedUser = event.user.name == lastBlampedUser,
                blampCooldown = blampCooldown,
                wisdomCooldown = wisdomCooldown,
                calloutCooldown = calloutCooldown,
                retryCooldown = retryCooldown,
                requestCooldown = requestCooldown,
            )
        }

        private fun isSomeoneSayingHello(event: ChannelMessageEvent) =
            with(event.message.lowercase()) {
                (contains("hi, blampbot")
                        || contains("hello, blampbot")
                        || contains("hi blampbot")
                        || (startsWith("hey") && contains("blampbot")))
            }

        private fun isJerbRequest(event: ChannelMessageEvent) =
            event.user.name.lowercase() == "jerbtrundles"
                    && event.message.startsWith("blampbot, ")

        private fun isSomeoneSayingHelloAndTurkey(event: ChannelMessageEvent) =
            isSomeoneSayingHello(event) && event.message.contains("turkey")
    }
}