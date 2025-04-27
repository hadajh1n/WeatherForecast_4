package com.example.weatherforecast_4

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat

class StrokedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val strokePaint = Paint(paint).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f // Толщина обводки
        color = ContextCompat.getColor(context, android.R.color.black) // Цвет обводки
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        // Рисуем текст с обводкой
        val text = text.toString()
        if (text.isNotEmpty()) {
            canvas.drawText(text, 0, text.length, 0f, baseline.toFloat(), strokePaint)
        }
        // Рисуем обычный текст поверх
        super.onDraw(canvas)
    }
}