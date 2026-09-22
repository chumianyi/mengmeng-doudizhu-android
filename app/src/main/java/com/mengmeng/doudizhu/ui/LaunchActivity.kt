package com.mengmeng.doudizhu.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mengmeng.doudizhu.R
import com.mengmeng.doudizhu.api.ApiClient
import com.mengmeng.doudizhu.util.AssetLoader

class LaunchActivity : AppCompatActivity() {

    private lateinit var api: ApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        api = ApiClient.getInstance(this)

        val logo = findViewById<ImageView>(R.id.launchLogo)
        val progress = findViewById<ProgressBar>(R.id.launchProgress)

        AssetLoader.loadDrawable(this, "logo-primary.png")?.let { logo.setImageDrawable(it) }

        // Check version first
        api.checkVersion { response, error ->
            runOnUiThread {
                if (error != null) {
                    // Network error, try to proceed with cached session
                    checkSession()
                    return@runOnUiThread
                }
                if (response?.ok == true) {
                    val version = response.data
                    if (version?.version != null && version.version != "1.0.1") {
                        Toast.makeText(this, "发现新版本: ${version.version}\n${version.updateContent ?: ""}", Toast.LENGTH_LONG).show()
                    }
                }
                checkSession()
            }
        }
    }

    private fun checkSession() {
        if (api.sid.isNullOrEmpty()) {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, 1500)
            return
        }

        api.restoreSession { response, error ->
            runOnUiThread {
                if (response?.ok == true && response.data != null) {
                    api.currentUser = response.data
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }, 1000)
                } else {
                    api.clearSession()
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }, 1000)
                }
            }
        }
    }
}
