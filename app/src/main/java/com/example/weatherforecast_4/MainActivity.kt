package com.example.weatherforecast_4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherforecast_4.retrofit.CurrentWeather
import com.example.weatherforecast_4.retrofit.WeatherApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Inject
import com.bumptech.glide.Glide

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    val weatherViewModel: WeatherViewModel by viewModels()

    private lateinit var cityTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var weatherIconImageView: ImageView

    private val apiKey = "3a40caaed30624dd3ed13790e371b4bd"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityTextView = findViewById(R.id.cityTextView)
        temperatureTextView = findViewById(R.id.temperatureTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        weatherIconImageView = findViewById(R.id.weatherIconImageView)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val weatherApi = retrofit.create(WeatherApi::class.java)

        // Использование Coroutine для фонового потока
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val weather = weatherApi.getCurrentWeather(
                    city = "Orel",
                    apiKey = apiKey,
                    lang = "ru"
                )
                runOnUiThread {
                    cityTextView.text = weather.name
                    temperatureTextView.text = "${weather.main.temp}°"
                    descriptionTextView.text = weather.weather[0].description
                    val iconUrl = "https://openweathermap.org/img/wn/${weather.weather[0].icon}@2x.png"
                    Glide.with(this@MainActivity)
                        .load(iconUrl)
                        .into(weatherIconImageView)
                }

            } catch (e: Exception) {
                runOnUiThread {
                    cityTextView.text = "Город не найден"
                    temperatureTextView.text = "Не удалось загрузить данные"
                    descriptionTextView.text = e.message
                }
            }
        }
    }
}