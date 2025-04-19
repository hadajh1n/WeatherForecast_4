package com.example.weatherforecast_4.retrofit

data class CurrentWeather(
    val name: String,               // Название города
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Float,                // Температура
    val humidity: Int               // Влажность
)

data class Weather(
    val description: String,        // Описание погоды
    val icon: String                // Код иконки
)