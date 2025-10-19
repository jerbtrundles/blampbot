package com.github.twitch4j.chatbot.kotlin

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent

object Phrases {
    // sometimes the shorter phrases are substrings of longer phrases
    // sort by length so that longer phrases are returned before shorter ones
    val calloutWords = sequenceOf(
        "name something people take with them to the beach",
        "the first thing you buy at a supermarket",
        "a food often stuffed",
        "turkey tits",
        "durrrr key",
        "spaghetti",
        "rigatoni",
        "gabagool",
        "asshole",
        "boobies",
        "titties",
        "turkey",
        "tits",
        // "doing good",
    )

    val forbiddenWords = sequenceOf(
        "_",
        "@",
        "gg",
        "http",
        "addcheese",
        "depress",
        "moist",
        "cheer",
        "hype"
    )

    fun containsCallout(str: String) =
        calloutWords.any { str.contains(it) }

    fun getCalloutWordsList(str: String) =
        calloutWords.filter { str.contains(it) }.toList()

    fun getForbiddenWordsList(str: String) =
        forbiddenWords.filter { str.contains(it) }.toList()

    val regexEmoji =
        Regex("""\b[a-z][\S]*[A-Z][\S]*\b""")

    fun containsEmoji(str: String) =
        str.split(" ")
            .any { token -> isEmoji(token) }

    private fun isEmoji(token: String) =
        regexEmoji.containsMatchIn(token)

    fun getEmojis(str: String) =
        regexEmoji.findAll(str)
            .map { it.value }
            .toList()
            .distinct()

    fun getCalloutFromMessageOrNull(event: ChannelMessageEvent) =
        calloutWords.firstOrNull { calloutWord -> event.message.lowercase().contains(calloutWord) }

}
