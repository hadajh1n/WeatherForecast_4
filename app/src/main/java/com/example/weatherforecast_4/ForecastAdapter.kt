package com.example.weatherforecast_4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherforecast_4.retrofit.ForecastItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class ForecastAdapter(
    private val forecastList: List<ForecastItem>,
    private val timezoneOffset: Int
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val temperatureTextView: TextView = itemView.findViewById(R.id.forecastTemperatureTextView)
        val weatherIconItemImageView: ImageView = itemView.findViewById(R.id.weatherIconItemImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecast = forecastList[position]
        val iconCode = forecast.weather.firstOrNull()?.icon

        val sdfDate = SimpleDateFormat("d MMMM yyyy", Locale("ru")).apply {
            timeZone = TimeZone.getTimeZone("UTC") // Установка UTC как базовый
        }
        val adjustedTime = forecast.dt + timezoneOffset // Корректировка времени с учетом смещения города
        val dateOnly = sdfDate.format(Date(adjustedTime * 1000))

        if (forecast.dt == 0L || iconCode == null || iconCode.isEmpty()) {
            holder.dateTextView.text = "Нет данных"
            holder.temperatureTextView.text = ""
            holder.weatherIconItemImageView.setImageDrawable(null)
        } else {
            holder.dateTextView.text = dateOnly
            holder.temperatureTextView.text = "${forecast.main.temp.roundToInt()}°С"
            Glide.with(holder.itemView.context)
                .load("https://openweathermap.org/img/wn/$iconCode@2x.png")
                .into(holder.weatherIconItemImageView)
        }
    }

    override fun getItemCount(): Int = forecastList.size
}