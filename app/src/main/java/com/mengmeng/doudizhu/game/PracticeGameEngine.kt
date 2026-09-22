package com.mengmeng.doudizhu.game

import com.mengmeng.doudizhu.model.*

class PracticeGameEngine {

    enum class Phase { WAITING, BIDDING, DOUBLING, DEALING, PLAYING, ENDED }

    var phase: Phase = Phase.WAITING
    var players: MutableList<Player> = mutableListOf()
    var kittyCards: List<Card> = listOf()
    var landlordSeat: Int = -1
    var currentSeat: Int = 0
    var lastAction: GameAction? = null
    var lastActionSeat: Int = -1
    var passCount: Int = 0
    var baseScore: Int = 1
    var multiplier: Int = 1
    var mySeat: Int = 0
    var bidCount: Int = 0
    var doubles: IntArray = intArrayOf(0, 0, 0)
    var winner: Int = -1
    var actionVersion: Int = 0
    private var deck: List<Card> = listOf()

    fun startGame() {
        deck = CardRules.shuffle(CardRules.createDeck())
        players = mutableListOf(
            Player(seat = 0, name = "我", isBot = false, cardCount = 17),
            Player(seat = 1, name = "机器人A", isBot = true, cardCount = 17),
            Player(seat = 2, name = "机器人B", isBot = true, cardCount = 17)
        )
        // Deal cards
        for (i in 0..2) {
            players[i] = players[i].copy(handCards = deck.subList(i * 17, (i + 1) * 17).sortedBy { CardRules.cardValue(it) })
        }
        kittyCards = deck.subList(51, 54)
        phase = Phase.BIDDING
        currentSeat = (0..2).random()
        landlordSeat = -1
        lastAction = null
        lastActionSeat = -1
        passCount = 0
        bidCount = 0
        multiplier = 1
        doubles = intArrayOf(0, 0, 0)
        winner = -1
        actionVersion = 0
    }

    fun getState(): RoomState {
        return RoomState(
            roomId = "practice_${System.currentTimeMillis()}",
            state = phase.name.lowercase(),
            mode = "practice",
            baseScore = baseScore,
            multiplier = multiplier,
            currentSeat = currentSeat,
            landlordSeat = landlordSeat,
            mySeat = mySeat,
            players = players.map { it.copy(handCards = if (it.seat == mySeat) it.handCards else null) },
            kittyCards = if (phase == Phase.PLAYING || phase == Phase.ENDED) kittyCards else null,
            lastAction = lastAction,
            lastActionSeat = lastActionSeat,
            passCount = passCount,
            winner = winner,
            result = if (phase == Phase.ENDED) buildResult() else null,
            actionVersion = actionVersion,
            doubles = mapOf("0" to doubles[0], "1" to doubles[1], "2" to doubles[2])
        )
    }

    private fun buildResult(): GameResult {
        val landlordWin = winner == landlordSeat
        val coinChanges = mutableMapOf<String, Long>()
        val scoreChanges = mutableMapOf<String, Long>()
        val winAmount = baseScore.toLong() * multiplier
        for (p in players) {
            val isWinnerSide = if (landlordWin) p.seat == landlordSeat else p.seat != landlordSeat
            val amount = if (isWinnerSide) winAmount else -winAmount
            val mult = if (p.seat == landlordSeat) 2 else 1
            coinChanges[p.seat.toString()] = amount * mult
            scoreChanges[p.seat.toString()] = amount * mult
        }
        return GameResult(
            winner = winner,
            landlordWin = landlordWin,
            baseScore = baseScore,
            multiplier = multiplier,
            coinChanges = coinChanges,
            scoreChanges = scoreChanges
        )
    }

    fun playerAction(action: String, cards: List<Card>? = null, extra: String? = null): Boolean {
        actionVersion++
        return when (phase) {
            Phase.BIDDING -> handleBid(action)
            Phase.DOUBLING -> handleDouble(action)
            Phase.PLAYING -> handlePlay(action, cards)
            else -> false
        }
    }

    private fun handleBid(action: String): Boolean {
        val seat = currentSeat
        if (action == "bid_call") {
            landlordSeat = seat
            bidCount++
            lastAction = GameAction(type = "bid_call", seat = seat)
            lastActionSeat = seat
            passCount = 0
        } else {
            lastAction = GameAction(type = "bid_no_call", seat = seat)
            lastActionSeat = seat
            passCount++
        }
        currentSeat = (currentSeat + 1) % 3

        // Check if bidding done
        if (landlordSeat >= 0 && passCount >= 2) {
            // Landlord confirmed, give kitty cards
            finishBidding()
        } else if (passCount >= 3 && landlordSeat < 0) {
            // No one called, redeal
            startGame()
        } else if (bidCount >= 3) {
            finishBidding()
        }
        return true
    }

    private fun finishBidding() {
        if (landlordSeat < 0) {
            landlordSeat = currentSeat
        }
        // Give kitty to landlord
        val landlord = players[landlordSeat]
        val newHand = (landlord.handCards ?: emptyList()) + kittyCards
        players[landlordSeat] = landlord.copy(
            handCards = newHand.sortedBy { CardRules.cardValue(it) },
            cardCount = newHand.size,
            isLandlord = true
        )
        phase = Phase.DOUBLING
        currentSeat = landlordSeat
        passCount = 0
        lastAction = null
        lastActionSeat = -1
    }

    private fun handleDouble(action: String): Boolean {
        val seat = currentSeat
        doubles[seat] = when (action) {
            "super_double" -> 2
            "double" -> 1
            else -> 0
        }
        if (doubles[seat] > 0) {
            multiplier *= if (doubles[seat] == 2) 4 else 2
        }
        lastAction = GameAction(type = action, seat = seat)
        lastActionSeat = seat
        currentSeat = (currentSeat + 1) % 3
        passCount++

        if (passCount >= 3) {
            phase = Phase.PLAYING
            currentSeat = landlordSeat
            passCount = 0
            lastAction = null
            lastActionSeat = -1
        }
        return true
    }

    private fun handlePlay(action: String, cards: List<Card>?): Boolean {
        val seat = currentSeat
        if (action == "pass") {
            if (lastAction == null || lastActionSeat == seat) return false
            lastAction = GameAction(type = "pass", seat = seat)
            lastActionSeat = seat
            passCount++
        } else if (action == "play" && cards != null) {
            val combo = CardRules.analyzeCards(cards) ?: return false
            // Validate
            if (lastAction != null && lastAction!!.type != "pass" && lastActionSeat != seat) {
                if (!CardRules.comboBeatsCombo(combo, lastAction!!.combo)) return false
            }
            // Remove cards from hand
            val player = players[seat]
            val hand = player.handCards?.toMutableList() ?: return false
            val cardIds = cards.map { it.id }.toSet()
            hand.removeAll { it.id in cardIds }
            players[seat] = player.copy(handCards = hand.sortedBy { CardRules.cardValue(it) }, cardCount = hand.size)
            lastAction = GameAction(type = "play", seat = seat, cards = cards, combo = combo)
            lastActionSeat = seat
            passCount = 0

            // Check win
            if (hand.isEmpty()) {
                winner = seat
                phase = Phase.ENDED
                return true
            }
        } else {
            return false
        }

        // Check if everyone passed
        if (passCount >= 2) {
            lastAction = null
            lastActionSeat = -1
            passCount = 0
        }

        currentSeat = (currentSeat + 1) % 3
        return true
    }

    // AI takes a turn
    fun aiTurn(): Boolean {
        if (phase != Phase.BIDDING && phase != Phase.DOUBLING && phase != Phase.PLAYING) return false
        val seat = currentSeat
        val player = players[seat]
        if (!player.isBot) return false

        return when (phase) {
            Phase.BIDDING -> {
                val hand = player.handCards ?: emptyList()
                val shouldCall = CardRules.aiDecideBid(hand, bidCount)
                playerAction(if (shouldCall) "bid_call" else "bid_no_call")
            }
            Phase.DOUBLING -> {
                val hand = player.handCards ?: emptyList()
                val d = CardRules.aiDecideDouble(hand, seat == landlordSeat)
                playerAction(when (d) { 2 -> "super_double"; 1 -> "double"; else -> "no_double" })
            }
            Phase.PLAYING -> {
                val hand = player.handCards ?: emptyList()
                val isTeammateLast = lastActionSeat >= 0 && lastActionSeat != landlordSeat && seat != landlordSeat && lastActionSeat != seat
                val play = CardRules.aiDecidePlay(hand, lastAction?.combo, seat == landlordSeat, isTeammateLast)
                if (play != null) {
                    playerAction("play", play)
                } else {
                    playerAction("pass")
                }
            }
            else -> false
        }
    }

    fun isMyTurn(): Boolean = currentSeat == mySeat

    fun getMyHand(): List<Card> = players[mySeat].handCards ?: emptyList()
}
