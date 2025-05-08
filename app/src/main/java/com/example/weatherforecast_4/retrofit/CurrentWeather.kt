package com.example.weatherforecast_4.retrofit

import com.google.gson.annotations.SerializedName

data class CurrentWeather(
    val name: String,               // Название города
    val main: Main,                 // Основные погодные параметры
    val weather: List<Weather>,     // Описание погодных условий
    val wind: Wind,                 // Данные о ветре
    val coord: Coord? = null,       // Координаты
    val timezone: Int
)

data class Main(
    val temp: Float,                // Температура
    @SerializedName("feels_like") val feelsLike: Float, // Температура (ощущается как)
    val humidity: Int,              // Влажность
    val pressure: Int               // Давление
)

data class Weather(
    val main: String,               // Краткое описание погоды
    val description: String,        // Описание погоды
    val icon: String                // Код иконки
)

data class Wind(
    val speed: Float                // Скорость ветра (м/с)
)

data class Coord(
    val lat: Float,
    val lon: Float
)