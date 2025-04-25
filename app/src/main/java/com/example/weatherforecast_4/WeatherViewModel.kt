package com.example.weatherforecast_4

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast_4.retrofit.CurrentWeather
import com.example.weatherforecast_4.retrofit.ForecastItem
import com.example.weatherforecast_4.retrofit.Main
import com.example.weatherforecast_4.retrofit.WeatherApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt
import com.example.weatherforecast_4.retrofit.Weather
import kotlin.random.Random

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherApi: WeatherApi,
    @ApplicationContext private val context: Context
): ViewModel() {

    private val _weather = MutableLiveData<CurrentWeather?>()
    val weather: LiveData<CurrentWeather?> get() = _weather

    private val _forecast = MutableLiveData<List<ForecastItem>?>()
    val forecast: LiveData<List<ForecastItem>?> get() = _forecast

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // LiveData для фоновой картинки
    private val _backgroundImageResId = MutableLiveData<Int>()
    val backgroundImageResId: LiveData<Int> = _backgroundImageResId

    private val apiKey = "3a40caaed30624dd3ed13790e371b4bd"

    val backgroundImages = listOf(
        R.drawable.background1,
        R.drawable.background2,
        R.drawable.background3,
        R.drawable.background4,
        R.drawable.background5,
        R.drawable.background7,
        R.drawable.background8,
        R.drawable.background9,
    )

    init {
        // Выбираем случайный фон при создании ViewModel
        selectRandomBackground()
    }

    // Метод для выбора случайного фона
    private fun selectRandomBackground() {
        val randomBackground = backgroundImages[Random.nextInt(backgroundImages.size)]
        _backgroundImageResId.value = randomBackground
    }

    fun fetchWeather(city: String) {
        if (city.isBlank()) return
        viewModelScope.launch {
            try {
                val response = weatherApi.getCurrentWeather(
                    city = city,
                    apiKey = apiKey,
                    lang = "ru"
                )
                _weather.postValue(response)
                _error.postValue(null)

                // Сохранение города
                val prefs = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
                prefs.edit {
                    putString("last_city", response.name)
                }

                // Запрос прогноза
                fetchFiveDayForecast(city)
            } catch (e: Exception) {
                _weather.postValue(null)
                _error.postValue(if (e.message?.contains("429") == true) {
                    "Слишком много запросов"
                } else {
                    "Город не найден"
                })
            }
        }
    }

    private fun fetchFiveDayForecast(city: String) {
        viewModelScope.launch {
            try {
                val response = weatherApi.getFiveDayForecast(
                    city = city,
                    apiKey = apiKey,
                    lang = "ru"
                )
                // Выбираем данные на 12:00 для каждого дня
                val dailyForecasts = aggregateToDaily(response.list)
                _forecast.postValue(if (dailyForecasts.isEmpty()) null else dailyForecasts)
                // Сохраняем прогноз в SharedPreferences
                val prefs = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
                prefs.edit {
                    dailyForecasts.forEachIndexed { index, forecast ->
                        putLong("forecast_dt_$index", forecast.dt)
                        putInt("forecast_temp_$index", forecast.main.temp.roundToInt())
                        putString("forecast_icon_$index", forecast.weather.firstOrNull()?.icon)
                    }
                    putInt("forecast_count", dailyForecasts.size)
                }
            } catch (e: Exception) {
                _forecast.postValue(null)
                _error.postValue(if (e.message?.contains("429") == true) {
                    "Слишком много запросов"
                } else {
                    "Не удалось загрузить прогноз"
                })
            }
        }
    }

    private fun aggregateToDaily(forecasts: List<ForecastItem>): List<ForecastItem> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        // Группируем по дате и выбираем прогноз на 12:00
        return forecasts
            .groupBy { sdf.format(Date(it.dt * 1000)) }
            .mapNotNull { (_, items) ->
                items.minByOrNull {
                    val time = timeFormat.format(Date(it.dt * 1000))
                    kotlin.math.abs(time.toHourMinute() - 12 * 60) // Ближайший к 12:00
                }
            }
            .take(5) // Ограничиваем 5 днями
    }

    private fun String.toHourMinute(): Int {
        val parts = split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    fun loadCachedForecast() {
        val prefs = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        val count = prefs.getInt("forecast_count", 0)
        if (count > 0) {
            val daily = mutableListOf<ForecastItem>()
            for (i in 0 until count) {
                val icon = prefs.getString("forecast_icon_$i", null)
                daily.add(
                    ForecastItem(
                        dt = prefs.getLong("forecast_dt_$i", 0),
                        main = Main(
                            temp = prefs.getInt("forecast_temp_$i", 0).toFloat(),
                            feelsLike = 0f, // Не используется
                            humidity = 0,   // Не используется
                            pressure = 0    // Не используется
                        ),
                        weather = if (icon != null) listOf(Weather("", "", icon)) else emptyList()
                    )
                )
            }
            _forecast.postValue(daily)
        }
    }
}