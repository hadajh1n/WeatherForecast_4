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
import kotlin.math.roundToInt

class ForecastAdapter(private val forecastList: List<ForecastItem>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

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
        // Форматируем дату
        val date = Date(forecast.dt * 1000)
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        holder.dateTextView.text = sdf.format(date)
        // Температура
        holder.temperatureTextView.text = "${forecast.main.temp.roundToInt()}°"
        val iconCode = forecast.weather.firstOrNull()?.icon
        if (iconCode != null) {
            val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
            Glide.with(holder.itemView.context)
                .load(iconUrl)
                .into(holder.weatherIconItemImageView)
        } else {
            holder.weatherIconItemImageView.setImageDrawable(null) // Очистка, если нет иконки
        }
    }

    override fun getItemCount(): Int = forecastList.size
}