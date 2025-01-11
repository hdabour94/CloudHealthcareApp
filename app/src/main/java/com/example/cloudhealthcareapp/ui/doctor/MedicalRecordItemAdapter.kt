package com.example.cloudhealthcareapp.ui.doctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.MedicalRecordItem

class MedicalRecordItemAdapter(private var medicalRecordItems: List<MedicalRecordItem>) :
    RecyclerView.Adapter<MedicalRecordItemAdapter.MedicalRecordItemViewHolder>() {

    class MedicalRecordItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.recordDateTextView)
        val diagnosisTextView: TextView = view.findViewById(R.id.diagnosisTextView)
        val prescriptionTextView: TextView = view.findViewById(R.id.prescriptionTextView)
        val notesTextView: TextView = view.findViewById(R.id.notesTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicalRecordItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medical_record_item, parent, false)
        return MedicalRecordItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicalRecordItemViewHolder, position: Int) {
        val item = medicalRecordItems[position]
        holder.dateTextView.text = item.date
        holder.diagnosisTextView.text = item.diagnosis
        holder.prescriptionTextView.text = item.prescription ?: "N/A"
        holder.notesTextView.text = item.notes ?: "N/A"
    }

    override fun getItemCount() = medicalRecordItems.size

    fun updateMedicalRecordItems(newItems: List<MedicalRecordItem>) {
        medicalRecordItems = newItems
        notifyDataSetChanged()
    }
}