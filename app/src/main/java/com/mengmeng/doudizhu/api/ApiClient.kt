package com.mengmeng.doudizhu.api

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mengmeng.doudizhu.model.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiClient private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    var sid: String?
        get() = prefs.getString(KEY_SID, null)
        set(value) {
            prefs.edit().putString(KEY_SID, value).apply()
        }

    var currentUser: User?
        get() {
            val json = prefs.getString(KEY_USER, null) ?: return null
            return try { gson.fromJson(json, User::class.java) } catch (e: Exception) { null }
        }
        set(value) {
            prefs.edit().putString(KEY_USER, value?.let { gson.toJson(it) }).apply()
        }

    var isNightTheme: Boolean
        get() = prefs.getBoolean(KEY_NIGHT_THEME, false)
        set(value) = prefs.edit().putBoolean(KEY_NIGHT_THEME, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC, true)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC, value).apply()

    fun clearSession() {
        prefs.edit().remove(KEY_SID).remove(KEY_USER).apply()
    }

    private fun buildUrl(path: String): String = "$BASE_URL$path"

    private fun buildRequest(path: String, method: String, body: RequestBody? = null, includeSid: Boolean = true): Request {
        val builder = Request.Builder().url(buildUrl(path))
        if (includeSid && sid != null) {
            builder.addHeader("X-App-Sid", sid!!)
        }
        builder.addHeader("Accept", "application/json")
        builder.addHeader("User-Agent", "MengmengDoudizhu-Android/1.0.1")
        when (method) {
            "GET" -> builder.get()
            "POST" -> builder.post(body ?: "{}".toRequestBody(JSON))
            "PUT" -> builder.put(body ?: "{}".toRequestBody(JSON))
            "DELETE" -> builder.delete()
        }
        return builder.build()
    }

    private fun <T> executeRequest(request: Request, typeToken: TypeToken<ApiResponse<T>>, callback: (ApiResponse<T>?, IOException?) -> Unit) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }
            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string() ?: ""
                    val result = gson.fromJson<ApiResponse<T>>(body, typeToken.type)
                    callback(result, null)
                } catch (e: Exception) {
                    callback(null, IOException("Parse error: ${e.message}"))
                }
            }
        })
    }

    // Auth
    fun login(email: String, password: String, callback: (ApiResponse<LoginData>?, IOException?) -> Unit) {
        val body = gson.toJson(LoginRequest(email, password)).toRequestBody(JSON)
        val request = buildRequest("login", "POST", body, includeSid = false)
        executeRequest(request, object : TypeToken<ApiResponse<LoginData>>() {}, callback)
    }

    fun logout(callback: (ApiResponse<Any>?, IOException?) -> Unit) {
        val request = buildRequest("logout", "POST", includeSid = true)
        executeRequest(request, object : TypeToken<ApiResponse<Any>>() {}, callback)
    }

    fun checkVersion(callback: (ApiResponse<VersionInfo>?, IOException?) -> Unit) {
        val request = buildRequest("version/check", "GET", includeSid = false)
        executeRequest(request, object : TypeToken<ApiResponse<VersionInfo>>() {}, callback)
    }

    fun restoreSession(callback: (ApiResponse<User>?, IOException?) -> Unit) {
        val request = buildRequest("me", "GET", includeSid = true)
        executeRequest(request, object : TypeToken<ApiResponse<User>>() {}, callback)
    }

    // Room
    fun getActiveRoom(callback: (ApiResponse<RoomState>?, IOException?) -> Unit) {
        val request = buildRequest("room/active", "GET", includeSid = true)
        executeRequest(request, object : TypeToken<ApiResponse<RoomState>>() {}, callback)
    }

    fun getRoomState(callback: (ApiResponse<RoomState>?, IOException?) -> Unit) {
        val request = buildRequest("room/state", "GET", includeSid = true)
        executeRequest(request, object : TypeToken<ApiResponse<RoomState>>() {}, callback)
    }

    fun sendAction(action: String, extra: String? = null, cards: List<Card>? = null, callback: (ApiResponse<RoomState>?, IOException?) -> Unit) {
        val body = gson.toJson(RoomActionRequest(action, extra, cards)).toRequestBody(JSON)
        val request = buildRequest("room/action", "POST", body, includeSid = true)
        executeRequest(request, object : TypeToken<ApiResponse<RoomState>>() {}, callback)
    }

    fun leaveRoom(callback: (ApiResponse<Any>?, IOException?) -> Unit) {
        val request = buildRequest("room/leave", "POST", includeSid = true)
        executeRequest(request, object : TypeToken<ApiResponse<Any>>() {}, callback)
    }

    // History
    fun getHistory(page: Int = 1, callback: (ApiResponse<HistoryData>?, IOException?) -> Unit) {
        val request = buildRequest("game/history?page=$page", "GET", includeSid = true)
        executeRequest(request, object : TypeToken<ApiResponse<HistoryData>>() {}, callback)
    }

    companion object {
        private const val BASE_URL = "https://ddz.oldipa.com/api/app/v1/"
        private const val PREFS_NAME = "mmddz_prefs"
        private const val KEY_SID = "MMSID"
        private const val KEY_USER = "MMUser"
        private const val KEY_NIGHT_THEME = "MMNightTheme"
        private const val KEY_SOUND = "MMSoundEnabled"
        private const val KEY_MUSIC = "MMMusicEnabled"
        private val JSON = "application/json; charset=utf-8".toMediaType()

        @Volatile
        private var instance: ApiClient? = null

        fun getInstance(context: Context): ApiClient {
            return instance ?: synchronized(this) {
                instance ?: ApiClient(context.applicationContext).also { instance = it }
            }
        }
    }
}
