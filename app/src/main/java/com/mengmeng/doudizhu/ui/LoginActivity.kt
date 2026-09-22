package com.mengmeng.doudizhu.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mengmeng.doudizhu.R
import com.mengmeng.doudizhu.api.ApiClient
import com.mengmeng.doudizhu.util.AssetLoader

class LoginActivity : AppCompatActivity() {

    private lateinit var api: ApiClient
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        api = ApiClient.getInstance(this)

        val bg = findViewById<ImageView>(R.id.loginBg)
        val logo = findViewById<ImageView>(R.id.loginLogo)
        AssetLoader.loadDrawable(this, "login-background.png")?.let { bg.setImageDrawable(it) }
        AssetLoader.loadDrawable(this, "logo-compact.png")?.let { logo.setImageDrawable(it) }

        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入邮箱和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doLogin(email, password)
        }
    }

    private fun doLogin(email: String, password: String) {
        loginButton.isEnabled = false
        loginButton.text = "登录中..."
        api.login(email, password) { response, error ->
            runOnUiThread {
                loginButton.isEnabled = true
                loginButton.text = getString(R.string.login)
                if (error != null) {
                    Toast.makeText(this, "网络错误: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                if (response?.ok == true && response.data != null) {
                    api.sid = response.data.sid
                    api.currentUser = response.data.user
                    Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, response?.message ?: "登录失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
