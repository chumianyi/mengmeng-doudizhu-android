package com.mengmeng.doudizhu.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val ok: Boolean = false,
    val code: String? = null,
    val message: String? = null,
    val data: T? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginData(
    val sid: String? = null,
    val sidExpiresIn: Long? = null,
    val sidExpiresAt: Long? = null,
    val user: User? = null
)

data class User(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val avatarURL: String? = null,
    val coins: Long = 0,
    val score: Long = 0,
    val level: Int = 0,
    val exp: Long = 0,
    val rank: Int = 0,
    val winCount: Int = 0,
    val loseCount: Int = 0,
    val vip: Int = 0
)

data class VersionInfo(
    val version: String? = null,
    val updateContent: String? = null,
    val downloadUrl: String? = null
)

data class RoomState(
    val roomId: String? = null,
    val state: String? = null, // waiting, bidding, doubling, playing, ended
    val mode: String? = null, // practice, online
    val baseScore: Int = 1,
    val multiplier: Int = 1,
    val currentSeat: Int = -1,
    val landlordSeat: Int = -1,
    val mySeat: Int = -1,
    val players: List<Player>? = null,
    val kittyCards: List<Card>? = null,
    val lastAction: GameAction? = null,
    val lastActionSeat: Int = -1,
    val passCount: Int = 0,
    val winner: Int = -1,
    val result: GameResult? = null,
    val turnClock: Int = 0,
    val actionVersion: Int = 0,
    val chatEvents: List<ChatEvent>? = null,
    val doubles: Map<String, Int>? = null
)

data class Player(
    val seat: Int = 0,
    val userId: String? = null,
    val name: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val avatarURL: String? = null,
    val coins: Long = 0,
    val score: Long = 0,
    val cardCount: Int = 0,
    val handCards: List<Card>? = null,
    val isLandlord: Boolean = false,
    val isBot: Boolean = false,
    val isReady: Boolean = false,
    val doubleState: String? = null // none, double, super
)

data class Card(
    val id: String? = null,
    val suit: String? = null, // spade, heart, club, diamond, joker
    val rank: String? = null, // 3-10, J, Q, K, A, 2, small_joker, big_joker
    val value: Int = 0
)

data class GameAction(
    val type: String? = null, // bid_call, bid_no_call, bid_rob, bid_no_rob, double, no_double, super_double, play, pass, chat, start, ready
    val seat: Int = -1,
    val cards: List<Card>? = null,
    val combo: Combo? = null,
    val extra: String? = null,
    val timestamp: Long = 0
)

data class Combo(
    val type: String? = null, // single, pair, triple, triple_single, triple_pair, straight, pair_straight, airplane, airplane_single, airplane_pair, four_two, four_two_pair, bomb, rocket, four_pairs
    val main: Int = 0,
    val length: Int = 0,
    val cards: List<Card>? = null
)

data class GameResult(
    val winner: Int = -1,
    val landlordWin: Boolean = false,
    val baseScore: Int = 0,
    val multiplier: Int = 0,
    val coinChanges: Map<String, Long>? = null,
    val scoreChanges: Map<String, Long>? = null
)

data class ChatEvent(
    val id: String? = null,
    val seat: Int = 0,
    val text: String? = null,
    val type: String? = null, // quick, text
    val timestamp: Long = 0
)

data class RoomActionRequest(
    val action: String,
    val extra: String? = null,
    val cards: List<Card>? = null
)

data class HistoryRecord(
    val id: String? = null,
    val roomId: String? = null,
    val mode: String? = null,
    val result: String? = null, // win, lose
    val landlord: String? = null,
    val landlordName: String? = null,
    val winner: String? = null,
    val winnerName: String? = null,
    val baseScore: Int = 0,
    val multiplier: Int = 0,
    val coinChange: Long = 0,
    val scoreChange: Long = 0,
    val players: List<String>? = null,
    val playerNames: List<String>? = null,
    val createdAt: String? = null,
    val finishedAt: String? = null
)

data class HistoryData(
    val list: List<HistoryRecord>? = null,
    val total: Int = 0,
    val page: Int = 0,
    val totalPages: Int = 0,
    val hasMore: Boolean = false
)
