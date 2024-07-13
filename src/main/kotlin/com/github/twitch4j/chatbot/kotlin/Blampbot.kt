package com.github.twitch4j.chatbot.kotlin

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

object Blampbot {
    private val channel = "thecheeseball81"

    private val startInstantly = false
    private val startDelay = 30000L
    private val blampIntervalMinimum = 180000L
    private val blampIntervalMaximum = 480000L
    private val minimumMessageLength = 23
    private val maximumMessageLength = 100

    // Random.nextLong(180000, 480000)
    // Random.nextLong(blampIntervalMinimum, blampIntervalMaximum)
    private var blampCooldown = startDelay
    private var lastBlampTime =
        if (startInstantly) {
            Instant.now().minusMillis(blampCooldown)
        } else {
            Instant.now()
        }

    private val blampCooldownSeconds
        get() = timeRemaining(lastBlampTime, blampCooldown) / 1000.0

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

    private fun isBlampbotNo(message: String) = message == "blampbot no"
    private fun isBlampbotYes(message: String) = message == "blampbot yes"
    private fun isBlampbotWhy(message: String) = message == "blampbot why"

    private fun displayBlampStatus() =
        if (blampCooldownSeconds <= 0) {
            green("\tBLAMP READY!")
        } else {
            info("BLAMP COOLDOWN: $blampCooldownSeconds seconds to go.")
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
        green("\tTHEY LOVE ME!")
        event.twitchChat.sendMessage(channel, ":D")
    }

    private fun beScolded(event: ChannelMessageEvent) {
        info("THEY NO LIKE ME :(")
        event.twitchChat.sendMessage(channel, ":(")
    }

    private fun beWhyd(event: ChannelMessageEvent) {
        info("THEY NOT SURE ABOUT ME !?!!!?")
        event.twitchChat.sendMessage(channel, "O_o")
    }

    private val commonEnding = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"

    private fun rainbow(str: String, addNewLine: Boolean = true) {
        val colors = arrayOf(
            ::red,
            ::yellow,
            ::green,
            ::blue,
            ::purple
        )
        str.forEachIndexed { index, c ->
            val colorFunction = colors[index % colors.size]
            colorFunction(c.toString(), false)
        }

        if(addNewLine) {
            println()
        }
    }

    private fun yellow(str: String, addNewLine: Boolean = true) =
        if (addNewLine) {
            println("${ConsoleColors.YELLOW}$str$commonEnding")
        } else {
            print("${ConsoleColors.YELLOW}$str")
        }

    private fun red(str: String, addNewLine: Boolean = true) =
        if (addNewLine) {
            println("${ConsoleColors.RED}$str$commonEnding")
        } else {
            print("${ConsoleColors.RED}$str")
        }

    private fun yellowBold(str: String, addNewLine: Boolean = true) =
        if (addNewLine) {
            println("${ConsoleColors.YELLOW_BOLD}$str$commonEnding")
        } else {
            print("${ConsoleColors.YELLOW_BOLD}$str")
        }

    private fun brightCyan(str: String, addNewLine: Boolean = true) =
        if (addNewLine) {
            println("${ConsoleColors.CYAN_BRIGHT}$str$commonEnding")
        } else {
            print("${ConsoleColors.CYAN_BRIGHT}$str")
        }

    private fun brightGreen(str: String, addNewLine: Boolean = true) =
        if (addNewLine) {
            println("${ConsoleColors.GREEN_BRIGHT}$str$commonEnding")
        } else {
            print("${ConsoleColors.GREEN_BRIGHT}$str")
        }

    private fun green(str: String, addNewLine: Boolean = true) =
        if (addNewLine) {
            println("${ConsoleColors.GREEN}$str$commonEnding")
        } else {
            print("${ConsoleColors.GREEN}$str")
        }

    private fun purple(str: String, addNewLine: Boolean = true) =
        if (addNewLine) {
            println("${ConsoleColors.PURPLE}$str$commonEnding")
        } else {
            print("${ConsoleColors.PURPLE}$str")
        }

    private fun blue(str: String, addNewLine: Boolean = true) =
        if (addNewLine) {
            println("${ConsoleColors.BLUE}$str$commonEnding")
        } else {
            print("${ConsoleColors.BLUE}$str")
        }

    private fun cyan(str: String, addNewLine: Boolean = true) =
        if (addNewLine) {
            println("${ConsoleColors.CYAN}$str$commonEnding")
        } else {
            print("${ConsoleColors.CYAN}$str")
        }

    private fun reset(str: String, addNewLine: Boolean = true) =
        if (addNewLine) {
            println("${ConsoleColors.RESET}$str$commonEnding")
        } else {
            print("${ConsoleColors.RESET}$str")
        }

    private fun error(str: String, addNewLine: Boolean = true) = red("\tERROR: $str", addNewLine)
    private fun info(str: String, addNewLine: Boolean = true) = yellow("\t$str", addNewLine)
    private fun callout(str: String, addNewLine: Boolean = true) = blue("\tCALLOUT: $str", addNewLine)
    private fun requestResponse(str: String, addNewLine: Boolean = true) = cyan("\t$str", addNewLine)
    private fun message(str: String, addNewLine: Boolean = true) = reset(str, addNewLine)

    private fun canBlamp(event: ChannelMessageEvent) =
        if (inBlampCooldownPeriod()) {
            false
        } else if (event.user.name == lastUser && lastUser != "jerbtrundles") {
            error("NO BLAMPING SAME PERSON TWICE.")
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
        if (isCalloutInCooldown()) {
            info("CALLOUT COOLDOWN: ${timeRemaining(lastCalloutTime, calloutCooldown) / 1000.0} seconds to go.")
        } else {
            getCallout(event.message.lowercase())?.let {
                val message = when (it) {
                    "durrrr key" -> "DOIKEY!"
                    "turkey",
                    "name something people take with them to the beach",
                    "the first thing you buy at a supermarket",
                    "a food often stuffed" -> "TOIKEY!"

                    "turkey tits" -> "TOIKEY TITS!"
                    else -> it.uppercase() + "!"
                }
                event.twitchChat.sendMessage(channel, message)
                callout(message)
                lastCalloutTime = Instant.now()
            }
        }
    }

    private fun doBlamp(event: ChannelMessageEvent) {
        val blamped = AIHelper.blampify(event.message).trim()

        if (blamped.isEmpty()) {
            error("BLAMPED MESSAGE IS EMPTY")
        } else if (blamped.lowercase() == event.message.lowercase()) {
            error("BLAMPED MESSAGE SAME AS ORIGINAL MESSAGE")
        } else if (Phrases.containsEmoji(blamped)) {
            error("BLAMPED MESSAGE CONTAINS EMOJI")
        } else if (Phrases.forbidden.any { blamped.lowercase().contains(it) }) {
            error("BLAMPED MESSAGE CONTAINS FORBIDDEN WORD")
        } else if (blamped.lowercase().contains("blampfied")) {
            error("BLAMPBOT IS QUOTING ITSELF AGAIN!")
        } else if (!blamped.lowercase().contains("blamp")) {
            error("BLAMPED MESSAGE DOESN'T CONTAIN BLAMP")
        } else if (bigMismatch(event.message, blamped)) {
            green("\tBLAMPIFY - ${event.message} -> $blamped")
            error("BLAMPED MESSAGE SIGNIFICANTLY DIFFERENT THAN ORIGINAL")
        } else {
            if (blamped.startsWith("\"") && blamped.endsWith("\"")) {
                val trimmed = blamped.substring(
                    startIndex = 1,
                    endIndex = blamped.length - 1
                )

                green("\tBLAMPIFY (TRIMMED) - ${event.message} -> $trimmed")
                event.twitchChat.sendMessage(channel, trimmed)
                lastMessage = trimmed
            } else {
                green("\tBLAMPIFY - ${event.message} -> $blamped")
                event.twitchChat.sendMessage(channel, blamped)
                lastMessage = blamped
            }

            lastBlampTime = Instant.now()
            blampCooldown = Random.nextLong(from = blampIntervalMinimum, until = blampIntervalMaximum)
            lastUser = event.user.name
        }
    }

    private const val BLAMPED_STRING_MISMATCH_THRESHOLD = 60
    private fun bigMismatch(message: String, blamped: String): Boolean {
        val score = Levenshtein.similarityScore(message, blamped)
        yellowBold("\tSimilarity: $score/100")
        return score < BLAMPED_STRING_MISMATCH_THRESHOLD
    }

    private fun canRetry(event: ChannelMessageEvent) =
        event.message.startsWith("try again, blampbot")
                && !inRetryCooldownPeriod()

    private fun doRetry(event: ChannelMessageEvent) =
        with(AIHelper.blampify(lastMessage)) {
            event.twitchChat.sendMessage("jerbtrundles", this)
            event.twitchChat.sendMessage(channel, this)
        }


    private fun isValid(event: ChannelMessageEvent) =
        if (isBlampbotYes(event.message.lowercase()) || isBlampbotNo(event.message.lowercase()) || isBlampbotWhy(event.message.lowercase())) {
            true
        } else if (!messageContainsSpace(event) && !messageContainsCallout(event)) {
            error("MESSAGE MUST CONTAIN AT LEAST ONE SPACE")
            false
        } else if (messageFromExcludedUser(event)) {
            error("MESSAGE IS FROM EXCLUDED USER - ", addNewLine = false)
            purple(event.user.name)
            false
        } else if (event.message.length < minimumMessageLength) {
            error("MESSAGE TOO SHORT - ${event.message.length} (minimum: $minimumMessageLength)")
            false
        } else if (event.message.length > maximumMessageLength) {
            error("MESSAGE TOO LONG - ${event.message.length} (maximum: $maximumMessageLength)")
            false
        } else {
            val emojis = Phrases.getEmojis(event.message)
            if (emojis.isNotEmpty()) {
                error("MESSAGE CONTAINS EMOJIS - ", addNewLine = false)
                rainbow(emojis.joinToString())
                false
            } else {
                getExcludedPhraseFromMessage(event)?.let {
                    error("CONTAINS EXCLUDED PHRASING - ", addNewLine = false)
                    purple(it)
                    false
                } ?: true
            }
        }

    private fun getExcludedPhraseFromMessage(event: ChannelMessageEvent) =
        Phrases.excluded.firstOrNull { event.message.lowercase().contains(it.lowercase()) }

    private fun messageContainsSpace(event: ChannelMessageEvent) =
        event.message.contains(" ")

    private fun messageContainsExcludedPhrase(event: ChannelMessageEvent) =
        Phrases.excluded.any { event.message.lowercase().contains(it.lowercase()) }

    private fun messageFromExcludedUser(event: ChannelMessageEvent) =
        Users.isExcluded(event.user.name.lowercase())

    private fun messageContainsCallout(event: ChannelMessageEvent) =
        Phrases.containsCallout(event.message.lowercase())

    internal fun processEvent(event: ChannelMessageEvent) {
        message("${event.channel.name} - ${event.user.name} - \"${event.message}\"")

        displayBlampStatus()

        if (isJerbRequest(event)) {
            doJerbRequest(event)
        } else if (isValid(event) || isSomeoneSayingHello(event)) {
            with(event.message.lowercase()) {
                when {
                    isBlampbotNo(this) -> beScolded(event)
                    isBlampbotYes(this) -> bePraised(event)
                    isBlampbotWhy(this) -> beWhyd(event)
                    isSomeoneSayingHelloAndTurkey(event) -> doHelloBackWithTurkey(event)
                    isSomeoneSayingHello(event) -> doHelloBack(event)
                    canRequest(event) -> doRequest(event)
                    canWisdom(event) -> doWisdom(event)
                    containsCallout(this) -> doCallout(event)
                    canRetry(event) -> doRetry(event)
                    canBlamp(event) -> doBlamp(event)
                    else -> brightCyan("\tMessage is blampifiable!")
                }
            }
        }
    }
}