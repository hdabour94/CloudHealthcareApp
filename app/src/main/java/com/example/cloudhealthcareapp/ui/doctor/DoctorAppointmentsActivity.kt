package com.example.cloudhealthcareapp.ui.doctor

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.viewmodel.DoctorViewModel

class DoctorAppointmentsActivity : AppCompatActivity() {

    private val viewModel: DoctorViewModel by viewModels()
    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var appointmentsAdapter: DoctorAppointmentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_appointments)

        appointmentsRecyclerView = findViewById(R.id.doctorAppointmentsRecyclerView) // Use the new ID
        appointmentsAdapter = DoctorAppointmentsAdapter(emptyList())

        appointmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DoctorAppointmentsActivity)
            adapter = appointmentsAdapter
        }

        // Fetch and display the appointments for the current doctor
        viewModel.getAppointmentsForDoctor()

        viewModel.appointments.observe(this) { appointments ->
            appointmentsAdapter.updateAppointments(appointments)
        }
    }
}