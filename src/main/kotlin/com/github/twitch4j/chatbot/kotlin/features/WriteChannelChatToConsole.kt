package com.github.twitch4j.chatbot.kotlin.features

import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.chatbot.kotlin.AIHelper
import kotlin.random.Random
import java.time.Duration
import java.time.Instant

class WriteChannelChatToConsole(eventHandler: SimpleEventHandler) {
    init {
        eventHandler.onEvent(ChannelMessageEvent::class.java, this::onChannelMessage)
    }

    private var blampInterval = 180000L
    private var lastBlampTime = Instant.now() // .minusMillis(blampInterval)

    private var wisdomInterval = 15000L
    private var lastWisdomTime = Instant.now().minusMillis(wisdomInterval)

    private var calloutCooldown = 30000L
    private var lastCalloutTime = Instant.now().minusMillis(calloutCooldown)

    private var retryInterval = 30000L
    private var lastRetryTime = Instant.now().minusMillis(retryInterval)

    private var requestInterval = 30000L
    private var lastRequestTime = Instant.now().minusMillis(requestInterval)

    private var lastMessage = ""
    private var lastUser = ""
    private val minimumMessageLength = 30

    private val excludedPhrases = sequenceOf(
        "I just wanted to say that I truly",
        "a31j",
        "aliceh",
        "arcus",
        "blackw",
        "cheese23",
        "chroma82",
        "ejsa",
        "erinpl",
        "fcough",
        "finame",
        "gg",
        "grayskull",
        "hgo",
        "hoodyt",
        "http",
        "hype",
        "jammit",
        "jayc",
        "jimbles",
        "jsr",
        "jt",
        "junkya",
        "just cheered with",
        "kbro",
        "kek",
        "king69",
        "kripp",
        "ladyga",
        "lakegu",
        "lupus",
        "method420",
        "missme64",
        "necrov",
        "pega",
        "pikas",
        "rat707",
        "rbz",
        "retroi3",
        "rockst",
        "shindi",
        "ssonic",
        "sweetheart",
        "tagn",
        "tasn",
        "tetral",
        "theche31",
        "thekom",
        "themec2",
        "xchris",
        "scg"
    )

    // TODO: fix this to allow direct messages from the streamer (thecheeseball81)
    //  only filter out certain bot messages
    private val excludedUsers = arrayOf(
        "nightbot",
        "thecheeseball81", // sends bot messages sometimes; can fix this
        "heyits_b_a_618",
    )

    private fun timeSince(instant: Instant) = Duration.between(instant, Instant.now()).toMillis()
    private fun timeRemaining(lastTime: Instant, interval: Long) = interval - timeSince(lastTime)
    private fun isCooldownOver(lastTime: Instant, interval: Long) = timeRemaining(lastTime, interval) <= 0

    private fun inBlampCooldownPeriod() = !isCooldownOver(lastBlampTime, blampInterval)
    private fun inRetryCooldownPeriod() = !isCooldownOver(lastRetryTime, retryInterval)
    private fun isCalloutInCooldown() = !isCooldownOver(lastCalloutTime, calloutCooldown)
    private fun isWisdomInCooldown() = !isCooldownOver(lastWisdomTime, wisdomInterval)
    private fun isRequestInCooldown() = !isCooldownOver(lastRequestTime, requestInterval)

    val channel = "thecheeseball81"

    /** Subscribe to the ChannelMessage Event and write the output to the console */
    private fun onChannelMessage(event: ChannelMessageEvent) {
        try {
            println("${event.channel.name} - ${event.user.name} - \"${event.message}\"")

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
        } catch (e: Exception) {
            println("caught an exception")
            println(e.stackTraceToString())
        }
    }

    private fun isSomeoneSayingHelloAndTurkey(event: ChannelMessageEvent) =
        isSomeoneSayingHello(event) && event.message.contains("turkey")

    private fun doHelloBackWithTurkey(event: ChannelMessageEvent) =
        with("you tryna BLAMP wit' me?") {
            println("\t$this")
            event.twitchChat.sendMessage(channel, this)
        }


    private fun isJerbRequest(event: ChannelMessageEvent) =
        event.user.name.lowercase() == "jerbtrundles" && event.message.startsWith("blampbot, ")

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
                    || (
                    (startsWith("hey")) && contains("blampbot"))
                    )
        }

    private fun doHelloBack(event: ChannelMessageEvent) {
        val response = "Hey there, ${event.user.name}!"
        println("\tHELLO BACK - $response")
        event.twitchChat.sendMessage(channel, response)
    }

    private fun doWisdom(event: ChannelMessageEvent) {
        val wisdom = AIHelper.getWisdom()
        println("\t$wisdom")
        event.twitchChat.sendMessage(channel, wisdom)
    }

    private fun bePraised(event: ChannelMessageEvent) {
        println("\tTHEY LOVE ME!")
        event.twitchChat.sendMessage(channel, ":D")
    }

    private fun beScolded(event: ChannelMessageEvent) {
        println("\tTHEY NO LIKE ME :(")
        event.twitchChat.sendMessage(channel, ":(")
    }

    private fun beWhyd(event: ChannelMessageEvent) {
        println("\tTHEY NOT SURE ABOUT ME !?!!!?")
        event.twitchChat.sendMessage(channel, "O_o")
    }

    private fun canBlamp(event: ChannelMessageEvent) =
        if (inBlampCooldownPeriod()) {
            println("\tBLAMP COOLDOWN: ${timeRemaining(lastBlampTime, blampInterval) / 1000.0} seconds to go.")
            false
        } else if (event.user.name == lastUser) {
            println("\tNO BLAMPING SAME PERSON TWICE.")
            false
        } else if (event.message.length < minimumMessageLength) {
            println("\tMESSAGE TOO SHORT: ${event.message.length} (minimum: $minimumMessageLength)")
            false
        } else {
            true
        }

    private fun canRequest(event: ChannelMessageEvent) =
        if (!event.message.startsWith("blampbot, ")) {
            false
        } else if (isRequestInCooldown()) {
            println("\tREQUEST COOLDOWN: ${timeRemaining(lastRequestTime, requestInterval) / 1000.0} seconds to go.")
            false
        } else {
            true
        }

    private fun doRequest(event: ChannelMessageEvent) {
        val response = AIHelper.request(event.message.substringAfter("blampbot, "))
        println("\t$response")
        event.twitchChat.sendMessage(channel, response)
    }

    private fun canWisdom(event: ChannelMessageEvent) =
        if (!event.message.startsWith("blampbot wisdom")) {
            false
        } else if (isWisdomInCooldown()) {
            println("\tWISDOM COOLDOWN: ${timeRemaining(lastWisdomTime, wisdomInterval) / 1000.0} seconds to go.")
            false
        } else {
            true
        }

    private fun containsCallout(lowerCaseString: String) =
        calloutWords.any { lowerCaseString.contains(it) }

    private fun getCallout(lowerCaseString: String) =
        calloutWords.firstOrNull { lowerCaseString.contains(it) }

    private val calloutWords = arrayOf(
        "boobies",
        "titties",
        "tits",
        "spaghetti",
        "asshole",
        "turkey",
        "name something people take with them to the beach",
        "the first thing you buy at a supermarket",
        "a food often stuffed",
        "gabagool",
        "durrrr key"
    )

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
                lastCalloutTime = Instant.now()
            }
        }
    }

    fun doBlamp(event: ChannelMessageEvent) {
        if (isValid(event)) {
            val blamped = AIHelper.blampify(event.message)

            if (blamped.isNotEmpty()
                && blamped != event.message
                && !blamped.contains("moist")
            ) {
                println(
                    "\t<-- *** -->\n" +
                            "\tBLAMPIFY\n" +
                            "\t${event.message} -> $blamped\n" +
                            "\t<-- *** -->"
                )

                event.twitchChat.sendMessage(channel, blamped)
                lastBlampTime = Instant.now()
                blampInterval = Random.nextLong(180000, 480000)
                lastMessage = event.message
                lastUser = event.user.name
            } else {
                println("\tsomething happened; not sending this one")
            }
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
        if (!containsSpace(event)) {
            println("\tNO SPACEY")
            false
        } else if (containsExcludedPhrase(event)) {
            println("\tCONTAINS EXCLUDED PHRASING")
            false
        } else if (containsExcludedUser(event)) {
            println("\tFROM EXCLUDED USER: ${event.user.name}")
            false
        } else {
            true
        }

    fun containsSpace(event: ChannelMessageEvent) = event.message.contains(" ")
    fun containsExcludedPhrase(event: ChannelMessageEvent) =
        excludedPhrases.any { event.message.lowercase().contains(it.lowercase()) }

    fun containsExcludedUser(event: ChannelMessageEvent) = excludedUsers.any { event.user.name.lowercase() == it }
}

// AIzaSyBxx_AOmE8gsgdYERBM6bpYdjtsU_T6qwY
// curl \
//  -H 'Content-Type: application/json' \
//  -d '{"contents":[{"parts":[{"text":"Write a story about a magic backpack"}]}]}' \
//  -X POST https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=AIzaSyBxx_AOmE8gsgdYERBM6bpYdjtsU_T6qwY