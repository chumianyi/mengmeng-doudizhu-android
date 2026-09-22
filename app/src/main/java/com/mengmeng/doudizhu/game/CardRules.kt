package com.mengmeng.doudizhu.game

import com.mengmeng.doudizhu.model.Card
import com.mengmeng.doudizhu.model.Combo

object CardRules {

    // Card values: 3=3, 4=4, ..., 10=10, J=11, Q=12, K=13, A=14, 2=15, small_joker=16, big_joker=17
    fun cardValue(card: Card): Int {
        return card.value.takeIf { it > 0 } ?: when (card.rank) {
            "3" -> 3; "4" -> 4; "5" -> 5; "6" -> 6; "7" -> 7; "8" -> 8; "9" -> 9; "10" -> 10
            "J" -> 11; "Q" -> 12; "K" -> 13; "A" -> 14; "2" -> 15
            "small_joker" -> 16; "big_joker" -> 17
            else -> 0
        }
    }

    fun rankDisplay(card: Card): String {
        return when (card.rank) {
            "small_joker" -> "小王"; "big_joker" -> "大王"
            else -> card.rank ?: ""
        }
    }

    fun isRed(card: Card): Boolean {
        return card.suit == "heart" || card.suit == "diamond" || card.suit == "joker"
    }

    fun sortedCards(cards: List<Card>): List<Card> {
        return cards.sortedBy { cardValue(it) }
    }

    fun sortedCardsDesc(cards: List<Card>): List<Card> {
        return cards.sortedByDescending { cardValue(it) }
    }

    fun createDeck(): List<Card> {
        val suits = listOf("spade", "heart", "club", "diamond")
        val ranks = listOf("3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2")
        val deck = mutableListOf<Card>()
        var id = 0
        for (suit in suits) {
            for (rank in ranks) {
                deck.add(Card(id = "c$id", suit = suit, rank = rank, value = rankToValue(rank)))
                id++
            }
        }
        deck.add(Card(id = "c$id", suit = "joker", rank = "small_joker", value = 16)); id++
        deck.add(Card(id = "c$id", suit = "joker", rank = "big_joker", value = 17))
        return deck
    }

    private fun rankToValue(rank: String): Int = when (rank) {
        "3" -> 3; "4" -> 4; "5" -> 5; "6" -> 6; "7" -> 7; "8" -> 8; "9" -> 9; "10" -> 10
        "J" -> 11; "Q" -> 12; "K" -> 13; "A" -> 14; "2" -> 15
        "small_joker" -> 16; "big_joker" -> 17
        else -> 0
    }

    fun shuffle(deck: List<Card>): List<Card> {
        val list = deck.toMutableList()
        list.shuffle()
        return list
    }

    // Analyze a set of cards and return the combo type
    fun analyzeCards(cards: List<Card>): Combo? {
        if (cards.isEmpty()) return null
        val sorted = sortedCards(cards)
        val values = sorted.map { cardValue(it) }
        val counts = values.groupingBy { it }.eachCount().toSortedMap()

        // Rocket (two jokers)
        if (cards.size == 2 && values.contains(16) && values.contains(17)) {
            return Combo(type = "rocket", main = 17, length = 2, cards = sorted)
        }

        // Bomb (four of a kind)
        if (cards.size == 4 && counts.size == 1 && counts.values.first() == 4) {
            return Combo(type = "bomb", main = counts.keys.first(), length = 4, cards = sorted)
        }

        // Single
        if (cards.size == 1) {
            return Combo(type = "single", main = values[0], length = 1, cards = sorted)
        }

        // Pair
        if (cards.size == 2 && counts.size == 1 && counts.values.first() == 2) {
            return Combo(type = "pair", main = counts.keys.first(), length = 2, cards = sorted)
        }

        // Triple
        if (cards.size == 3 && counts.size == 1 && counts.values.first() == 3) {
            return Combo(type = "triple", main = counts.keys.first(), length = 3, cards = sorted)
        }

        // Triple + single
        if (cards.size == 4 && counts.size == 2) {
            val tripleKey = counts.entries.find { it.value == 3 }?.key
            val singleKey = counts.entries.find { it.value == 1 }?.key
            if (tripleKey != null && singleKey != null && tripleKey < 16) {
                return Combo(type = "triple_single", main = tripleKey, length = 3, cards = sorted)
            }
        }

        // Triple + pair
        if (cards.size == 5 && counts.size == 2) {
            val tripleKey = counts.entries.find { it.value == 3 }?.key
            val pairKey = counts.entries.find { it.value == 2 }?.key
            if (tripleKey != null && pairKey != null && tripleKey < 16) {
                return Combo(type = "triple_pair", main = tripleKey, length = 3, cards = sorted)
            }
        }

        // Straight (5+ consecutive singles, no 2 or jokers)
        if (cards.size >= 5 && counts.values.all { it == 1 }) {
            val vals = counts.keys.filter { it <= 14 }.sorted()
            if (vals.size == cards.size && isConsecutive(vals)) {
                return Combo(type = "straight", main = vals.last(), length = vals.size, cards = sorted)
            }
        }

        // Pair straight (3+ consecutive pairs, no 2 or jokers)
        if (cards.size >= 6 && cards.size % 2 == 0 && counts.values.all { it == 2 }) {
            val vals = counts.keys.filter { it <= 14 }.sorted()
            if (vals.size == counts.size && isConsecutive(vals)) {
                return Combo(type = "pair_straight", main = vals.last(), length = vals.size, cards = sorted)
            }
        }

        // Airplane (2+ consecutive triples, no 2 or jokers)
        if (cards.size >= 6 && cards.size % 3 == 0) {
            val tripleKeys = counts.entries.filter { it.value >= 3 }.map { it.key }.filter { it <= 14 }.sorted()
            if (tripleKeys.size >= 2 && isConsecutive(tripleKeys)) {
                val pureTriples = tripleKeys.size * 3
                if (cards.size == pureTriples) {
                    return Combo(type = "airplane", main = tripleKeys.last(), length = tripleKeys.size, cards = sorted)
                }
            }
        }

        // Airplane + singles
        if (cards.size >= 8) {
            val tripleKeys = counts.entries.filter { it.value >= 3 }.map { it.key }.filter { it <= 14 }.sorted()
            if (tripleKeys.size >= 2 && isConsecutive(tripleKeys)) {
                val tripleCount = tripleKeys.size
                val expectedSize = tripleCount * 4
                if (cards.size == expectedSize) {
                    val singleCount = counts.entries.count { it.value == 1 }
                    val usedInTriples = tripleKeys.sumOf { minOf(counts[it] ?: 0, 3) }
                    val remaining = cards.size - usedInTriples
                    if (remaining == tripleCount) {
                        return Combo(type = "airplane_single", main = tripleKeys.last(), length = tripleCount, cards = sorted)
                    }
                }
            }
        }

        // Airplane + pairs
        if (cards.size >= 10 && cards.size % 5 == 0) {
            val tripleKeys = counts.entries.filter { it.value >= 3 }.map { it.key }.filter { it <= 14 }.sorted()
            if (tripleKeys.size >= 2 && isConsecutive(tripleKeys)) {
                val tripleCount = tripleKeys.size
                val expectedSize = tripleCount * 5
                if (cards.size == expectedSize) {
                    val pairKeys = counts.entries.filter { it.value == 2 }.map { it.key }
                    if (pairKeys.size == tripleCount) {
                        return Combo(type = "airplane_pair", main = tripleKeys.last(), length = tripleCount, cards = sorted)
                    }
                }
            }
        }

        // Four + two singles
        if (cards.size == 6 && counts.size == 3) {
            val fourKey = counts.entries.find { it.value == 4 }?.key
            if (fourKey != null && fourKey < 16) {
                val singles = counts.entries.filter { it.value == 1 }
                if (singles.size == 2) {
                    return Combo(type = "four_two", main = fourKey, length = 4, cards = sorted)
                }
            }
        }

        // Four + two pairs
        if (cards.size == 8 && counts.size == 3) {
            val fourKey = counts.entries.find { it.value == 4 }?.key
            if (fourKey != null && fourKey < 16) {
                val pairs = counts.entries.filter { it.value == 2 }
                if (pairs.size == 2) {
                    return Combo(type = "four_two_pair", main = fourKey, length = 4, cards = sorted)
                }
            }
        }

        return null
    }

    fun isConsecutive(values: List<Int>): Boolean {
        if (values.size < 2) return true
        for (i in 1 until values.size) {
            if (values[i] != values[i - 1] + 1) return false
        }
        return true
    }

    // Check if comboA beats comboB
    fun comboBeatsCombo(a: Combo?, b: Combo?): Boolean {
        if (a == null) return false
        if (b == null) return true // Any play beats nothing
        if (a.type == "rocket") return true
        if (b.type == "rocket") return false
        if (a.type == "bomb" && b.type != "bomb") return true
        if (b.type == "bomb" && a.type != "bomb") return false
        if (a.type != b.type) return false
        if (a.length != b.length) return false
        return a.main > b.main
    }

    // Find a valid play that beats the last combo, from available cards
    fun findBeatingPlay(hand: List<Card>, lastCombo: Combo?): List<Card>? {
        if (lastCombo == null) {
            // Lead: play lowest single
            val sorted = sortedCards(hand)
            return listOf(sorted.first())
        }

        val sorted = sortedCards(hand)
        val values = sorted.map { cardValue(it) }
        val counts = values.groupingBy { it }.eachCount().toSortedMap()

        when (lastCombo.type) {
            "single" -> {
                val higher = sorted.firstOrNull { cardValue(it) > lastCombo.main }
                if (higher != null) return listOf(higher)
                // Try bomb
                return findBomb(hand)
            }
            "pair" -> {
                for ((v, c) in counts) {
                    if (c >= 2 && v > lastCombo.main && v < 16) {
                        return sorted.filter { cardValue(it) == v }.take(2)
                    }
                }
                return findBomb(hand)
            }
            "triple" -> {
                for ((v, c) in counts) {
                    if (c >= 3 && v > lastCombo.main && v < 16) {
                        return sorted.filter { cardValue(it) == v }.take(3)
                    }
                }
                return findBomb(hand)
            }
            "triple_single" -> {
                for ((v, c) in counts) {
                    if (c >= 3 && v > lastCombo.main && v < 16) {
                        val triple = sorted.filter { cardValue(it) == v }.take(3)
                        val single = sorted.firstOrNull { cardValue(it) != v }
                        if (single != null) return triple + single
                    }
                }
                return findBomb(hand)
            }
            "triple_pair" -> {
                for ((v, c) in counts) {
                    if (c >= 3 && v > lastCombo.main && v < 16) {
                        val triple = sorted.filter { cardValue(it) == v }.take(3)
                        for ((pv, pc) in counts) {
                            if (pc >= 2 && pv != v && pv < 16) {
                                val pair = sorted.filter { cardValue(it) == pv }.take(2)
                                return triple + pair
                            }
                        }
                    }
                }
                return findBomb(hand)
            }
            "straight" -> {
                val len = lastCombo.length
                val validVals = counts.keys.filter { it <= 14 }.sorted()
                for (start in 0..(validVals.size - len)) {
                    val sub = validVals.subList(start, start + len)
                    if (isConsecutive(sub) && sub.last() > lastCombo.main) {
                        return sub.flatMap { v -> sorted.filter { cardValue(it) == v }.take(1) }
                    }
                }
                return findBomb(hand)
            }
            "pair_straight" -> {
                val len = lastCombo.length
                val validVals = counts.entries.filter { it.value >= 2 && it.key <= 14 }.map { it.key }.sorted()
                for (start in 0..(validVals.size - len)) {
                    val sub = validVals.subList(start, start + len)
                    if (isConsecutive(sub) && sub.last() > lastCombo.main) {
                        return sub.flatMap { v -> sorted.filter { cardValue(it) == v }.take(2) }
                    }
                }
                return findBomb(hand)
            }
            "bomb" -> {
                for ((v, c) in counts) {
                    if (c >= 4 && v > lastCombo.main && v < 16) {
                        return sorted.filter { cardValue(it) == v }.take(4)
                    }
                }
                // Rocket
                if (values.contains(16) && values.contains(17)) {
                    return sorted.filter { cardValue(it) >= 16 }
                }
                return null
            }
            "rocket" -> return null
            else -> return findBomb(hand)
        }
    }

    private fun findBomb(hand: List<Card>): List<Card>? {
        val sorted = sortedCards(hand)
        val counts = sorted.map { cardValue(it) }.groupingBy { it }.eachCount()
        for ((v, c) in counts) {
            if (c >= 4 && v < 16) {
                return sorted.filter { cardValue(it) == v }.take(4)
            }
        }
        // Rocket
        val values = sorted.map { cardValue(it) }
        if (values.contains(16) && values.contains(17)) {
            return sorted.filter { cardValue(it) >= 16 }
        }
        return null
    }

    // AI: decide what to play
    fun aiDecidePlay(hand: List<Card>, lastCombo: Combo?, isLandlord: Boolean, isTeammateLast: Boolean): List<Card>? {
        if (lastCombo == null) {
            // Leading: play lowest combo
            return findLowestLead(hand)
        }
        if (isTeammateLast && !isLandlord) {
            // Teammate played, don't beat unless very strong
            if (hand.size <= 4) {
                return findBeatingPlay(hand, lastCombo)
            }
            return null // Pass
        }
        return findBeatingPlay(hand, lastCombo)
    }

    private fun findLowestLead(hand: List<Card>): List<Card> {
        val sorted = sortedCards(hand)
        val counts = sorted.map { cardValue(it) }.groupingBy { it }.eachCount().toSortedMap()

        // Try to play a straight if we have one
        val validVals = counts.keys.filter { it <= 14 }.sorted()
        var longestStraight = mutableListOf<Int>()
        var current = mutableListOf<Int>()
        for (v in validVals) {
            if (current.isEmpty() || v == current.last() + 1) {
                current.add(v)
            } else {
                if (current.size >= 5 && current.size > longestStraight.size) longestStraight = current.toMutableList()
                current = mutableListOf(v)
            }
        }
        if (current.size >= 5 && current.size > longestStraight.size) longestStraight = current.toMutableList()
        if (longestStraight.size >= 5 && longestStraight.size <= hand.size - 2) {
            return longestStraight.flatMap { v -> sorted.filter { cardValue(it) == v }.take(1) }
        }

        // Try pair straight
        val pairVals = counts.entries.filter { it.value >= 2 && it.key <= 14 }.map { it.key }.sorted()
        var longestPairStraight = mutableListOf<Int>()
        current = mutableListOf()
        for (v in pairVals) {
            if (current.isEmpty() || v == current.last() + 1) {
                current.add(v)
            } else {
                if (current.size >= 3 && current.size > longestPairStraight.size) longestPairStraight = current.toMutableList()
                current = mutableListOf(v)
            }
        }
        if (current.size >= 3 && current.size > longestPairStraight.size) longestPairStraight = current.toMutableList()
        if (longestPairStraight.size >= 3 && longestPairStraight.size * 2 <= hand.size - 2) {
            return longestPairStraight.flatMap { v -> sorted.filter { cardValue(it) == v }.take(2) }
        }

        // Play lowest triple+single or triple
        for ((v, c) in counts) {
            if (c >= 3 && v < 15) {
                val triple = sorted.filter { cardValue(it) == v }.take(3)
                val single = sorted.firstOrNull { cardValue(it) != v && cardValue(it) < 15 }
                if (single != null && hand.size > 4) return triple + single
                return triple
            }
        }

        // Play lowest pair
        for ((v, c) in counts) {
            if (c >= 2 && v < 15) {
                return sorted.filter { cardValue(it) == v }.take(2)
            }
        }

        // Play lowest single
        return listOf(sorted.first())
    }

    // AI bid decision
    fun aiDecideBid(hand: List<Card>, callCount: Int): Boolean {
        var score = 0
        val values = hand.map { cardValue(it) }
        if (values.contains(17)) score += 4 // Big joker
        if (values.contains(16)) score += 3 // Small joker
        score += values.count { it == 15 } * 2 // 2s
        score += values.count { it == 14 } // Aces
        val counts = values.groupingBy { it }.eachCount()
        score += counts.values.count { it >= 4 } * 5 // Bombs
        score += counts.values.count { it == 3 } // Triples
        return score >= (8 - callCount * 2)
    }

    // AI double decision
    fun aiDecideDouble(hand: List<Card>, isLandlord: Boolean): Int {
        var score = 0
        val values = hand.map { cardValue(it) }
        if (values.contains(17)) score += 4
        if (values.contains(16)) score += 3
        score += values.count { it == 15 } * 2
        val counts = values.groupingBy { it }.eachCount()
        score += counts.values.count { it >= 4 } * 5
        return if (score >= 10) 2 else if (score >= 6) 1 else 0
    }
}
