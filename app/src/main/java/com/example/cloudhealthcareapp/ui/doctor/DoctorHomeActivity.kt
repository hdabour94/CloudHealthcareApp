package com.example.cloudhealthcareapp.ui.doctor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.viewmodel.DoctorViewModel
import com.google.firebase.auth.FirebaseAuth

class DoctorHomeActivity : AppCompatActivity() {

    private val viewModel: DoctorViewModel by viewModels()
    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var appointmentsAdapter: DoctorAppointmentsAdapter
    private lateinit var viewPatientsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_home)

        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView)
        viewPatientsButton = findViewById(R.id.viewPatientsButton)

        appointmentsAdapter = DoctorAppointmentsAdapter(emptyList())
        appointmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DoctorHomeActivity)
            adapter = appointmentsAdapter
        }

        val doctorId = FirebaseAuth.getInstance().currentUser?.uid
        if (doctorId != null) {
            viewModel.getDoctorAppointments(doctorId)
        }

        viewModel.appointments.observe(this) { appointments ->
            appointmentsAdapter.updateAppointments(appointments)
        }

        viewPatientsButton.setOnClickListener {
            // TODO: Implement View Patients (start an activity to list doctor's patients)
        }
    }
}