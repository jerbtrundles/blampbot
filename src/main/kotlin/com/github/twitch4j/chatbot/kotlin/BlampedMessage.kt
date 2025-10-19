package com.github.twitch4j.chatbot.kotlin

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.chatbot.kotlin.Blampbot.BLAMPED_STRING_MISMATCH_LOWER_THRESHOLD
import com.github.twitch4j.chatbot.kotlin.Blampbot.MINIMUM_MESSAGES_BETWEEN_BLAMPS
import com.github.twitch4j.chatbot.kotlin.Blampbot.messagesSinceLastBlamp

class BlampedMessage private constructor(
    val original: String,
    val blamped: String,
    val similarityScore: Int,
    val isEmpty: Boolean,
    val isSame: Boolean,
    val containsEmoji: Boolean,
    val containsForbidden: Boolean,
    val quotingItself: Boolean,
    val containsBlamp: Boolean,
    val tooDifferent: Boolean,
    val isValid: Boolean,
    val fromLastBlampedUser: Boolean,
    val notEnoughMessages: Boolean,
) {
    fun printStatus(event: ChannelMessageEvent) {
        Messaging.green("\tBLAMPIFY - ${event.message} -> $blamped")
        Messaging.green("\tSimilarity score: - $similarityScore")

        if (isEmpty) Messaging.error("BLAMPED MESSAGE IS EMPTY")
        if (isSame) Messaging.error("BLAMPED MESSAGE SAME AS ORIGINAL MESSAGE")
        if (containsEmoji) Messaging.error("BLAMPED MESSAGE CONTAINS EMOJI")
        if (containsForbidden) Messaging.error("BLAMPED MESSAGE CONTAINS FORBIDDEN WORD")
        if (quotingItself) Messaging.error("BLAMPBOT IS QUOTING ITSELF AGAIN!")
        if (!containsBlamp) Messaging.error("BLAMPED MESSAGE DOESN'T CONTAIN BLAMP")
        if (tooDifferent) Messaging.error("BLAMPED MESSAGE SIGNIFICANTLY DIFFERENT THAN ORIGINAL")
        if (fromLastBlampedUser) Messaging.error("NO BLAMPING SAME PERSON TWICE.")
        if (notEnoughMessages) Messaging.error("NOT ENOUGH MESSAGES SINCE LAST BLAMP ($messagesSinceLastBlamp/$MINIMUM_MESSAGES_BETWEEN_BLAMPS)")
    }

    companion object {
        fun fromString(original: String, fromLastBlampedUser: Boolean, notEnoughMessages: Boolean): BlampedMessage {
            val blamped = blampify(original)
            val similarityScore = Levenshtein.similarityScore(original, blamped)
            val tooDifferent = similarityScore < BLAMPED_STRING_MISMATCH_LOWER_THRESHOLD
            val containsEmoji = Phrases.containsEmoji(blamped)
            val containsForbidden = Phrases.forbiddenWords.any { blamped.lowercase().contains(it) }
            val quotingItself = blamped.lowercase().contains("blampfied")
            val containsBlamp = blamped.lowercase().contains("blamp")
            val isSame = blamped == original
            val isEmpty = blamped.isEmpty()

            return BlampedMessage(
                original = original,
                blamped = blamped,
                similarityScore = similarityScore,
                isEmpty = isEmpty,
                isSame = isSame,
                containsEmoji = containsEmoji,
                containsForbidden = containsForbidden,
                quotingItself = quotingItself,
                containsBlamp = containsBlamp,
                tooDifferent = tooDifferent,
                isValid = !isEmpty && !isSame && !containsEmoji && !containsForbidden
                        && !quotingItself && !tooDifferent && containsBlamp,
                fromLastBlampedUser = fromLastBlampedUser,
                notEnoughMessages = notEnoughMessages
            )
        }

        fun blampify(original: String): String {
            var blamped = AIHelper.blampify(original).trim()

            blamped = if (blamped.startsWith("\"") && blamped.endsWith("\"")) {
                blamped.substring(
                    startIndex = 1,
                    endIndex = blamped.length - 1
                )
            } else {
                blamped
            }

            return blamped
        }
    }
}