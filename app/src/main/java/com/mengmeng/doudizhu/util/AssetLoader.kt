package com.mengmeng.doudizhu.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import java.io.IOException

object AssetLoader {
    private var cache: MutableMap<String, Bitmap> = mutableMapOf()

    fun loadBitmap(context: Context, path: String): Bitmap? {
        cache[path]?.let { return it }
        return try {
            context.assets.open(path).use {
                val bmp = BitmapFactory.decodeStream(it)
                if (bmp != null) cache[path] = bmp
                bmp
            }
        } catch (e: IOException) {
            null
        }
    }

    fun loadDrawable(context: Context, path: String): Drawable? {
        val bmp = loadBitmap(context, path) ?: return null
        return BitmapDrawable(context.resources, bmp)
    }

    fun loadScaledBitmap(context: Context, path: String, maxWidth: Int, maxHeight: Int): Bitmap? {
        val bmp = loadBitmap(context, path) ?: return null
        if (bmp.width <= maxWidth && bmp.height <= maxHeight) return bmp
        val scale = minOf(maxWidth.toFloat() / bmp.width, maxHeight.toFloat() / bmp.height)
        return Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true)
    }

    fun clearCache() {
        cache.values.forEach { it.recycle() }
        cache.clear()
    }
}

class SoundManager(private val context: Context) {
    private var bgPlayer: MediaPlayer? = null
    private var sfxPlayers: MutableList<MediaPlayer> = mutableListOf()
    var soundEnabled: Boolean = true
    var musicEnabled: Boolean = true

    fun playSfx(path: String) {
        if (!soundEnabled) return
        try {
            val mp = MediaPlayer()
            context.assets.openFd(path).use { afd ->
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
            mp.prepare()
            mp.setOnCompletionListener {
                mp.release()
                sfxPlayers.remove(mp)
            }
            mp.start()
            sfxPlayers.add(mp)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun playBgm(path: String) {
        if (!musicEnabled) {
            stopBgm()
            return
        }
        try {
            stopBgm()
            bgPlayer = MediaPlayer()
            context.assets.openFd(path).use { afd ->
                bgPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
            bgPlayer?.isLooping = true
            bgPlayer?.setVolume(0.5f, 0.5f)
            bgPlayer?.prepare()
            bgPlayer?.start()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun stopBgm() {
        bgPlayer?.stop()
        bgPlayer?.release()
        bgPlayer = null
    }

    fun pauseBgm() {
        bgPlayer?.pause()
    }

    fun resumeBgm() {
        if (musicEnabled) bgPlayer?.start()
    }

    fun release() {
        stopBgm()
        sfxPlayers.forEach { it.release() }
        sfxPlayers.clear()
    }
}
