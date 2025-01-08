package com.example.cloudhealthcareapp.ui.patient

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.Appointment
import com.example.cloudhealthcareapp.viewmodel.PatientViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.UUID

class BookAppointmentActivity : AppCompatActivity() {

    private val viewModel: PatientViewModel by viewModels()
    private lateinit var doctorNameTextView: TextView
    private lateinit var appointmentDateEditText: EditText
    private lateinit var appointmentTimeEditText: EditText
    private lateinit var reasonEditText: EditText
    private lateinit var bookButton: Button
    private var doctorId: String? = null
    private var doctorName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)

        doctorId = intent.getStringExtra("doctorId")
        doctorName = intent.getStringExtra("doctorName")

        doctorNameTextView = findViewById(R.id.doctorNameTextView)
        appointmentDateEditText = findViewById(R.id.appointmentDateEditText)
        appointmentTimeEditText = findViewById(R.id.appointmentTimeEditText)
        reasonEditText = findViewById(R.id.reasonEditText)
        bookButton = findViewById(R.id.bookButton)

        doctorNameTextView.text = doctorName ?: "Select Doctor"

        bookButton.setOnClickListener {
            val patientId = FirebaseAuth.getInstance().currentUser?.uid
            val appointmentDate = appointmentDateEditText.text.toString()
            val appointmentTime = appointmentTimeEditText.text.toString()
            val reason = reasonEditText.text.toString()

            if (patientId != null && doctorId != null && appointmentDate.isNotEmpty() && appointmentTime.isNotEmpty() && reason.isNotEmpty()) {
                val appointmentDateTime = "$appointmentDate $appointmentTime"
                val appointment = Appointment(
                    appointmentId = UUID.randomUUID().toString(), // Generate unique ID
                    patientId = patientId,
                    doctorId = doctorId,
                    appointmentDateTime = appointmentDateTime,
                    reason = reason,
                    status = "pending"
                )

                viewModel.bookAppointment(appointment)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.appointmentBookingResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to book appointment. Please check for conflicts.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}