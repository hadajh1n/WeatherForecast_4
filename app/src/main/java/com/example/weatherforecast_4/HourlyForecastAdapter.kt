package com.example.weatherforecast_4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherforecast_4.retrofit.ForecastItem
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class HourlyForecastAdapter(private val hourlyList: List<ForecastItem>) :
    RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {

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

        if (forecast.dt == 0L || iconCode == null || iconCode.isEmpty()) {
            holder.timeTextView.text = "Нет данных"
            holder.temperatureTextView.text = ""
            holder.weatherIcon.setImageDrawable(null)
        } else {
            val date = Date(forecast.dt * 1000)
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            holder.timeTextView.text = sdf.format(date)
            holder.temperatureTextView.text = "${forecast.main.temp.toInt()}°C"
            Glide.with(holder.itemView.context)
                .load("https://openweathermap.org/img/wn/$iconCode.png")
                .into(holder.weatherIcon)
        }
    }

    override fun getItemCount(): Int = hourlyList.size
}