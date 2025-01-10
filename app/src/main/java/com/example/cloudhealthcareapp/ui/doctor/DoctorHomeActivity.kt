package com.example.cloudhealthcareapp.ui.doctor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.services.AppointmentCleanupJobService
import com.example.cloudhealthcareapp.viewmodel.DoctorViewModel
import com.google.firebase.auth.FirebaseAuth

class DoctorHomeActivity : AppCompatActivity() {

    private val viewModel: DoctorViewModel by viewModels()
    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var appointmentsAdapter: DoctorAppointmentsAdapter
    private lateinit var viewPatientsButton: Button
    private lateinit var viewAppointmentRequestsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_home)

        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView)
        viewPatientsButton = findViewById(R.id.viewPatientsButton)
        viewAppointmentRequestsButton = findViewById(R.id.viewAppointmentRequestsButton)

        appointmentsAdapter = DoctorAppointmentsAdapter(emptyList())
        appointmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DoctorHomeActivity)
            adapter = appointmentsAdapter
        }

        // Fetch appointments for the current doctor
        viewModel.getAppointmentsForDoctor()

        viewModel.appointments.observe(this) { appointments ->
            Log.d("DoctorHomeActivity", "Appointments received: ${appointments.size}")
            appointmentsAdapter.updateAppointments(appointments)
        }

        viewPatientsButton.setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        viewAppointmentRequestsButton.setOnClickListener {
            startActivity(Intent(this, AppointmentRequestsActivity::class.java))
        }
        // Schedule the job when the activity is created
        AppointmentCleanupJobService.scheduleJob(this)
    }
}