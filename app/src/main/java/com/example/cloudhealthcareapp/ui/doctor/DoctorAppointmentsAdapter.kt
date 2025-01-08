package com.example.cloudhealthcareapp.ui.doctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.Appointment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DoctorAppointmentsAdapter(private var appointments: List<Appointment>) :
    RecyclerView.Adapter<DoctorAppointmentsAdapter.AppointmentViewHolder>() {

    private val db = Firebase.firestore

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val patientNameTextView: TextView = view.findViewById(R.id.patientNameTextView)
        val appointmentDateTimeTextView: TextView =
            view.findViewById(R.id.appointmentDateTimeTextView)
        val statusTextView: TextView = view.findViewById(R.id.statusTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]

        // Get patient name from Firestore
        appointment.patientId?.let { patientId ->
            db.collection("patients").document(patientId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val patientName = document.getString("fullName") ?: "Unknown Patient"
                        holder.patientNameTextView.text = patientName
                    } else {
                        holder.patientNameTextView.text = "Unknown Patient"
                    }
                }
                .addOnFailureListener {
                    holder.patientNameTextView.text = "Unknown Patient"
                }
        }

        holder.appointmentDateTimeTextView.text = appointment.appointmentDateTime
        holder.statusTextView.text = appointment.status
    }

    override fun getItemCount() = appointments.size

    fun updateAppointments(newAppointments: List<Appointment>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }
}