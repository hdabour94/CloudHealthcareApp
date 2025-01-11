package com.example.cloudhealthcareapp.ui.patient

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.ui.doctor.MedicalRecordsAdapter
import com.example.cloudhealthcareapp.viewmodel.PatientViewModel

class MedicalRecordsActivity : AppCompatActivity() {

    private val viewModel: PatientViewModel by viewModels()
    private lateinit var medicalRecordsRecyclerView: RecyclerView
    private lateinit var medicalRecordsAdapter: MedicalRecordsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_records)

        medicalRecordsRecyclerView = findViewById(R.id.medicalRecordsRecyclerView)
        medicalRecordsAdapter = MedicalRecordsAdapter(this, emptyList())

        medicalRecordsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MedicalRecordsActivity)
            adapter = medicalRecordsAdapter
        }

        val patientId = intent.getStringExtra("patientId") ?: ""
        viewModel.fetchMedicalRecords(patientId)

        viewModel.medicalRecords.observe(this) { medicalRecords ->
            medicalRecordsAdapter.updateMedicalRecords(medicalRecords)
        }
    }
}