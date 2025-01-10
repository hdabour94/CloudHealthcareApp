package com.example.cloudhealthcareapp.ui.doctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R

class PrescriptionsAdapter(private var prescriptions: List<Map<String, Any>>) :
    RecyclerView.Adapter<PrescriptionsAdapter.PrescriptionViewHolder>() {

    class PrescriptionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val prescriptionDateTextView: TextView = view.findViewById(R.id.prescriptionDateTextView)
        val prescriptionDetailsTextView: TextView = view.findViewById(R.id.prescriptionDetailsTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrescriptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_prescription, parent, false)
        return PrescriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrescriptionViewHolder, position: Int) {
        val prescription = prescriptions[position]
        holder.prescriptionDateTextView.text = prescription["date"] as? String ?: ""
        holder.prescriptionDetailsTextView.text = prescription["prescriptionText"] as? String ?: ""
    }

    override fun getItemCount() = prescriptions.size

    fun updatePrescriptions(newPrescriptions: List<Map<String, Any>>) {
        prescriptions = newPrescriptions
        notifyDataSetChanged()
    }
}