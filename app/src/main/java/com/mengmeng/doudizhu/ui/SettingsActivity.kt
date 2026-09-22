package com.mengmeng.doudizhu.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mengmeng.doudizhu.R
import com.mengmeng.doudizhu.api.ApiClient

class SettingsActivity : AppCompatActivity() {

    private lateinit var api: ApiClient
    private lateinit var soundSwitch: Switch
    private lateinit var musicSwitch: Switch
    private lateinit var themeSwitch: Switch
    private lateinit var versionText: TextView
    private lateinit var logoutBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        api = ApiClient.getInstance(this)

        soundSwitch = findViewById(R.id.soundSwitch)
        musicSwitch = findViewById(R.id.musicSwitch)
        themeSwitch = findViewById(R.id.themeSwitch)
        versionText = findViewById(R.id.versionText)
        logoutBtn = findViewById(R.id.logoutButton)

        soundSwitch.isChecked = api.isSoundEnabled
        musicSwitch.isChecked = api.isMusicEnabled
        themeSwitch.isChecked = api.isNightTheme
        versionText.text = "版本 1.0.1"

        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            api.isSoundEnabled = isChecked
        }
        musicSwitch.setOnCheckedChangeListener { _, isChecked ->
            api.isMusicEnabled = isChecked
        }
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            api.isNightTheme = isChecked
            Toast.makeText(this, "主题已切换，重启游戏生效", Toast.LENGTH_SHORT).show()
        }

        logoutBtn.setOnClickListener {
            api.logout { _, _ ->
                runOnUiThread {
                    api.clearSession()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
