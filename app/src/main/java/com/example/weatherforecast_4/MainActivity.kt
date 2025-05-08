package com.example.weatherforecast_4

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import com.bumptech.glide.Glide
import com.example.weatherforecast_4.retrofit.ForecastItem
import com.example.weatherforecast_4.retrofit.Main
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()

    private lateinit var backgroundImageView: ImageView
    private lateinit var searchButton: ImageButton
    private lateinit var cityTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var weatherIconImageView: ImageView
    private lateinit var descriptionTextView: TextView
    private lateinit var feelsLikeTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var windTextView: TextView
    private lateinit var pressureTextView: TextView
    private lateinit var hourlyWeatherTextView: TextView
    private lateinit var hourlyForecastRecyclerView: RecyclerView
    private lateinit var fiveDayWeatherTextView: TextView
    private lateinit var forecastRecyclerView: RecyclerView

    private lateinit var itemHourlyForecastProgressBar: ProgressBar
    private lateinit var itemForecastProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefsStartActivity = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = prefsStartActivity.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch) {
            // Первый запуск
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        } else {
            // Не первый запуск
            setContentView(R.layout.activity_main)
            backgroundImageView = findViewById(R.id.backgroundImageView)
            searchButton = findViewById(R.id.btnSearchMain)
            cityTextView = findViewById(R.id.cityTextView)
            dateTextView = findViewById(R.id.dateTextView)
            temperatureTextView = findViewById(R.id.temperatureTextView)
            weatherIconImageView = findViewById(R.id.weatherIconImageView)
            descriptionTextView = findViewById(R.id.descriptionTextView)
            feelsLikeTextView = findViewById(R.id.feelsLikeTextView)
            humidityTextView = findViewById(R.id.humidityTextView)
            windTextView = findViewById(R.id.windTextView)
            pressureTextView = findViewById(R.id.pressureTextView)
            hourlyWeatherTextView = findViewById(R.id.hourlyWeatherTextView)
            hourlyForecastRecyclerView = findViewById(R.id.hourlyForecastRecyclerView)
            fiveDayWeatherTextView = findViewById(R.id.fiveDayWeatherTextView)
            forecastRecyclerView = findViewById(R.id.forecastRecyclerView)

            itemHourlyForecastProgressBar = findViewById(R.id.itemHourlyForecastProgressBar)
            itemForecastProgressBar = findViewById(R.id.itemForecastProgressBar)

            hourlyForecastRecyclerView.layoutManager = LinearLayoutManager(this)
            forecastRecyclerView.layoutManager = LinearLayoutManager(this)

            // Наблюдение за сменой фона
            weatherViewModel.backgroundImageResId.observe(this) { resId ->
                backgroundImageView.setImageResource(resId)
            }

            searchButton.setOnClickListener {
                startActivity(Intent(this, SearchActivity::class.java))
                finish()
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

            // ProgressBar
            weatherViewModel.isLoading.observe(this) { isLoading ->
                itemHourlyForecastProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                itemForecastProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            weatherViewModel.weather.observe(this) { weather ->
                weather?.let {
                    // Получаем смещение часового пояса города в секундах
                    val timezoneOffset = it.timezone

                    // Создаём объект Calendar с текущим временем устройства
                    val calendar = Calendar.getInstance()

                    // Получаем смещение часового пояса устройства в секундах
                    val deviceOffset = calendar.timeZone.rawOffset / 1000

                    // Вычисляем разницу для корректировки
                    val localOffset = timezoneOffset - deviceOffset

                    // Корректируем время
                    calendar.add(Calendar.SECOND, localOffset)

                    // Форматируем дату
                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val currentDate = sdf.format(calendar.time)
                    dateTextView.text = "Сегодня: $currentDate"

                    cityTextView.text = it.name
                    temperatureTextView.text = "${it.main.temp.roundToInt()}°С"
                    val iconUrl = "https://openweathermap.org/img/wn/${it.weather[0].icon}@2x.png"
                    Glide.with(this@MainActivity)
                        .load(iconUrl)
                        .into(weatherIconImageView)
                    descriptionTextView.text = it.weather[0].description
                    feelsLikeTextView.text = "Ощущается как: ${it.main.feelsLike.roundToInt()}°С"
                    humidityTextView.text = "Влажность\n${it.main.humidity}%"
                    windTextView.text = "Сила ветра\n${it.wind.speed} м/с"
                    pressureTextView.text = "Давление\n${it.main.pressure} гПа"
                }
            }

            weatherViewModel.hourlyForecast.observe(this) { hourlyList ->
                hourlyList?.let {
                    val adapter = HourlyForecastAdapter(it)
                    hourlyWeatherTextView.text = "Прогноз на текущий день"
                    hourlyForecastRecyclerView.adapter = adapter
                } ?: run {
                    hourlyForecastRecyclerView.adapter = HourlyForecastAdapter(emptyList())
                }
            }

            weatherViewModel.forecast.observe(this) { forecast ->
                forecast?.let {
                    val adapter = ForecastAdapter(it)
                    fiveDayWeatherTextView.text = "Прогноз на 5 дней"
                    forecastRecyclerView.adapter = adapter
                } ?: run {
                    forecastRecyclerView.adapter = ForecastAdapter(emptyList())
                }
            }

            weatherViewModel.error.observe(this) { error ->
                error?.let {
                    cityTextView.text = "Город не найден"
                    temperatureTextView.text = ""
                    descriptionTextView.text = "Нет подключения к интернету"
                    humidityTextView.text = ""
                    windTextView.text = ""
                    pressureTextView.text = ""
                    hourlyWeatherTextView.text = "Прогноз на текущий день"
                    fiveDayWeatherTextView.text = "Прогноз на 5 дней"
                    val placeholderList = List(5) { index ->
                        ForecastItem(
                            dt = 0L,
                            main = Main(temp = 0f, feelsLike = 0f, humidity = 0, pressure = 0),
                            weather = emptyList()
                        )
                    }
                    val dailyholderList = List(8) { index ->
                        ForecastItem(
                            dt = 0L,
                            main = Main(temp = 0f, feelsLike = 0f, humidity = 0, pressure = 0),
                            weather = emptyList()
                        )
                    }
                    hourlyForecastRecyclerView.adapter = HourlyForecastAdapter(dailyholderList)
                    forecastRecyclerView.adapter = ForecastAdapter(placeholderList)

                    itemHourlyForecastProgressBar.visibility = View.GONE
                    itemForecastProgressBar.visibility = View.GONE
                }
            }
        }
    }
}