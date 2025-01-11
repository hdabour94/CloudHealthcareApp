package com.example.cloudhealthcareapp.ui.doctor

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.Appointment

class AppointmentRequestAdapter(
    private var appointments: List<Appointment>,
    private val onAccept: (Appointment) -> Unit,
    private val onReject: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentRequestAdapter.AppointmentViewHolder>() {

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val patientNameTextView: TextView = view.findViewById(R.id.patientNameTextView)
        val dateTimeTextView: TextView = view.findViewById(R.id.dateTimeTextView)
        val acceptButton: Button = view.findViewById(R.id.acceptButton)
        val rejectButton: Button = view.findViewById(R.id.rejectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment_request, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.patientNameTextView.text = appointment.patientName
        holder.dateTimeTextView.text = appointment.appointmentDateTime

        holder.acceptButton.setOnClickListener { onAccept(appointment) }
        holder.rejectButton.setOnClickListener { onReject(appointment) }
    }

    override fun getItemCount() = appointments.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateAppointments(newAppointments: List<Appointment>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }
}