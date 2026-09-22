package com.mengmeng.doudizhu.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mengmeng.doudizhu.R
import com.mengmeng.doudizhu.api.ApiClient
import com.mengmeng.doudizhu.game.CardRules
import com.mengmeng.doudizhu.game.PracticeGameEngine
import com.mengmeng.doudizhu.model.*
import com.mengmeng.doudizhu.util.AssetLoader
import com.mengmeng.doudizhu.util.SoundManager

class GameActivity : AppCompatActivity() {

    private lateinit var api: ApiClient
    private lateinit var soundManager: SoundManager
    private lateinit var engine: PracticeGameEngine
    private lateinit var mode: String
    private var selectedCards: MutableSet<String> = mutableSetOf()
    private val handler = Handler(Looper.getMainLooper())
    private var pollRunnable: Runnable? = null
    private var currentState: RoomState? = null

    // Views
    private lateinit var gameBg: ImageView
    private lateinit var handContainer: LinearLayout
    private lateinit var kittyContainer: LinearLayout
    private lateinit var opponent1Cards: TextView
    private lateinit var opponent2Cards: TextView
    private lateinit var opponent1Name: TextView
    private lateinit var opponent2Name: TextView
    private lateinit var myName: TextView
    private lateinit var baseScoreLabel: TextView
    private lateinit var multiplierLabel: TextView
    private lateinit var turnLabel: TextView
    private lateinit var lastPlayLabel: TextView
    private lateinit var resultPanel: LinearLayout
    private lateinit var resultLabel: TextView
    private lateinit var resultCoins: TextView
    private lateinit var waitingView: LinearLayout
    private lateinit var actionBar: LinearLayout
    private lateinit var bidCallBtn: Button
    private lateinit var bidNoCallBtn: Button
    private lateinit var bidRobBtn: Button
    private lateinit var bidNoRobBtn: Button
    private lateinit var doubleBtn: Button
    private lateinit var doubleNoBtn: Button
    private lateinit var doubleSuperBtn: Button
    private lateinit var playBtn: Button
    private lateinit var passBtn: Button
    private lateinit var hintBtn: Button
    private lateinit var reselectBtn: Button
    private lateinit var startBtn: Button
    private lateinit var againBtn: Button
    private lateinit var homeBtn: Button
    private lateinit var leaveBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        api = ApiClient.getInstance(this)
        soundManager = SoundManager(this).apply {
            soundEnabled = api.isSoundEnabled
            musicEnabled = api.isMusicEnabled
        }
        mode = intent.getStringExtra("mode") ?: "practice"
        engine = PracticeGameEngine()

        initViews()
        setupButtons()

        if (mode == "practice") {
            startPracticeGame()
        } else {
            startOnlineGame()
        }
    }

    private fun initViews() {
        gameBg = findViewById(R.id.gameBg)
        val bgRes = if (api.isNightTheme) "game-night.png" else "game-day.png"
        AssetLoader.loadDrawable(this, bgRes)?.let { gameBg.setImageDrawable(it) }

        handContainer = findViewById(R.id.handContainer)
        kittyContainer = findViewById(R.id.kittyContainer)
        opponent1Cards = findViewById(R.id.opponent1Cards)
        opponent2Cards = findViewById(R.id.opponent2Cards)
        opponent1Name = findViewById(R.id.opponent1Name)
        opponent2Name = findViewById(R.id.opponent2Name)
        myName = findViewById(R.id.myName)
        baseScoreLabel = findViewById(R.id.baseScoreLabel)
        multiplierLabel = findViewById(R.id.multiplierLabel)
        turnLabel = findViewById(R.id.turnLabel)
        lastPlayLabel = findViewById(R.id.lastPlayLabel)
        resultPanel = findViewById(R.id.resultPanel)
        resultLabel = findViewById(R.id.resultLabel)
        resultCoins = findViewById(R.id.resultCoins)
        waitingView = findViewById(R.id.waitingView)
        actionBar = findViewById(R.id.actionBar)

        bidCallBtn = findViewById(R.id.bidCallBtn)
        bidNoCallBtn = findViewById(R.id.bidNoCallBtn)
        bidRobBtn = findViewById(R.id.bidRobBtn)
        bidNoRobBtn = findViewById(R.id.bidNoRobBtn)
        doubleBtn = findViewById(R.id.doubleBtn)
        doubleNoBtn = findViewById(R.id.doubleNoBtn)
        doubleSuperBtn = findViewById(R.id.doubleSuperBtn)
        playBtn = findViewById(R.id.playBtn)
        passBtn = findViewById(R.id.passBtn)
        hintBtn = findViewById(R.id.hintBtn)
        reselectBtn = findViewById(R.id.reselectBtn)
        startBtn = findViewById(R.id.startBtn)
        againBtn = findViewById(R.id.againBtn)
        homeBtn = findViewById(R.id.homeBtn)
        leaveBtn = findViewById(R.id.leaveBtn)

        // Load button images
        mapOf(
            bidCallBtn to "components/bid-call.png",
            bidNoCallBtn to "components/bid-no-call.png",
            bidRobBtn to "components/bid-rob.png",
            bidNoRobBtn to "components/bid-no-rob.png",
            doubleBtn to "components/double.png",
            doubleNoBtn to "components/double-no.png",
            doubleSuperBtn to "components/double-super.png",
            playBtn to "components/play.png",
            passBtn to "components/pass.png",
            hintBtn to "components/hint.png",
            reselectBtn to "components/reselect.png",
            startBtn to "components/start-game.png",
            againBtn to "components/result-again.png",
            homeBtn to "components/result-home.png",
            leaveBtn to "components/logout.png"
        ).forEach { (btn, path) ->
            AssetLoader.loadDrawable(this, path)?.let { btn.setBackgroundDrawable(it) }
        }
    }

    private fun setupButtons() {
        bidCallBtn.setOnClickListener { sendGameAction("bid_call") }
        bidNoCallBtn.setOnClickListener { sendGameAction("bid_no_call") }
        bidRobBtn.setOnClickListener { sendGameAction("bid_rob") }
        bidNoRobBtn.setOnClickListener { sendGameAction("bid_no_rob") }
        doubleBtn.setOnClickListener { sendGameAction("double") }
        doubleNoBtn.setOnClickListener { sendGameAction("no_double") }
        doubleSuperBtn.setOnClickListener { sendGameAction("super_double") }
        playBtn.setOnClickListener { playSelectedCards() }
        passBtn.setOnClickListener { sendGameAction("pass") }
        hintBtn.setOnClickListener { showHint() }
        reselectBtn.setOnClickListener { selectedCards.clear(); renderHand() }
        startBtn.setOnClickListener {
            if (mode == "practice") startPracticeGame() else sendGameAction("start")
        }
        againBtn.setOnClickListener {
            resultPanel.visibility = View.GONE
            if (mode == "practice") startPracticeGame() else sendGameAction("start")
        }
        homeBtn.setOnClickListener { finish() }
        leaveBtn.setOnClickListener { leaveRoom() }
    }

    private fun startPracticeGame() {
        engine.startGame()
        currentState = engine.getState()
        selectedCards.clear()
        resultPanel.visibility = View.GONE
        waitingView.visibility = View.GONE
        renderState()
        soundManager.playBgm("bgm/game.mp3")
        // If AI starts bidding, process
        scheduleAiTurn()
    }

    private fun startOnlineGame() {
        waitingView.visibility = View.VISIBLE
        api.getActiveRoom { response, error ->
            runOnUiThread {
                if (response?.ok == true && response.data != null) {
                    currentState = response.data
                    waitingView.visibility = View.GONE
                    renderState()
                    startPolling()
                } else {
                    // Try to create/join a room via start action
                    api.sendAction("start") { resp, err ->
                        runOnUiThread {
                            if (resp?.ok == true && resp.data != null) {
                                currentState = resp.data
                                waitingView.visibility = View.GONE
                                renderState()
                                startPolling()
                            } else {
                                Toast.makeText(this, "无法加入房间: ${resp?.message ?: err?.message}", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startPolling() {
        pollRunnable = object : Runnable {
            override fun run() {
                api.getRoomState { response, error ->
                    if (response?.ok == true && response.data != null) {
                        val newState = response.data
                        if (newState.actionVersion != currentState?.actionVersion) {
                            currentState = newState
                            runOnUiThread { renderState() }
                        }
                    }
                }
                handler.postDelayed(this, 1500)
            }
        }
        handler.postDelayed(pollRunnable!!, 1500)
    }

    private fun stopPolling() {
        pollRunnable?.let { handler.removeCallbacks(it) }
        pollRunnable = null
    }

    private fun sendGameAction(action: String, cards: List<Card>? = null) {
        soundManager.playSfx("sounds/button-click.mp3")
        if (mode == "practice") {
            if (engine.playerAction(action, cards)) {
                currentState = engine.getState()
                selectedCards.clear()
                renderState()
                scheduleAiTurn()
            } else {
                Toast.makeText(this, "无效操作", Toast.LENGTH_SHORT).show()
            }
        } else {
            api.sendAction(action, cards = cards) { response, error ->
                runOnUiThread {
                    if (response?.ok == true && response.data != null) {
                        currentState = response.data
                        selectedCards.clear()
                        renderState()
                    } else {
                        Toast.makeText(this, response?.message ?: error?.message ?: "操作失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun playSelectedCards() {
        val hand = getMyHand()
        val cards = hand.filter { it.id in selectedCards }
        if (cards.isEmpty()) {
            Toast.makeText(this, "请选择要出的牌", Toast.LENGTH_SHORT).show()
            return
        }
        val combo = CardRules.analyzeCards(cards)
        if (combo == null) {
            Toast.makeText(this, "牌型不正确", Toast.LENGTH_SHORT).show()
            return
        }
        val lastCombo = currentState?.lastAction?.combo
        if (lastCombo != null && currentState?.lastActionSeat != currentState?.mySeat) {
            if (!CardRules.comboBeatsCombo(combo, lastCombo)) {
                Toast.makeText(this, "压不过上家的牌", Toast.LENGTH_SHORT).show()
                return
            }
        }
        sendGameAction("play", cards)
    }

    private fun showHint() {
        val hand = getMyHand()
        val lastCombo = currentState?.lastAction?.combo
        val hint = CardRules.findBeatingPlay(hand, lastCombo)
        if (hint != null) {
            selectedCards.clear()
            selectedCards.addAll(hint.map { it.id ?: "" })
            renderHand()
        } else {
            Toast.makeText(this, "没有能出的牌", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleAiTurn() {
        handler.postDelayed({
            if (mode == "practice" && !engine.isMyTurn() && engine.phase != PracticeGameEngine.Phase.ENDED) {
                if (engine.aiTurn()) {
                    currentState = engine.getState()
                    renderState()
                    if (engine.phase != PracticeGameEngine.Phase.ENDED && !engine.isMyTurn()) {
                        scheduleAiTurn()
                    }
                }
            }
        }, 800)
    }

    private fun getMyHand(): List<Card> {
        if (mode == "practice") return engine.getMyHand()
        return currentState?.players?.find { it.seat == currentState?.mySeat }?.handCards ?: emptyList()
    }

    private fun renderState() {
        val state = currentState ?: return
        val mySeat = state.mySeat
        val players = state.players ?: emptyList()
        val me = players.find { it.seat == mySeat }
        val opp1 = players.find { it.seat == (mySeat + 1) % 3 }
        val opp2 = players.find { it.seat == (mySeat + 2) % 3 }

        myName.text = "${me?.nickname ?: me?.name ?: "我"}${if (me?.isLandlord == true) " [地主]" else ""}"
        opponent1Name.text = "${opp1?.nickname ?: opp1?.name ?: "玩家1"}${if (opp1?.isLandlord == true) " [地主]" else ""}"
        opponent2Name.text = "${opp2?.nickname ?: opp2?.name ?: "玩家2"}${if (opp2?.isLandlord == true) " [地主]" else ""}"
        opponent1Cards.text = "${opp1?.cardCount ?: 0}张"
        opponent2Cards.text = "${opp2?.cardCount ?: 0}张"

        baseScoreLabel.text = "底分: ${state.baseScore}"
        multiplierLabel.text = "倍数: ${state.multiplier}"

        // Last play
        val lastAction = state.lastAction
        if (lastAction != null && lastAction.type == "play" && lastAction.cards != null) {
            val comboName = comboDisplayName(lastAction.combo?.type)
            lastPlayLabel.text = "${playerName(lastAction.seat)}: $comboName (${lastAction.cards.joinToString(" ") { CardRules.rankDisplay(it) }})"
        } else if (lastAction != null && lastAction.type == "pass") {
            lastPlayLabel.text = "${playerName(lastAction.seat)}: 不出"
        } else {
            lastPlayLabel.text = ""
        }

        // Turn indicator
        turnLabel.text = if (state.currentSeat == mySeat) "轮到你了" else "等待 ${playerName(state.currentSeat)}"

        // Kitty cards
        kittyContainer.removeAllViews()
        state.kittyCards?.let { cards ->
            for (card in cards) {
                val cv = CardView(this)
                cv.card = card
                cv.cardWidth = 50
                cv.cardHeight = 70
                kittyContainer.addView(cv)
            }
        }

        renderHand()

        // Action buttons visibility
        val phase = state.state
        val isMyTurn = state.currentSeat == mySeat
        hideAllActionButtons()

        when (phase) {
            "waiting" -> {
                waitingView.visibility = View.VISIBLE
                startBtn.visibility = View.VISIBLE
            }
            "bidding" -> {
                waitingView.visibility = View.GONE
                if (isMyTurn) {
                    if (state.landlordSeat >= 0) {
                        bidRobBtn.visibility = View.VISIBLE
                        bidNoRobBtn.visibility = View.VISIBLE
                    } else {
                        bidCallBtn.visibility = View.VISIBLE
                        bidNoCallBtn.visibility = View.VISIBLE
                    }
                }
            }
            "doubling" -> {
                if (isMyTurn) {
                    doubleBtn.visibility = View.VISIBLE
                    doubleNoBtn.visibility = View.VISIBLE
                    doubleSuperBtn.visibility = View.VISIBLE
                }
            }
            "playing" -> {
                if (isMyTurn) {
                    playBtn.visibility = View.VISIBLE
                    hintBtn.visibility = View.VISIBLE
                    reselectBtn.visibility = View.VISIBLE
                    if (state.lastAction != null && state.lastActionSeat != mySeat && state.lastAction?.type != "pass") {
                        passBtn.visibility = View.VISIBLE
                    }
                }
            }
            "ended" -> {
                showResult(state)
            }
        }
    }

    private fun renderHand() {
        handContainer.removeAllViews()
        val hand = getMyHand()
        for (card in hand) {
            val cv = CardView(this)
            cv.card = card
            cv.isChosen = card.id in selectedCards
            cv.cardWidth = 56
            cv.cardHeight = 78
            cv.setOnClickListener {
                if (card.id in selectedCards) selectedCards.remove(card.id)
                else selectedCards.add(card.id ?: "")
                renderHand()
            }
            handContainer.addView(cv)
        }
    }

    private fun hideAllActionButtons() {
        bidCallBtn.visibility = View.GONE
        bidNoCallBtn.visibility = View.GONE
        bidRobBtn.visibility = View.GONE
        bidNoRobBtn.visibility = View.GONE
        doubleBtn.visibility = View.GONE
        doubleNoBtn.visibility = View.GONE
        doubleSuperBtn.visibility = View.GONE
        playBtn.visibility = View.GONE
        passBtn.visibility = View.GONE
        hintBtn.visibility = View.GONE
        reselectBtn.visibility = View.GONE
        startBtn.visibility = View.GONE
    }

    private fun showResult(state: RoomState) {
        resultPanel.visibility = View.VISIBLE
        val mySeat = state.mySeat
        val iWin = state.winner == mySeat || (state.result?.landlordWin == true && state.landlordSeat == mySeat) ||
                (state.result?.landlordWin == false && state.landlordSeat != mySeat)
        resultLabel.text = if (iWin) "胜利!" else "失败"
        val coinChange = state.result?.coinChanges?.get(mySeat.toString()) ?: 0
        resultCoins.text = "金币: ${if (coinChange >= 0) "+" else ""}$coinChange"
        AssetLoader.loadDrawable(this, if (iWin) "components/result-win.png" else "components/result-loss.png")?.let {
            resultLabel.setBackgroundDrawable(it)
        }
        againBtn.visibility = View.VISIBLE
        homeBtn.visibility = View.VISIBLE
        soundManager.playSfx(if (iWin) "win.mp3" else "lose.mp3")
        soundManager.stopBgm()
    }

    private fun playerName(seat: Int): String {
        return currentState?.players?.find { it.seat == seat }?.nickname
            ?: currentState?.players?.find { it.seat == seat }?.name
            ?: "玩家$seat"
    }

    private fun comboDisplayName(type: String?): String = when (type) {
        "single" -> "单张"; "pair" -> "对子"; "triple" -> "三张"
        "triple_single" -> "三带一"; "triple_pair" -> "三带二"
        "straight" -> "顺子"; "pair_straight" -> "连对"
        "airplane" -> "飞机"; "airplane_single" -> "飞机带单"
        "airplane_pair" -> "飞机带对"; "four_two" -> "四带二"
        "four_two_pair" -> "四带两对"; "bomb" -> "炸弹"
        "rocket" -> "王炸"; else -> type ?: ""
    }

    private fun leaveRoom() {
        if (mode == "online") {
            api.leaveRoom { _, _ -> finish() }
        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mode == "practice" && engine.phase == PracticeGameEngine.Phase.PLAYING) {
            soundManager.resumeBgm()
        }
    }

    override fun onPause() {
        super.onPause()
        soundManager.pauseBgm()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
        soundManager.release()
    }
}
