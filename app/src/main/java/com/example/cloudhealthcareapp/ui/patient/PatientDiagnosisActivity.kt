package com.example.cloudhealthcareapp.ui.patient

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.ui.doctor.DiagnosisAdapter
import com.example.cloudhealthcareapp.viewmodel.PatientViewModel

class PatientDiagnosisActivity : AppCompatActivity() {

    private val viewModel: PatientViewModel by viewModels()
    private lateinit var diagnosisRecyclerView: RecyclerView
    private lateinit var diagnosisAdapter: DiagnosisAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_diagnosis)

        diagnosisRecyclerView = findViewById(R.id.diagnosisRecyclerView)
        diagnosisAdapter = DiagnosisAdapter(emptyList())

        diagnosisRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientDiagnosisActivity)
            adapter = diagnosisAdapter
        }

        val patientId = intent.getStringExtra("patientId") ?: ""

        // Observe the diagnosis LiveData from PatientViewModel
        viewModel.diagnosis.observe(this) { diagnosis ->
            diagnosisAdapter.updateDiagnosis(diagnosis)
        }

        // Fetch diagnosis using PatientViewModel
        viewModel.fetchDiagnosis(patientId)
    }
}