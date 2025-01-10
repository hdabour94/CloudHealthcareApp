package com.example.cloudhealthcareapp.ui.doctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.MedicalRecord

class DiagnosisAdapter(private var diagnosisList: List<MedicalRecord>) :
    RecyclerView.Adapter<DiagnosisAdapter.DiagnosisViewHolder>() {

    class DiagnosisViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val diagnosisDateTextView: TextView = view.findViewById(R.id.diagnosisDateTextView)
        val diagnosisDetailsTextView: TextView = view.findViewById(R.id.diagnosisDetailsTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiagnosisViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diagnosis, parent, false)
        return DiagnosisViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiagnosisViewHolder, position: Int) {
        val diagnosis = diagnosisList[position]
        holder.diagnosisDateTextView.text = diagnosis.date
        holder.diagnosisDetailsTextView.text = diagnosis.diagnosis
    }

    override fun getItemCount() = diagnosisList.size

    fun updateDiagnosis(newDiagnosis: List<MedicalRecord>) {
        diagnosisList = newDiagnosis
        notifyDataSetChanged()
    }
}