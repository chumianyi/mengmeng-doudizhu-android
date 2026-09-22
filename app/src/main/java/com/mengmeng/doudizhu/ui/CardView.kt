package com.mengmeng.doudizhu.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.mengmeng.doudizhu.game.CardRules
import com.mengmeng.doudizhu.model.Card

class CardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var card: Card? = null
        set(value) {
            field = value
            invalidate()
        }
    var isChosen: Boolean = false
        set(value) {
            field = value
            invalidate()
        }
    var isFaceDown: Boolean = false
        set(value) {
            field = value
            invalidate()
        }
    var cardWidth: Int = 60
    var cardHeight: Int = 84

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.BLACK
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 18f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(cardWidth, cardHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        if (isFaceDown) {
            // Card back
            cardPaint.color = Color.parseColor("#1B5E20")
            canvas.drawRoundRect(0f, 0f, w, h, 6f, 6f, cardPaint)
            cardPaint.color = Color.parseColor("#2E7D32")
            canvas.drawRoundRect(4f, 4f, w - 4f, h - 4f, 4f, 4f, cardPaint)
            // Pattern
            cardPaint.color = Color.parseColor("#4CAF50")
            for (i in 8 until w.toInt() step 10) {
                for (j in 8 until h.toInt() step 10) {
                    canvas.drawCircle(i.toFloat(), j.toFloat(), 2f, cardPaint)
                }
            }
            borderPaint.color = Color.BLACK
            canvas.drawRoundRect(0f, 0f, w, h, 6f, 6f, borderPaint)
            return
        }

        val c = card ?: run {
            cardPaint.color = Color.WHITE
            canvas.drawRoundRect(0f, 0f, w, h, 6f, 6f, cardPaint)
            canvas.drawRoundRect(0f, 0f, w, h, 6f, 6f, borderPaint)
            return
        }

        // Card background
        cardPaint.color = Color.WHITE
        canvas.drawRoundRect(0f, 0f, w, h, 6f, 6f, cardPaint)

        val isRed = CardRules.isRed(c)
        val color = if (isRed) Color.parseColor("#D32F2F") else Color.BLACK
        textPaint.color = color
        smallTextPaint.color = color

        val rank = CardRules.rankDisplay(c)
        val suitSymbol = when (c.suit) {
            "spade" -> "♠"
            "heart" -> "♥"
            "club" -> "♣"
            "diamond" -> "♦"
            "joker" -> if (c.rank == "big_joker") "★" else "☆"
            else -> ""
        }

        // Top left
        canvas.drawText(rank, 4f, 18f, textPaint)
        canvas.drawText(suitSymbol, 4f, 32f, smallTextPaint)

        // Center suit (large)
        if (c.suit == "joker") {
            textPaint.textSize = 22f
            canvas.drawText(if (c.rank == "big_joker") "大王" else "小王", w / 2 - 22f, h / 2 + 8f, textPaint)
            textPaint.textSize = 18f
        } else {
            textPaint.textSize = 28f
            canvas.drawText(suitSymbol, w / 2 - 10f, h / 2 + 10f, textPaint)
            textPaint.textSize = 18f
        }

        // Bottom right (rotated)
        canvas.save()
        canvas.translate(w, h)
        canvas.rotate(180f)
        canvas.drawText(rank, 4f, 18f, textPaint)
        canvas.drawText(suitSymbol, 4f, 32f, smallTextPaint)
        canvas.restore()

        // Border
        borderPaint.color = if (isChosen) Color.parseColor("#FFD700") else Color.BLACK
        borderPaint.strokeWidth = if (isChosen) 4f else 2f
        canvas.drawRoundRect(0f, 0f, w, h, 6f, 6f, borderPaint)

        if (isChosen) {
            // Highlight glow
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor("#40FFD700")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(-2f, -2f, w + 2f, h + 2f, 8f, 8f, glowPaint)
        }
    }
}
