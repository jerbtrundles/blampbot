package com.github.twitch4j.chatbot.kotlin

object Phrases {
    val calloutWords = arrayOf(
        "boobies",
        "titties",
        "tits",
        "spaghetti",
        "rigatoni",
        "asshole",
        "turkey",
        "name something people take with them to the beach",
        "the first thing you buy at a supermarket",
        "a food often stuffed",
        "gabagool",
        "durrrr key"
    )

    val excluded = sequenceOf(
        "a31j",
        "airfor6",
        "aliceh",
        "amad",
        "arcus",
        "blackw",
        "cheese23",
        "chroma82",
        "contri",
        "destin279",
        "ejsa",
        "erinpl",
        "fcough",
        "finame",
        "geemal",
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
        "jwong",
        "kbro",
        "kek",
        "king69",
        "kripp",
        "ladyga",
        "lakegu",
        "luckybun",
        "lupus",
        "method420",
        "missme64",
        "necrov",
        "pega",
        "pikas",
        "pog",
        "rat707",
        "rbz",
        "retroi3",
        "rockst",
        "rpgm",
        "shindi",
        "ssonic",
        "sweetheart",
        "tagn",
        "tasn",
        "tetral",
        "theche31",
        "thekin502MeatWad",
        "thekom",
        "themec2",
        "xchris",
        "scg"
    )

    fun containsCallout(str: String) = calloutWords.any { str.contains(it) }
}