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

class HourlyForecastAdapter(
    private val hourlyList: List<ForecastItem>,
    private val timezoneOffset: Int
) : RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {

    class HourlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val temperatureTextView: TextView = itemView.findViewById(R.id.temperatureTextView)
        val weatherIcon: ImageView = itemView.findViewById(R.id.weatherIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hourly_forecast, parent, false)
        return HourlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        val forecast = hourlyList[position]
        val iconCode = forecast.weather.firstOrNull()?.icon

        val sdfTime = SimpleDateFormat("HH:mm", Locale("ru")).apply {
            timeZone = TimeZone.getTimeZone("UTC").apply { rawOffset = timezoneOffset * 1000 } // Локальный часовой пояс
        }
        val dateTime = sdfTime.format(Date(forecast.dt * 1000))

        if (forecast.dt == 0L || iconCode == null || iconCode.isEmpty()) {
            holder.timeTextView.text = "Нет данных"
            holder.temperatureTextView.text = ""
            holder.weatherIcon.setImageDrawable(null)
        } else {
            holder.timeTextView.text = dateTime
            holder.temperatureTextView.text = "${forecast.main.temp.toInt()}°C"
            Glide.with(holder.itemView.context)
                .load("https://openweathermap.org/img/wn/$iconCode@2x.png")
                .into(holder.weatherIcon)
        }
    }

    override fun getItemCount(): Int = hourlyList.size
}