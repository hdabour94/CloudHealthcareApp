package com.example.cloudhealthcareapp.ui.doctor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.viewmodel.DoctorViewModel

class AppointmentRequestsActivity : AppCompatActivity() {

    private val viewModel: DoctorViewModel by viewModels()
    private lateinit var requestsRecyclerView: RecyclerView
    private lateinit var requestsAdapter: AppointmentRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_requests)

        requestsRecyclerView = findViewById(R.id.requestsRecyclerView)
        requestsAdapter = AppointmentRequestAdapter(
            appointments = emptyList(),
            onAccept = { appointment ->
                viewModel.acceptAppointment(appointment)
            },
            onReject = { appointment ->
                viewModel.rejectAppointment(appointment)
            }
        )

        requestsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AppointmentRequestsActivity)
            adapter = requestsAdapter
        }

        viewModel.appointmentRequests.observe(this) { requests ->
            requestsAdapter.updateAppointments(requests)
        }

        viewModel.appointmentAcceptanceResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Appointment accepted", Toast.LENGTH_SHORT).show()
                viewModel.getAppointmentRequests() // Refresh the list
            } else {
                Toast.makeText(this, "Failed to accept appointment", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.appointmentRejectionResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Appointment rejected", Toast.LENGTH_SHORT).show()
                viewModel.getAppointmentRequests() // Refresh the list
            } else {
                Toast.makeText(this, "Failed to reject appointment", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.getAppointmentRequests()
    }
}