package com.github.twitch4j.chatbot.kotlin

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent

object Blampbot {
    val channel = "thecheeseball81"

    const val START_INSTANTLY = true
    const val START_DELAY = 150000L

    internal const val BLAMP_INTERVAL_MINIMUM = 210000L
    internal const val BLAMP_INTERVAL_MAXIMUM = 420000L
    internal const val MINIMUM_MESSAGE_LENGTH = 23
    internal const val MAXIMUM_MESSAGE_LENGTH = 100
    internal const val MINIMUM_MESSAGES_BETWEEN_BLAMPS = 20
    internal const val BLAMPED_STRING_MISMATCH_LOWER_THRESHOLD = 70

    internal var messagesSinceLastBlamp = MINIMUM_MESSAGES_BETWEEN_BLAMPS

    var lastMessage = ""
    var lastUser = ""

    internal fun processEvent(event: ChannelMessageEvent) {
        Messaging.message("${event.channel.name} - ${event.user.name} - \"${event.message}\"")
        Cooldowns.printCooldownMessages()
        messagesSinceLastBlamp++

        val processedEvent = ProcessedEvent.fromEvent(
            event = event,
            lastBlampedUser = lastUser,
            lastMessage = lastMessage,
            blampCooldown = Cooldowns.blampCooldown,
            wisdomCooldown = Cooldowns.WISDOM_COOLDOWN,
            calloutCooldown = Cooldowns.CALLOUT_COOLDOWN,
            retryCooldown = Cooldowns.RETRY_COOLDOWN,
            requestCooldown = Cooldowns.REQUEST_COOLDOWN,
        )

        processedEvent.printStatus()

        with(processedEvent) {
            when {
                isJerbRequest -> BlampbotActions.doJerbRequest(event)
                isValid -> {
                    when {
                        // todo: double check that containsCallout works after being moved inside when
                        containsCallout -> BlampbotActions.doCallout(event)

                        isBlampbotNo -> BlampbotActions.beScolded(event)
                        isBlampbotYes -> BlampbotActions.bePraised(event)
                        isBlampbotWhy -> BlampbotActions.beWhyd(event)
                        isSomeoneSayingHelloAndTurkey -> BlampbotActions.doHelloBackWithTurkey(event)
                        isSomeoneSayingHello -> BlampbotActions.doHelloBack(event)
                        Cooldowns.canRequest(event) -> BlampbotActions.doRequest(event)
                        Cooldowns.canWisdom(event) -> BlampbotActions.doWisdom(event)
                        Cooldowns.canRetry(event) -> BlampbotActions.doRetry(event)
                        else -> {
                            Messaging.brightCyan("\tMessage is blampifiable!")
                            if (Cooldowns.canBlamp(event)) {
                                BlampbotActions.doBlamp(event)
                            } else {
                                Messaging.info("No action taken.")
                            }
                        }
                    }
                }

                else -> {
                    Messaging.info("No action taken.")
                }
            }
        }
    }
}
