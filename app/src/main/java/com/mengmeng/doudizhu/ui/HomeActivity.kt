package com.mengmeng.doudizhu.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mengmeng.doudizhu.R
import com.mengmeng.doudizhu.api.ApiClient
import com.mengmeng.doudizhu.util.AssetLoader
import com.mengmeng.doudizhu.util.SoundManager

class HomeActivity : AppCompatActivity() {

    private lateinit var api: ApiClient
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        api = ApiClient.getInstance(this)
        soundManager = SoundManager(this).apply {
            soundEnabled = api.isSoundEnabled
            musicEnabled = api.isMusicEnabled
        }

        val bg = findViewById<ImageView>(R.id.homeBg)
        val bgRes = if (api.isNightTheme) "home-night.png" else "home-day.png"
        AssetLoader.loadDrawable(this, bgRes)?.let { bg.setImageDrawable(it) }

        val logo = findViewById<ImageView>(R.id.homeLogo)
        AssetLoader.loadDrawable(this, "logo-primary.png")?.let { logo.setImageDrawable(it) }

        val userPanel = findViewById<ImageView>(R.id.userPanel)
        AssetLoader.loadDrawable(this, "home-user-panel.png")?.let { userPanel.setImageDrawable(it) }

        val userName = findViewById<TextView>(R.id.userName)
        val userCoins = findViewById<TextView>(R.id.userCoins)
        val user = api.currentUser
        userName.text = user?.nickname ?: user?.name ?: user?.email ?: "玩家"
        userCoins.text = "金币: ${user?.coins ?: 0}"

        val practiceBtn = findViewById<Button>(R.id.practiceButton)
        val onlineBtn = findViewById<Button>(R.id.onlineButton)
        val historyBtn = findViewById<Button>(R.id.historyButton)
        val settingsBtn = findViewById<Button>(R.id.settingsButton)

        AssetLoader.loadDrawable(this, "button-practice.png")?.let { practiceBtn.setBackgroundDrawable(it) }
        AssetLoader.loadDrawable(this, "button-online.png")?.let { onlineBtn.setBackgroundDrawable(it) }
        AssetLoader.loadDrawable(this, "button-history.png")?.let { historyBtn.setBackgroundDrawable(it) }
        AssetLoader.loadDrawable(this, "button-settings.png")?.let { settingsBtn.setBackgroundDrawable(it) }

        practiceBtn.setOnClickListener {
            soundManager.playSfx("sounds/button-click.mp3")
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("mode", "practice")
            startActivity(intent)
        }

        onlineBtn.setOnClickListener {
            soundManager.playSfx("sounds/button-click.mp3")
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("mode", "online")
            startActivity(intent)
        }

        historyBtn.setOnClickListener {
            soundManager.playSfx("sounds/button-click.mp3")
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        settingsBtn.setOnClickListener {
            soundManager.playSfx("sounds/button-click.mp3")
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        soundManager.playBgm("bgm/lobby.mp3")
    }

    override fun onPause() {
        super.onPause()
        soundManager.pauseBgm()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
