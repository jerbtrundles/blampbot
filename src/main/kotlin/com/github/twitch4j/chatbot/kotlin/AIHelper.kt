package com.github.twitch4j.chatbot.kotlin

import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler

object AIHelper {
    val vertexAI = VertexAI("gen-lang-client-0672052936", "us-west1")
    val model = GenerativeModel("gemini-pro", vertexAI)

    private const val boilerplate = "you are blampbot; " +
            "you're basically tony soprano; " +
            "take the string i give you and replace only one word with BLAMP to create the funniest output; " +
            "your response should be only the blampified sentence; " +
            "do NOT return a string with all caps; this is a big no-no; " +
            "here's the string: "

    val misblamps = arrayOf(
        "BLAMP!",
        "BLAMP,",
        "BLAMP.",
        "BLAMP;",
        "blamp",
    )

    private const val wisdomBoilerplate = "you are blampbot; " +
            "you act like tony soprano; " +
            "that is, you're a stereotypical italian-american new yorker with the accent and the walk and the talk and the attitude; " +
            "you love gabagool; also, you sometimes replace words you say with BLAMP to make things sound funny; people love it; " +
            "i'd like for you to come up with a quick witticism that would come from blampbot; " +
            "it could be about anything; " +
            "please make your response only the witticism itself"

    private const val requestBoilerplate = "you are blampbot; " +
            "you act like tony soprano; " +
            "that is, you're a stereotypical italian-american new yorker with the accent and the walk and the talk and the attitude; " +
            "you love gabagool; also, you sometimes replace words you say with BLAMP to make things sound funny; people love it; " +
            "you're the biggest genius the universe has ever seen; " +
            "everyone bows to your intellect; " +
            "you are the true sage of wisdom and wonder for all to behold; " +
            "please limit your response to 150 characters; we have but one humble request, and that is this: "

    fun request(requestString: String): String {
        while (true) {
            try {
                val result = ResponseHandler.getText(model.generateContent("$requestBoilerplate$requestString"))
                if (result.isEmpty()) {
                    println("\tEMPTY RESULT.")
                } else {
                    println("\tREQUEST: $result")
                    return result
                }
            } catch (e: Exception) {
                println("\tERROR: ${e.message}")
                Thread.sleep(1000)
            }
        }
    }


    fun getWisdom(): String {
        while (true) {
            try {
                val result = ResponseHandler.getText(model.generateContent(wisdomBoilerplate))
                if (result.isEmpty()) {
                    println("\tEMPTY RESULT.")
                } else {
                    println("\tWISDOM: $result")
                    return result
                }
            } catch (e: Exception) {
                println("\tERROR: ${e.message}")
                Thread.sleep(1000)
            }
        }
    }

    fun blampify(str: String): String {
        try {
            var result = ResponseHandler.getText(model.generateContent(boilerplate + "\"$str\""))
            if (result.isNotEmpty()) {
                misblamps.forEach { misblamp ->
                    result = result.replace(misblamp, "BLAMP")
                }
                return result
            } else {
                return ""
            }
        } catch (e: Exception) {
            println("\tERROR: ${e.message}.")
            Thread.sleep(1000)
            return ""
        }
    }
}