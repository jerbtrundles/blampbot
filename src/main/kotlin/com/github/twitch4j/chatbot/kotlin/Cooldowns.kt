package com.github.twitch4j.chatbot.kotlin

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.chatbot.kotlin.Blampbot.BLAMP_INTERVAL_MAXIMUM
import com.github.twitch4j.chatbot.kotlin.Blampbot.BLAMP_INTERVAL_MINIMUM
import com.github.twitch4j.chatbot.kotlin.Blampbot.MINIMUM_MESSAGES_BETWEEN_BLAMPS
import com.github.twitch4j.chatbot.kotlin.Blampbot.START_DELAY
import com.github.twitch4j.chatbot.kotlin.Blampbot.START_INSTANTLY
import com.github.twitch4j.chatbot.kotlin.Blampbot.lastUser
import com.github.twitch4j.chatbot.kotlin.Blampbot.messagesSinceLastBlamp
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

object Cooldowns {
    var blampCooldown = START_DELAY
    internal const val WISDOM_COOLDOWN = 30000L
    internal const val CALLOUT_COOLDOWN = 30000L
    internal const val RETRY_COOLDOWN = 0L
    internal const val REQUEST_COOLDOWN = 30000L

    var lastBlampTime =
        if (START_INSTANTLY) {
            Instant.now().minusMillis(blampCooldown)
        } else {
            Instant.now()
        }

    private val blampCooldownSeconds
        get() = timeRemaining(lastBlampTime, blampCooldown) / 1000.0
    private val wisdomCooldownSeconds
        get() = timeRemaining(lastWisdomTime, WISDOM_COOLDOWN) / 1000.0
    private val retryCooldownSeconds
        get() = timeRemaining(lastRetryTime, RETRY_COOLDOWN) / 1000.0
    private val calloutCooldownSeconds
        get() = timeRemaining(lastCalloutTime, CALLOUT_COOLDOWN) / 1000.0
    private val requestCooldownSeconds
        get() = timeRemaining(lastRequestTime, REQUEST_COOLDOWN) / 1000.0


    private var lastWisdomTime = Instant.now().minusMillis(WISDOM_COOLDOWN)
    var lastCalloutTime = Instant.now().minusMillis(CALLOUT_COOLDOWN)
    private var lastRetryTime = Instant.now().minusMillis(RETRY_COOLDOWN)
    private var lastRequestTime = Instant.now().minusMillis(REQUEST_COOLDOWN)

    private fun timeSince(instant: Instant) =
        Duration.between(instant, Instant.now()).toMillis()

    fun timeRemaining(lastTime: Instant, interval: Long) =
        interval - timeSince(lastTime)

    private fun isCooldownOver(lastTime: Instant, interval: Long) =
        timeRemaining(lastTime, interval) <= 0

    fun inBlampCooldownPeriod() = !isCooldownOver(lastBlampTime, blampCooldown)
    fun inRetryCooldownPeriod() = !isCooldownOver(lastRetryTime, RETRY_COOLDOWN)
    fun isCalloutInCooldown() = !isCooldownOver(lastCalloutTime, CALLOUT_COOLDOWN)
    fun isWisdomInCooldown() = !isCooldownOver(lastWisdomTime, WISDOM_COOLDOWN)
    fun isRequestInCooldown() = !isCooldownOver(lastRequestTime, REQUEST_COOLDOWN)

    fun printBlampCooldownMessage() =
        if (blampCooldownSeconds <= 0) {
            Messaging.green("[BLAMP]", addNewLine = false)
        } else {
            Messaging.yellowBold("[BLAMP (${blampCooldownSeconds}s)]", addNewLine = false)
        }

    fun printWisdomCooldownMessage() =
        if (wisdomCooldownSeconds <= 0) {
            Messaging.green(" [WISDOM]", addNewLine = false)
        } else {
            Messaging.yellowBold(" [WISDOM (${wisdomCooldownSeconds}s)]", addNewLine = false)
        }

    fun printRequestCooldownMessage() =
        if (requestCooldownSeconds <= 0) {
            Messaging.green(" [REQUEST]", addNewLine = false)
        } else {
            Messaging.yellowBold(" [REQUEST (${requestCooldownSeconds}s)]", addNewLine = false)
        }

    fun printRetryCooldownMessage() =
        if (retryCooldownSeconds <= 0) {
            Messaging.green(" [RETRY]", addNewLine = false)
        } else {
            Messaging.yellowBold(" [RETRY (${retryCooldownSeconds}s)]", addNewLine = false)
        }

    fun printCalloutCooldownMessage() =
        if (calloutCooldownSeconds <= 0) {
            Messaging.green(" [CALLOUT]")
        } else {
            Messaging.yellowBold(" [CALLOUT (${calloutCooldownSeconds}s)]")
        }

    fun canBlamp(event: ChannelMessageEvent) =
        when {
            inBlampCooldownPeriod() -> false
            event.user.name == lastUser && lastUser != "jerbtrundles" -> false
            messagesSinceLastBlamp < MINIMUM_MESSAGES_BETWEEN_BLAMPS -> false
            else -> true
        }

    fun canRequest(event: ChannelMessageEvent) =
        when {
            !event.message.startsWith("blampbot, ") -> false
            isRequestInCooldown() -> false
            else -> true
        }

    fun canWisdom(event: ChannelMessageEvent) =
        when {
            !event.message.startsWith("blampbot wisdom") -> false
            isWisdomInCooldown() -> false
            else -> true
        }

    fun canRetry(event: ChannelMessageEvent) =
        when {
            !event.message.startsWith("try again, blampbot") -> false
            inRetryCooldownPeriod() -> false
            else -> true
        }

    fun resetBlampCooldown() {
        lastBlampTime = Instant.now()
        blampCooldown = Random.nextLong(from = BLAMP_INTERVAL_MINIMUM, until = BLAMP_INTERVAL_MAXIMUM)
    }

    fun printCooldownMessages() {
        Messaging.info("COOLDOWNS: ", addNewLine = false)
        printBlampCooldownMessage()
        printWisdomCooldownMessage()
        printRequestCooldownMessage()
        printRetryCooldownMessage()
        printCalloutCooldownMessage()
    }
}