package com.example.cloudhealthcareapp.ui.patient

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.viewmodel.PatientViewModel
import androidx.activity.viewModels

class PatientAppointmentsActivity : AppCompatActivity() {

    private val viewModel: PatientViewModel by viewModels()
    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var appointmentsAdapter: PatientAppointmentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_appointments)

        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView)
        appointmentsAdapter = PatientAppointmentsAdapter(emptyList())

        appointmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientAppointmentsActivity)
            adapter = appointmentsAdapter
        }

        // Fetch and display the appointments
        viewModel.getAppointmentsForPatient()

        viewModel.appointments.observe(this) { appointments ->
            appointmentsAdapter.updateAppointments(appointments)
        }
    }
}