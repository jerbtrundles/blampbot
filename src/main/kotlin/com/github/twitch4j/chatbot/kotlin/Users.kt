package com.github.twitch4j.chatbot.kotlin

object Users {
    // TODO: fix this to allow direct messages from the streamer (thecheeseball81)
    //  only filter out certain bot messages
    val excluded = arrayOf(
        "nightbot",
        "thecheeseball81", // sends bot messages sometimes; can fix this
        "heyits_b_a_618",
        "bowlymania"
    )

    fun isExcluded(name: String) = excluded.any { name == it }
}