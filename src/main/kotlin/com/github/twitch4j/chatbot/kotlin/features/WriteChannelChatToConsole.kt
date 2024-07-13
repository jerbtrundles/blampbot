package com.github.twitch4j.chatbot.kotlin.features

import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.chatbot.kotlin.*

class WriteChannelChatToConsole(eventHandler: SimpleEventHandler) {
    init {
        eventHandler.onEvent(ChannelMessageEvent::class.java, this::onChannelMessage)
    }

    /** Subscribe to the ChannelMessage Event and write the output to the console */
    private fun onChannelMessage(event: ChannelMessageEvent) {
        try {
            Blampbot.processEvent(event)
        } catch (e: Exception) {
            println(e.stackTraceToString())
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
