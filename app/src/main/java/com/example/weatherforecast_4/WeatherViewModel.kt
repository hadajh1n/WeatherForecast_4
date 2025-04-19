package com.example.weatherforecast_4

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast_4.retrofit.CurrentWeather
import com.example.weatherforecast_4.retrofit.WeatherApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(private val weatherApi: WeatherApi): ViewModel() {
    private val _weather = MutableLiveData<CurrentWeather?>()
    val weather: LiveData<CurrentWeather?> get() = _weather

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val apiKey = "3a40caaed30624dd3ed13790e371b4bd"

    init {
        fetchWeather("Orel")
    }

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            try {
                val response = weatherApi.getCurrentWeather(
                    city = city,
                    apiKey = apiKey,
                    lang = "ru"
                )
                _weather.postValue(response)
                _error.postValue(null)
            } catch (e: Exception) {
                _weather.postValue(null)
                _error.postValue("")
            }
        }
    }
}