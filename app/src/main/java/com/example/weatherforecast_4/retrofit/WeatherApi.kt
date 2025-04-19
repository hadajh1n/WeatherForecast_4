package com.example.weatherforecast_4.retrofit

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,                   // Название города
        @Query("appid") apiKey: String,             // Мой API-ключ
        @Query("units") units: String = "metric",   // Цельсии
        @Query("lang") lang: String                 // Русский язык для описания
    ): CurrentWeather
}