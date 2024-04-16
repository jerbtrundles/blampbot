package com.github.twitch4j.chatbot.kotlin.features

import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.chatbot.kotlin.AIHelper
import com.github.twitch4j.chatbot.kotlin.Phrases
import com.github.twitch4j.chatbot.kotlin.Users
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

const val RESET = "\u001B[0m"
const val BLACK = "\u001B[30m"
const val RED = "\u001B[31m"
const val GREEN = "\u001B[32m"
const val YELLOW = "\u001B[33m"
const val BLUE = "\u001B[34m"
const val PURPLE = "\u001B[35m"
const val CYAN = "\u001B[36m"
const val WHITE = "\u001B[37m"

// Bold
const val BLACK_BOLD = "\u001b[1;30m" // BLACK
const val RED_BOLD = "\u001b[1;31m" // RED
const val GREEN_BOLD = "\u001b[1;32m" // GREEN
const val YELLOW_BOLD = "\u001b[1;33m" // YELLOW
const val BLUE_BOLD = "\u001b[1;34m" // BLUE
const val PURPLE_BOLD = "\u001b[1;35m" // PURPLE
const val CYAN_BOLD = "\u001b[1;36m" // CYAN
const val WHITE_BOLD = "\u001b[1;37m" // WHITE

class WriteChannelChatToConsole(eventHandler: SimpleEventHandler) {
    init {
        eventHandler.onEvent(ChannelMessageEvent::class.java, this::onChannelMessage)
    }

    val channel = "thecheeseball81"

    private val startInstantly = false
    private val startDelay = 150000L
    private val blampIntervalMinimum = 180000L
    private val blampIntervalMaximum = 480000L
    private val minimumMessageLength = 23
    private val maximumMessageLength = 100

    // Random.nextLong(180000, 480000)
    // Random.nextLong(blampIntervalMinimum, blampIntervalMaximum)
    private var blampCooldown = startDelay
    private var lastBlampTime = if (startInstantly) {
        Instant.now().minusMillis(blampCooldown)
    } else {
        Instant.now()
    }

    private var wisdomCooldown = 30000L
    private var lastWisdomTime = Instant.now().minusMillis(wisdomCooldown)

    private var calloutCooldown = 30000L
    private var lastCalloutTime = Instant.now().minusMillis(calloutCooldown)

    private var retryCooldown = 0L
    private var lastRetryTime = Instant.now().minusMillis(retryCooldown)

    private var requestCooldown = 30000L
    private var lastRequestTime = Instant.now().minusMillis(requestCooldown)

    private var lastMessage = ""
    private var lastUser = ""

    private fun timeSince(instant: Instant) = Duration.between(instant, Instant.now()).toMillis()
    private fun timeRemaining(lastTime: Instant, interval: Long) = interval - timeSince(lastTime)
    private fun isCooldownOver(lastTime: Instant, interval: Long) = timeRemaining(lastTime, interval) <= 0
    private fun inBlampCooldownPeriod() = !isCooldownOver(lastBlampTime, blampCooldown)
    private fun inRetryCooldownPeriod() = !isCooldownOver(lastRetryTime, retryCooldown)
    private fun isCalloutInCooldown() = !isCooldownOver(lastCalloutTime, calloutCooldown)
    private fun isWisdomInCooldown() = !isCooldownOver(lastWisdomTime, wisdomCooldown)
    private fun isRequestInCooldown() = !isCooldownOver(lastRequestTime, requestCooldown)

    /** Subscribe to the ChannelMessage Event and write the output to the console */
    private fun onChannelMessage(event: ChannelMessageEvent) {
        try {
            message("${event.channel.name} - ${event.user.name} - \"${event.message}\"")

            if (isValid(event)) {
                with(event.message.lowercase()) {
                    when {
                        this == "blampbot no" -> beScolded(event)
                        this == "blampbot yes" -> bePraised(event)
                        this == "blampbot why" -> beWhyd(event)
                        isJerbRequest(event) -> doJerbRequest(event)
                        isSomeoneSayingHelloAndTurkey(event) -> doHelloBackWithTurkey(event)
                        isSomeoneSayingHello(event) -> doHelloBack(event)
                        canRequest(event) -> doRequest(event)
                        canWisdom(event) -> doWisdom(event)
                        containsCallout(this) -> doCallout(event)
                        canRetry(event) -> doRetry(event)
                        canBlamp(event) -> doBlamp(event)
                        else -> {}
                    }
                }
            }
        } catch (e: Exception) {
            println("caught an exception")
            println(e.stackTraceToString())
        }
    }

    private fun isSomeoneSayingHelloAndTurkey(event: ChannelMessageEvent) =
        isSomeoneSayingHello(event) && event.message.contains("turkey")

    private fun doHelloBackWithTurkey(event: ChannelMessageEvent) =
        with("you tryna BLAMP wit' me?") {
            purple("\t$this")
            event.twitchChat.sendMessage(channel, this)
        }


    private fun isJerbRequest(event: ChannelMessageEvent) =
        event.user.name.lowercase() == "jerbtrundles"
                && event.message.startsWith("blampbot, ")

    private fun doJerbRequest(event: ChannelMessageEvent) {
        val request = event.message.substringAfter("blampbot, ")
        val response = AIHelper.request(request)
        event.twitchChat.sendMessage(channel, response)
    }

    private fun isSomeoneSayingHello(event: ChannelMessageEvent) =
        with(event.message.lowercase()) {
            (contains("hi, blampbot")
                    || contains("hello, blampbot")
                    || contains("hi blampbot")
                    || (startsWith("hey") && contains("blampbot")))
        }

    private fun doHelloBack(event: ChannelMessageEvent) {
        val response = "Hey there, ${event.user.name}!"
        green("\tHELLO BACK - $response")
        event.twitchChat.sendMessage(channel, response)
    }

    private fun doWisdom(event: ChannelMessageEvent) {
        val wisdom = AIHelper.getWisdom()
        cyan("\t$wisdom")
        event.twitchChat.sendMessage(channel, wisdom)
    }

    private fun bePraised(event: ChannelMessageEvent) {
        info("\tTHEY LOVE ME!")
        event.twitchChat.sendMessage(channel, ":D")
    }

    private fun beScolded(event: ChannelMessageEvent) {
        info("\tTHEY NO LIKE ME :(")
        event.twitchChat.sendMessage(channel, ":(")
    }

    private fun beWhyd(event: ChannelMessageEvent) {
        info("\tTHEY NOT SURE ABOUT ME !?!!!?")
        event.twitchChat.sendMessage(channel, "O_o")
    }

    private fun yellow(str: String) = println("$YELLOW$str")
    private fun red(str: String) = println("$RED$str")
    private fun green(str: String) = println("$GREEN$str")
    private fun purple(str: String) = println("$PURPLE$str")
    private fun blue(str: String) = println("$BLUE$str")
    private fun cyan(str: String) = println("$CYAN$str")
    private fun reset(str: String) = println("$RESET$str")
    private fun error(str: String) = red("\tERROR: $str")
    private fun info(str: String) = yellow("\t$str")
    private fun callout(str: String) = blue("\tCALLOUT: $str")
    private fun requestResponse(str: String) = cyan("\t$str")
    private fun message(str: String) = reset(str)

    private fun canBlamp(event: ChannelMessageEvent) =
        if (inBlampCooldownPeriod()) {
            info("BLAMP COOLDOWN: ${timeRemaining(lastBlampTime, blampCooldown) / 1000.0} seconds to go.")
            false
        } else if (event.user.name == lastUser && lastUser != "jerbtrundles") {
            error("NO BLAMPING SAME PERSON TWICE.")
            false
        } else if (event.message.length < minimumMessageLength) {
            error("MESSAGE TOO SHORT - ${event.message.length} (minimum: $minimumMessageLength)")
            false
        } else if (event.message.length > maximumMessageLength) {
            error("MESSAGE TOO LONG - ${event.message.length} (maximum: $maximumMessageLength)")
            false
        } else {
            true
        }

    private fun canRequest(event: ChannelMessageEvent) =
        if (!event.message.startsWith("blampbot, ")) {
            false
        } else if (isRequestInCooldown()) {
            info(
                "REQUEST COOLDOWN: ${
                    timeRemaining(
                        lastRequestTime,
                        requestCooldown
                    ) / 1000.0
                } seconds to go."
            )
            false
        } else {
            true
        }

    private fun doRequest(event: ChannelMessageEvent) {
        val response = AIHelper.request(event.message.substringAfter("blampbot, "))
        requestResponse(response)
        event.twitchChat.sendMessage(channel, response)
    }

    private fun canWisdom(event: ChannelMessageEvent) =
        if (!event.message.startsWith("blampbot wisdom")) {
            false
        } else if (isWisdomInCooldown()) {
            info(
                "WISDOM COOLDOWN: ${
                    timeRemaining(
                        lastWisdomTime,
                        wisdomCooldown
                    ) / 1000.0
                } seconds to go."
            )
            false
        } else {
            true
        }

    private fun containsCallout(lowerCaseString: String) =
        Phrases.calloutWords.any { lowerCaseString.contains(it) }

    private fun getCallout(lowerCaseString: String) =
        Phrases.calloutWords.firstOrNull { lowerCaseString.contains(it) }


    private fun doCallout(event: ChannelMessageEvent) {
        if (!isCalloutInCooldown()) {
            getCallout(event.message.lowercase())?.let {
                val message = when (it) {
                    "durrrr key" -> "DOIKEY!"
                    "turkey",
                    "name something people take with them to the beach",
                    "the first thing you buy at a supermarket",
                    "a food often stuffed" -> "TOIKEY!"

                    else -> it.uppercase() + "!"
                }
                event.twitchChat.sendMessage(channel, message)
                callout(message)
                lastCalloutTime = Instant.now()
            }
        }
    }

    val forbiddenWords = arrayOf(
        "moist"
    )

    fun doBlamp(event: ChannelMessageEvent) {
        val blamped = AIHelper.blampify(event.message)

        if (blamped.isEmpty()) {
            error("BLAMPED MESSAGE IS EMPTY")
        } else if (blamped.lowercase() == event.message.lowercase()) {
            error("BLAMPED MESSAGE SAME AS ORIGINAL MESSAGE")
        } else if (forbiddenWords.any { blamped.lowercase().contains(it) }) {
            error("BLAMPED MESSAGE CONTAINS FORBIDDEN WORD")
        } else if (!blamped.lowercase().contains("blamp")) {
            error("BLAMPED MESSAGE DOESN'T CONTAIN BLAMP")
        } else {
            green(
                "<-- *** -->\n" +
                        "\tBLAMPIFY\n" +
                        "\t${event.message} -> $blamped\n" +
                        "\t<-- *** -->"
            )

            event.twitchChat.sendMessage(channel, blamped)
            lastBlampTime = Instant.now()
            blampCooldown = Random.nextLong(from = blampIntervalMinimum, until = blampIntervalMaximum)
            lastMessage = event.message
            lastUser = event.user.name
        }
    }

//    ye olde blampify; simple random token replacement
//    fun blampify(str: String): String {
//        val words = str.split(" ").toMutableList()
//        val replacementCount = (str.length / wordsPerBlamp).coerceAtLeast(1)
//
//        repeat(replacementCount) {
//            val i = Random.nextInt(from = 0, until = words.size - 1)
//            words[i] = "BLAMP"
//        }
//
//        return words.joinToString(separator = " ")
//    }

    fun canRetry(event: ChannelMessageEvent) =
        event.message.startsWith("try again, blampbot")
                && !inRetryCooldownPeriod()

    fun doRetry(event: ChannelMessageEvent) =
        with(AIHelper.blampify(lastMessage)) {
            event.twitchChat.sendMessage("jerbtrundles", this)
            event.twitchChat.sendMessage(channel, this)
        }


    fun isValid(event: ChannelMessageEvent) =
        if (!messageContainsSpace(event) && !messageContainsCallout(event)) {
            error("MESSAGE MUST CONTAIN AT LEAST ONE SPACE")
            false
        } else if (messageContainsExcludedPhrase(event)) {
            error("CONTAINS EXCLUDED PHRASING")
            false
        } else if (messageFromExcludedUser(event)) {
            error("MESSAGE IS FROM EXCLUDED USER - ${event.user.name}")
            false
        } else {
            true
        }

    private fun messageContainsSpace(event: ChannelMessageEvent) =
        event.message.contains(" ")

    private fun messageContainsExcludedPhrase(event: ChannelMessageEvent) =
        Phrases.excluded.any { event.message.lowercase().contains(it.lowercase()) }

    private fun messageFromExcludedUser(event: ChannelMessageEvent) =
        Users.isExcluded(event.user.name.lowercase())

    private fun messageContainsCallout(event: ChannelMessageEvent) =
        Phrases.containsCallout(event.message.lowercase())
}

// AIzaSyBxx_AOmE8gsgdYERBM6bpYdjtsU_T6qwY
// curl \
//  -H 'Content-Type: application/json' \
//  -d '{"contents":[{"parts":[{"text":"Write a story about a magic backpack"}]}]}' \
//  -X POST https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=AIzaSyBxx_AOmE8gsgdYERBM6bpYdjtsU_T6qwY