package com.example.weatherforecast_4

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.weatherforecast_4.retrofit.CurrentWeather

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
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
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
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

            // Обновление экрана
            swipeRefreshLayout.setOnRefreshListener {
                refreshData()
            }

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
                weather?.let { updateWeatherUI(it) }
            }

            weatherViewModel.hourlyForecast.observe(this) { hourlyList ->
                hourlyList?.let { updateHourlyForecastUI(it) }
                    ?: run { hourlyForecastRecyclerView.adapter = HourlyForecastAdapter(emptyList()) }
            }

            weatherViewModel.forecast.observe(this) { forecast ->
                forecast?.let { updateFiveDayForecastUI(it) }
                    ?: run { forecastRecyclerView.adapter = ForecastAdapter(emptyList()) }
            }

            weatherViewModel.error.observe(this) { error ->
                error?.let { updateErrorUI() }
            }
        }
    }

    private fun getSavedCity(): String? {
        val prefs = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        return prefs.getString("last_city", null)
    }

    private fun loadWeatherData(city: String) {
        weatherViewModel.fetchWeather(city)
    }

    // Функция обновления UI для текущей погоды
    private fun updateWeatherUI(weather: CurrentWeather) {
        val timezoneOffset = weather.timezone
        val calendar = Calendar.getInstance()
        val deviceOffset = calendar.timeZone.rawOffset / 1000
        val localOffset = timezoneOffset - deviceOffset
        calendar.add(Calendar.SECOND, localOffset)
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDate = sdf.format(calendar.time)
        dateTextView.text = "Сегодня: $currentDate"

        cityTextView.text = weather.name
        temperatureTextView.text = "${weather.main.temp.roundToInt()}°С"
        val iconUrl = "https://openweathermap.org/img/wn/${weather.weather[0].icon}@2x.png"
        Glide.with(this).load(iconUrl).into(weatherIconImageView)
        descriptionTextView.text = weather.weather[0].description
        feelsLikeTextView.text = "Ощущается как: ${weather.main.feelsLike.roundToInt()}°С"
        humidityTextView.text = "Влажность\n${weather.main.humidity}%"
        windTextView.text = "Сила ветра\n${weather.wind.speed} м/с"
        pressureTextView.text = "Давление\n${weather.main.pressure} гПа"
    }

    // Функция обновления UI для почасового прогноза
    private fun updateHourlyForecastUI(hourlyList: List<ForecastItem>) {
        val adapter = HourlyForecastAdapter(hourlyList)
        hourlyWeatherTextView.text = "Прогноз на текущий день"
        hourlyForecastRecyclerView.adapter = adapter
    }

    // Функция обновления UI для прогноза на 5 дней
    private fun updateFiveDayForecastUI(forecast: List<ForecastItem>) {
        val adapter = ForecastAdapter(forecast)
        fiveDayWeatherTextView.text = "Прогноз на 5 дней"
        forecastRecyclerView.adapter = adapter
    }

    // Функция обновления UI при ошибке
    private fun updateErrorUI() {
        cityTextView.text = "Город не найден"
        dateTextView.text = ""
        Glide.with(this).load(R.drawable.no_internet).into(weatherIconImageView)
        temperatureTextView.text = ""
        feelsLikeTextView.text = ""
        descriptionTextView.text = "Нет подключения к интернету"
        humidityTextView.text = ""
        windTextView.text = ""
        pressureTextView.text = ""
        hourlyWeatherTextView.text = "Прогноз на текущий день"
        fiveDayWeatherTextView.text = "Прогноз на 5 дней"
        val placeholderList = List(5) {
            ForecastItem(dt = 0L, main = Main(temp = 0f, feelsLike = 0f, humidity = 0, pressure = 0), weather = emptyList())
        }
        val dailyHolderList = List(8) {
            ForecastItem(dt = 0L, main = Main(temp = 0f, feelsLike = 0f, humidity = 0, pressure = 0), weather = emptyList())
        }
        hourlyForecastRecyclerView.adapter = HourlyForecastAdapter(dailyHolderList)
        forecastRecyclerView.adapter = ForecastAdapter(placeholderList)
        itemHourlyForecastProgressBar.visibility = View.GONE
        itemForecastProgressBar.visibility = View.GONE
    }

    // Функция обновления данных
    private fun refreshData() {
        val savedCity = getSavedCity()
        if (savedCity != null) {
            loadWeatherData(savedCity)
        } else {
            Toast.makeText(this, "Выберите город для обновления", Toast.LENGTH_SHORT).show()
            swipeRefreshLayout.isRefreshing = false
        }

        // Обновление UI на основе текущих значений в ViewModel
        weatherViewModel.weather.value?.let { updateWeatherUI(it) }
        weatherViewModel.hourlyForecast.value?.let { updateHourlyForecastUI(it) }
        weatherViewModel.forecast.value?.let { updateFiveDayForecastUI(it) }
        weatherViewModel.error.value?.let { updateErrorUI() }
        swipeRefreshLayout.isRefreshing = false
    }
}