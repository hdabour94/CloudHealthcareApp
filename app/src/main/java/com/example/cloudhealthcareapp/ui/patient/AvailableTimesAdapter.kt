package com.example.cloudhealthcareapp.ui.patient

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R

class AvailableTimesAdapter(
    private var availableTimes: List<String>,
    private val onTimeClick: (String) -> Unit
) : RecyclerView.Adapter<AvailableTimesAdapter.TimeViewHolder>() {

    private var selectedTime: String? = null

    class TimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_available_time, parent, false)
        return TimeViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val time = availableTimes[position]
        holder.timeTextView.text = time

        // Highlight the selected time
        if (time == selectedTime) {
            holder.timeTextView.setBackgroundColor(Color.LTGRAY)
        } else {
            holder.timeTextView.setBackgroundColor(Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            selectedTime = time
            onTimeClick(time)
            notifyDataSetChanged() // Update the list to reflect the selection
        }
    }

    override fun getItemCount() = availableTimes.size

    fun updateTimes(newTimes: List<String>) {
        availableTimes = newTimes
        notifyDataSetChanged()
    }
}