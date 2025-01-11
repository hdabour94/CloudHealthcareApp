package com.example.cloudhealthcareapp.ui.doctor

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.viewmodel.DoctorViewModel

class MedicalRecordsActivity : AppCompatActivity() {

    private val viewModel: DoctorViewModel by viewModels()
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