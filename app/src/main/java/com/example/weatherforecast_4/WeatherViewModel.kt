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
import java.util.Calendar
import java.util.TimeZone
import kotlin.random.Random

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherApi: WeatherApi,
    @ApplicationContext private val context: Context
): ViewModel() {

    private val _weather = MutableLiveData<CurrentWeather?>()
    val weather: LiveData<CurrentWeather?> get() = _weather

    private val _hourlyForecast = MutableLiveData<List<ForecastItem>?>()
    val hourlyForecast: LiveData<List<ForecastItem>?> get() = _hourlyForecast

    private val _forecast = MutableLiveData<List<ForecastItem>?>()
    val forecast: LiveData<List<ForecastItem>?> get() = _forecast

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // LiveData для фоновой картинки
    private val _backgroundImageResId = MutableLiveData<Int>()
    val backgroundImageResId: LiveData<Int> = _backgroundImageResId

    private val apiKey = BuildConfig.WEATHER_API_KEY

    val backgroundImages = listOf(
        R.drawable.background1,
        R.drawable.background2,
        R.drawable.background4,
        R.drawable.background5,
        R.drawable.background6,
        R.drawable.background7,
        R.drawable.background10
    )

    var cityTimezoneOffset: Int = 0 // Смещение часового пояса города в секундах

    init {
        // Выбираем случайный фон при создании ViewModel
        selectRandomBackground()
    }

    // Метод для выбора случайного фона
    private fun selectRandomBackground() {
        val randomBackground = backgroundImages[Random.nextInt(backgroundImages.size)]
        _backgroundImageResId.value = randomBackground
        context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE).edit {
            putInt("background_res_id", randomBackground)
        }
    }

    init {
        val prefs = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        val cachedBackground = prefs.getInt("background_res_id", -1)
        _backgroundImageResId.value = if (cachedBackground != -1) cachedBackground else backgroundImages[Random.nextInt(backgroundImages.size)]
    }

    fun fetchWeather(city: String) {
        if (city.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = weatherApi.getCurrentWeather(
                    city = city,
                    apiKey = apiKey,
                    lang = "ru"
                )
                _weather.postValue(response)
                _error.postValue(null)

                val prefs = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
                prefs.edit {
                    putString("last_city", response.name)
                }

                cityTimezoneOffset = response.timezone

                // Последовательный запуск прогнозов
                fetchHourlyForecast(city)
                fetchFiveDayForecast(city)
                _isLoading.value = false
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

    private fun fetchHourlyForecast(city: String) {
        viewModelScope.launch {
            try {
                val response = weatherApi.getFiveDayForecast(
                    city = city,
                    apiKey = apiKey,
                    lang = "ru"
                )
                // Использование времени из последнего запроса погоды (weather.dt) как базовое
                val currentWeather = _weather.value
                val currentTimeCity = currentWeather?.let { it.dt + it.timezone }
                    ?: throw IllegalStateException("Текущие данные погоды не доступны")
                val endTime = currentTimeCity + 24 * 3600
                // Фильтр прогноза: после текущего времени и в пределах 24 часов
                val hourlyList = response.list.filter { forecast ->
                    val forecastTimeCity = forecast.dt + cityTimezoneOffset
                    forecastTimeCity >= currentTimeCity - 3 * 3600 && forecastTimeCity <= endTime
                }
                _hourlyForecast.postValue(hourlyList)
                val prefs = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
                prefs.edit {
                    for (i in 0 until prefs.getInt("forecast_count", 0)) {
                        remove("hourly_dt_$i")
                        remove("hourly_temp_$i")
                        remove("hourly_icon_$i")
                    }
                    hourlyList.forEachIndexed { index, forecast ->
                        putLong("hourly_dt_$index", forecast.dt - cityTimezoneOffset)
                        putInt("hourly_temp_$index", forecast.main.temp.roundToInt())
                        putString("hourly_icon_$index", forecast.weather.firstOrNull()?.icon)
                    }
                    putInt("hourly_count", hourlyList.size)
                }
            } catch (e: Exception) {
                _hourlyForecast.postValue(null)
                _error.postValue("Не удалось загрузить почасовой прогноз")
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
                val currentWeather = _weather.value
                val currentTimeCity = currentWeather?.let { it.dt + it.timezone }
                    ?: throw IllegalStateException("Текущие данные погоды не доступны")
                // Выбираем данные на 12:00 для каждого дня
                val dailyForecasts = aggregateToDaily(response.list, currentTimeCity)
                _forecast.postValue(if (dailyForecasts.isEmpty()) null else dailyForecasts)
                val prefs = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
                prefs.edit {
                    dailyForecasts.forEachIndexed { index, forecast ->
                        putLong("forecast_dt_$index", forecast.dt - cityTimezoneOffset) // Сохранение в UTC
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

    private fun aggregateToDaily(forecasts: List<ForecastItem>, currentTimeCity: Long): List<ForecastItem> {
        if (forecasts.isEmpty()) return emptyList()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale("ru")).apply {
            timeZone = TimeZone.getTimeZone("UTC").apply { rawOffset = cityTimezoneOffset * 1000 }
        }
        val timeFormat = SimpleDateFormat("HH:mm", Locale("ru")).apply {
            timeZone = TimeZone.getTimeZone("UTC").apply { rawOffset = cityTimezoneOffset * 1000 }
        }
        // Текущая дата в часовом поясе города
        val startDate = sdf.format(Date(currentTimeCity * 1000))

        val calendar = Calendar.getInstance().apply {
            time = sdf.parse(startDate) ?: Date(currentTimeCity * 1000)
            add(Calendar.DAY_OF_MONTH, 1) // Добавление 1 дня
        }

        val tomorrowDate = sdf.format(calendar.time)

        val forecastsWithLocalTime = forecasts.map { forecast ->
            val localDt = forecast.dt + cityTimezoneOffset
            forecast.copy(dt = localDt)
        }

        // Группируем по дате и выбираем прогноз на 12:00
        return forecastsWithLocalTime
            .groupBy { sdf.format(Date(it.dt * 1000)) }
            .filterKeys { it >= tomorrowDate } // Начало с завтрашнего дня
            .mapNotNull { (_, items) ->
                items.minByOrNull {
                    val time = timeFormat.format(Date(it.dt * 1000))
                    kotlin.math.abs(time.toHourMinute() - 12 * 60)
                }
            }
            .take(5)
    }

    private fun String.toHourMinute(): Int {
        val parts = split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    fun loadCachedForecast() {
        val prefs = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        val forecastCount = prefs.getInt("forecast_count", 0)
        val hourlyCount = prefs.getInt("hourly_count", 0)

        if (forecastCount > 0) {
            val daily = mutableListOf<ForecastItem>()
            for (i in 0 until forecastCount) {
                val dt = prefs.getLong("forecast_dt_$i", 0)
                val adjustedDt = if (dt != 0L) dt - cityTimezoneOffset else dt // Убирается смещение, если оно было применено
                val icon = prefs.getString("forecast_icon_$i", null)
                daily.add(
                    ForecastItem(
                        dt = adjustedDt,
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

        if (hourlyCount > 0) {
            val hourly = mutableListOf<ForecastItem>()
            for (i in 0 until hourlyCount) {
                val dt = prefs.getLong("hourly_dt_$i", 0)
                val adjustedDt = if (dt != 0L) dt + cityTimezoneOffset else dt
                val icon = prefs.getString("hourly_icon_$i", null)
                hourly.add(
                    ForecastItem(
                        dt = adjustedDt,
                        main = Main(
                            temp = prefs.getInt("hourly_temp_$i", 0).toFloat(),
                            feelsLike = 0f,
                            humidity = 0,
                            pressure = 0
                        ),
                        weather = if (icon != null) listOf(Weather("", "", icon)) else emptyList()
                    )
                )
            }
            _hourlyForecast.postValue(hourly)
        }
    }
}