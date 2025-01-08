package com.example.cloudhealthcareapp.ui.patient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.Appointment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PatientAppointmentsAdapter(private var appointments: List<Appointment>) :
    RecyclerView.Adapter<PatientAppointmentsAdapter.AppointmentViewHolder>() {

    private val db = Firebase.firestore

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val doctorNameTextView: TextView = view.findViewById(R.id.doctorNameTextView)
        val appointmentDateTimeTextView: TextView =
            view.findViewById(R.id.appointmentDateTimeTextView)
        val statusTextView: TextView = view.findViewById(R.id.statusTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]

        // Get doctor name from Firestore
        appointment.doctorId?.let { doctorId ->
            db.collection("doctors").document(doctorId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val doctorName = document.getString("fullName") ?: "Unknown Doctor"
                        holder.doctorNameTextView.text = doctorName
                    } else {
                        holder.doctorNameTextView.text = "Unknown Doctor"
                    }
                }
                .addOnFailureListener {
                    holder.doctorNameTextView.text = "Unknown Doctor"
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