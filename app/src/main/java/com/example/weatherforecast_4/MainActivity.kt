package com.example.weatherforecast_4

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import com.bumptech.glide.Glide

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()

    private lateinit var cityTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var weatherIconImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityTextView = findViewById(R.id.cityTextView)
        temperatureTextView = findViewById(R.id.temperatureTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        weatherIconImageView = findViewById(R.id.weatherIconImageView)

        weatherViewModel.weather.observe(this) { weather ->
            weather?.let {
                cityTextView.text = it.name
                temperatureTextView.text = "${it.main.temp}°"
                descriptionTextView.text = it.weather[0].description
                val iconUrl = "https://openweathermap.org/img/wn/${it.weather[0].icon}@2x.png"
                Glide.with(this@MainActivity)
                    .load(iconUrl)
                    .into(weatherIconImageView)
            }
        }

        weatherViewModel.error.observe(this) { error ->
            error?.let {
                cityTextView.text = "Город не найден"
                temperatureTextView.text = "Не удалось загрузить данные"
                descriptionTextView.text = it
            }
        }
    }
}