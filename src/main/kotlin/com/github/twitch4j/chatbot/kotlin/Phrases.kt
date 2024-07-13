package com.github.twitch4j.chatbot.kotlin

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
    )

    val excluded = sequenceOf(
        "_",
        "@",
        "gg",
        "http",
    )

    internal val forbidden = arrayOf(
        "moist"
    )

    fun containsCallout(str: String) = calloutWords.any { str.contains(it) }

    val regexEmoji = Regex("""\b[a-z][\S]*[A-Z][\S]*\b""")

    fun containsEmoji(str: String) =
        str.split(" ").any { token -> isEmoji(token) }
    private fun isEmoji(token: String): Boolean {
        return regexEmoji.containsMatchIn(token)
    }

    fun getEmojis(str: String) = regexEmoji.findAll(str).map { it.value }.toList().distinct()
}
