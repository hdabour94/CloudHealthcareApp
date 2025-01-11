package com.example.cloudhealthcareapp.ui.patient

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.ui.doctor.PrescriptionsAdapter
import com.example.cloudhealthcareapp.viewmodel.PatientViewModel

class PatientPrescriptionsActivity : AppCompatActivity() {

    private val viewModel: PatientViewModel by viewModels()
    private lateinit var prescriptionsRecyclerView: RecyclerView
    private lateinit var prescriptionsAdapter: PrescriptionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_prescriptions)

        prescriptionsRecyclerView = findViewById(R.id.prescriptionsRecyclerView)
        prescriptionsAdapter = PrescriptionsAdapter(emptyList())

        prescriptionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientPrescriptionsActivity)
            adapter = prescriptionsAdapter
        }

        val patientId = intent.getStringExtra("patientId") ?: ""

        // Observe the prescriptions LiveData from PatientViewModel
        viewModel.prescriptions.observe(this) { prescriptions ->
            prescriptionsAdapter.updatePrescriptions(prescriptions)
        }

        // Fetch prescriptions using PatientViewModel
        viewModel.fetchPrescriptions(patientId)
    }
}