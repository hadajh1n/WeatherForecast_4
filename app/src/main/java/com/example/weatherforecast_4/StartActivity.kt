package com.example.weatherforecast_4

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {
    private lateinit var logoStartImageView: ImageView
    private lateinit var descriptionStartTextView: TextView
    private lateinit var buttonStartActivity: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        logoStartImageView = findViewById(R.id.logoImageView)
        descriptionStartTextView = findViewById(R.id.descriptionStartTextView)
        buttonStartActivity = findViewById(R.id.buttonStartActivity)

        // Анимация для иконки (появление + движение вниз)
        val logoAnimSet = AnimationSet(true)
        val logoAlpha = AlphaAnimation(0f, 1f)
        logoAlpha.duration = 2000
        val logoTranslate = TranslateAnimation(0f, 0f, 0f, 20f)
        logoTranslate.duration = 2000
        logoAnimSet.addAnimation(logoAlpha)
        logoAnimSet.addAnimation(logoTranslate)
        logoAnimSet.fillAfter = true

        // Анимация для текста (появление + движение вниз)
        val descAnimSet = AnimationSet(true)
        val descAlpha = AlphaAnimation(0f, 1f)
        descAlpha.duration = 1000
        val descTranslate = TranslateAnimation(0f, 0f, 0f, 20f)
        descTranslate.duration = 1000
        descAnimSet.addAnimation(descAlpha)
        descAnimSet.addAnimation(descTranslate)
        descAnimSet.fillAfter = true

        // Анимация для кнопки (появление + движение вверх)
        val buttonAnimSet = AnimationSet(true)
        val buttonAlpha = AlphaAnimation(0f, 1f)
        buttonAlpha.duration = 1000
        val buttonTranslate = TranslateAnimation(0f, 0f, 0f, -50f)
        buttonTranslate.duration = 1000
        buttonAnimSet.addAnimation(buttonAlpha)
        buttonAnimSet.addAnimation(buttonTranslate)
        buttonAnimSet.fillAfter = true

        // Запуск анимации (logo)
        logoStartImageView.startAnimation(logoAnimSet)
        logoStartImageView.visibility = View.VISIBLE

        // Задержка для текста
        Handler(Looper.getMainLooper()).postDelayed({
            descriptionStartTextView.startAnimation(descAnimSet)
            descriptionStartTextView.visibility = View.VISIBLE
        }, 1000)

        // Задержка для кнопки
        Handler(Looper.getMainLooper()).postDelayed({
            buttonStartActivity.startAnimation(buttonAnimSet)
            buttonStartActivity.visibility = View.VISIBLE
        }, 3000)


        val prefs = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        val isCitySelected = prefs.getBoolean("isCitySelected", false)

        if (isCitySelected) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        buttonStartActivity.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
        }
    }
}