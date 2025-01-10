package com.example.cloudhealthcareapp.ui.doctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.Patient

class PatientsAdapter(private var patients: List<Patient>) :
    RecyclerView.Adapter<PatientsAdapter.PatientViewHolder>() {

    class PatientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val patientNameTextView: TextView = view.findViewById(R.id.patientNameTextView)
        // Add other TextViews to display patient details
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patients[position]
        holder.patientNameTextView.text = patient.fullName
        // Set other TextViews with patient details
    }

    override fun getItemCount() = patients.size

    fun updatePatients(newPatients: List<Patient>) {
        patients = newPatients
        notifyDataSetChanged()
    }
}