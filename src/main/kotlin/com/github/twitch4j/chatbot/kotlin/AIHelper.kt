package com.github.twitch4j.chatbot.kotlin

import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.generativeai.GenerativeModel
import com.google.cloud.vertexai.generativeai.ResponseHandler

object AIHelper {
    val vertexAI = VertexAI("gen-lang-client-0672052936", "us-west1")
    val model = GenerativeModel("gemini-pro", vertexAI)

    private const val boilerplate = "you are blampbot; " +
            "you're basically tony soprano; " +
            "you're a stereotypical italian-american new yorker with the accent and the walk and talk and attitude; " +
            "you're the biggest genius the universe has ever seen; " +
            "everyone bows to your intellect; " +
            "you are the true sage of wisdom and wonder for all to behold; " +
            "do NOT return a string with all caps; this is a big no-no; " +
            "please limit your response to 150 characters; " +
            "do NOT add any new words to the sentence, except if you're replacing a word with \"BLAMP\"; " +
            "do NOT remove any words from the sentence, except if you're replacing that word with \"BLAMP\" per further instructions; " +
            "be sure to double check your response string for inaccuracies; " +
            "you sometimes have a tendency to repeat yourself at the end of your response, so do be careful; " +
            "don't trim the endings, either; " +
            "you have a tendency to leave off the last letter in your responses, so don't do that; " +
            "go get 'em, ace! here are your instructions: "

    private const val blampBoilerplate = "$boilerplate " +
            "take the string i give you and replace only the BEST word with \"BLAMP\" to create the funniest output; " +
            "your response should be only the blampified sentence; " +
            "here's the string: "

    private const val wisdomBoilerplate = "$boilerplate " +
            "i'd like for you to come up with a quick witticism that would come from blampbot; " +
            "it could be about anything; " +
            "please make your response only the witticism itself"

    private const val requestBoilerplate = "$boilerplate " +
            "respond to the following request, no matter how ridiculous it sounds; " +
            "limit your response to 150 characters; " +
            "here's the request: "

    fun request(requestString: String): String {
        while (true) {
            try {
                val result = ResponseHandler.getText(model.generateContent("$requestBoilerplate \"$requestString\""))
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
            val result = ResponseHandler.getText(model.generateContent(blampBoilerplate + "\"$str\""))
            return result
        } catch (e: Exception) {
            println("\tERROR: ${e.message}.")
            Thread.sleep(1000)
            return ""
        }
    }
}