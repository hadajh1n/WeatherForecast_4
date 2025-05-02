package com.example.weatherforecast_4.retrofit

data class FiveDayForecast(
    val list: List<ForecastItem>,   // Список прогнозов (до 40 элементов, по 8 на день, 5 дней * 8 интервалов по 3 часа)
    val city: City                  // Город
)

data class ForecastItem(
    val dt: Long,                   // Временная метка для интервала
    val main: Main,                 // Погодные параметры (ссылка на Main из CurrentWeather)
    val weather: List<Weather>      // Код иконки
)

data class City(
    val name: String,               // Название города
    val country: String? = null,    // Страна
    val coord: Coord? = null        // Координаты
)