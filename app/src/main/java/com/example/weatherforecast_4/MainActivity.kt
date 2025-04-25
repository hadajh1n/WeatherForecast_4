package com.example.weatherforecast_4

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import com.bumptech.glide.Glide
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()

    private lateinit var backgroundImageView: ImageView
    private lateinit var searchButton: ImageButton
    private lateinit var cityTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var weatherIconImageView: ImageView
    private lateinit var descriptionTextView: TextView
    private lateinit var feelsLikeTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var windTextView: TextView
    private lateinit var pressureTextView: TextView
    private lateinit var forecastRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        backgroundImageView = findViewById(R.id.backgroundImageView)
        searchButton = findViewById(R.id.btnSearchMain)
        cityTextView = findViewById(R.id.cityTextView)
        temperatureTextView = findViewById(R.id.temperatureTextView)
        weatherIconImageView = findViewById(R.id.weatherIconImageView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        feelsLikeTextView = findViewById(R.id.feelsLikeTextView)
        humidityTextView = findViewById(R.id.humidityTextView)
        windTextView = findViewById(R.id.windTextView)
        pressureTextView = findViewById(R.id.pressureTextView)
        forecastRecyclerView = findViewById(R.id.forecastRecyclerView)

        forecastRecyclerView.layoutManager = LinearLayoutManager(this)

        // Наблюдение за сменой фона
        weatherViewModel.backgroundImageResId.observe(this) { resId ->
            backgroundImageView.setImageResource(resId)
        }

        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // Проверка сохраненного города
        val prefs = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        val savedCity = prefs.getString("last_city", null)
        if (savedCity == null) {
            // Остаемся на MainActivity
        } else {
            val selectedCity = intent.getStringExtra("SELECTED_CITY") ?: savedCity
            weatherViewModel.fetchWeather(selectedCity)
            weatherViewModel.loadCachedForecast()
        }

        weatherViewModel.weather.observe(this) { weather ->
            weather?.let {
                cityTextView.text = it.name
                temperatureTextView.text = "${it.main.temp.roundToInt()}°"
                val iconUrl = "https://openweathermap.org/img/wn/${it.weather[0].icon}@2x.png"
                Glide.with(this@MainActivity)
                    .load(iconUrl)
                    .into(weatherIconImageView)
                descriptionTextView.text = it.weather[0].description
                feelsLikeTextView.text = "Ощущается как: ${it.main.feelsLike.roundToInt()}°"
                humidityTextView.text = "Влажность\n${it.main.humidity}%"
                windTextView.text = "Сила ветра\n${it.wind.speed} м/с"
                pressureTextView.text = "Давление\n${it.main.pressure} гПа"
            }
        }

        weatherViewModel.forecast.observe(this) { forecast ->
            forecast?.let {
                val adapter = ForecastAdapter(it)
                forecastRecyclerView.adapter = adapter
            } ?: run {
                forecastRecyclerView.adapter = ForecastAdapter(emptyList())
            }
        }

        weatherViewModel.error.observe(this) { error ->
            error?.let {
                cityTextView.text = "Город не найден"
                temperatureTextView.text = "Не удалось загрузить данные"
                descriptionTextView.text = it
                humidityTextView.text = ""
                windTextView.text = ""
                pressureTextView.text = ""
                forecastRecyclerView.adapter = ForecastAdapter(emptyList())
            }
        }
    }
}