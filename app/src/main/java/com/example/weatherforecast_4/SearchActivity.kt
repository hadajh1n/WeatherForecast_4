package com.example.weatherforecast_4

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()

    private lateinit var backButton: ImageButton
    private lateinit var cityAutoComplete: AppCompatAutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        backButton = findViewById(R.id.btnBackSearch)
        cityAutoComplete = findViewById(R.id.edtSearch)

        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val cities = resources.getStringArray(R.array.cities)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
        cityAutoComplete.setAdapter(adapter)

        cityAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedCity = adapter.getItem(position) as String
            weatherViewModel.fetchWeather(selectedCity)
        }

        weatherViewModel.weather.observe(this) { weather ->
            weather?.let {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("SELECTED_CITY", it.name)
                }
                startActivity(intent)
                cityAutoComplete.clearFocus()
                finish()
            }
        }

        weatherViewModel.error.observe(this) { error ->
            error?.let {
                if (it.isNotEmpty()) {
                    cityAutoComplete.error = "Нет подключения к интернету"
                }
            }
        }
    }
}