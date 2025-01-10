package com.example.cloudhealthcareapp.ui.patient

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.Appointment
import com.example.cloudhealthcareapp.viewmodel.PatientViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class BookAppointmentActivity : AppCompatActivity() {

    private val viewModel: PatientViewModel by viewModels()
    private lateinit var doctorNameTextView: TextView
    private lateinit var appointmentDateTextView: TextView
    private lateinit var availableTimesRecyclerView: RecyclerView
    private lateinit var availableTimesAdapter: AvailableTimesAdapter
    private lateinit var bookButton: Button

    private var doctorId: String? = null
    private var doctorName: String? = null
    private val calendar: Calendar = Calendar.getInstance()
    private var selectedTime: String? = null
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)

        doctorId = intent.getStringExtra("doctorId")
        doctorName = intent.getStringExtra("doctorName")

        doctorNameTextView = findViewById(R.id.doctorNameTextView)
        appointmentDateTextView = findViewById(R.id.appointmentDateTextView)
        availableTimesRecyclerView = findViewById(R.id.availableTimesRecyclerView)
        bookButton = findViewById(R.id.bookButton)

        doctorNameTextView.text = doctorName ?: "Select Doctor"

        // Initialize the adapter with an empty list
        availableTimesAdapter = AvailableTimesAdapter(emptyList()) { time ->
            selectedTime = time
          //  Toast.makeText(this, "Selected time: $time", Toast.LENGTH_SHORT).show()
        }

        availableTimesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BookAppointmentActivity)
            adapter = availableTimesAdapter
        }

        appointmentDateTextView.setOnClickListener {
            showDatePicker()
        }

        bookButton.setOnClickListener {
            val patientId = FirebaseAuth.getInstance().currentUser?.uid
            val patientName = FirebaseAuth.getInstance().currentUser?.displayName

            if (patientId != null && doctorId != null && selectedDate != null && selectedTime != null) {
                val appointmentDateTime = "$selectedDate $selectedTime"

                // Check if selected date and time are in the future
                val selectedDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(appointmentDateTime)
                if (selectedDateTime != null && selectedDateTime.before(Calendar.getInstance().time)) {
                    Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Check for conflicts before booking
                viewModel.checkAppointmentConflict(doctorId!!, appointmentDateTime)
                    .observe(this) { isConflict ->
                        if (!isConflict) {
                            val appointment = Appointment(
                                appointmentId = UUID.randomUUID().toString(),
                                patientId = patientId,
                                patientName = patientName,
                                doctorId = doctorId,
                                doctorName = doctorName,
                                appointmentDateTime = appointmentDateTime,
                                reason = null,
                                status = "pending"
                            )
                            viewModel.bookAppointment(appointment)
                        } else {
                            // Handle conflict
                            Toast.makeText(this, "Appointment time is not available. Please choose another time.", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show()
            }

        }

        viewModel.appointmentBookingResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show()
                // Refresh the available times after booking
                if (doctorId != null && selectedDate != null) {
                    viewModel.getAvailableTimes(doctorId!!, selectedDate!!).observe(this) { times ->
                        availableTimesAdapter.updateTimes(times)
                    }
                }
                finish()
            } else {
                Toast.makeText(this, "Failed to book appointment.", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe LiveData for available times
        if (doctorId != null && selectedDate != null) {
            viewModel.getAvailableTimes(doctorId!!, selectedDate!!).observe(this) { times ->
                availableTimesAdapter.updateTimes(times)
            }
        }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()

            // Update available times when a new date is selected
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            if (doctorId != null) {
                // Show a progress indicator or disable interaction while fetching
                availableTimesRecyclerView.visibility = View.GONE
                // Fetch available times
                viewModel.getAvailableTimes(doctorId!!, selectedDate!!).observe(this@BookAppointmentActivity) { times ->
                    Log.d("Available times", times.toString())
                    availableTimesAdapter.updateTimes(times)
                    availableTimesRecyclerView.visibility = View.VISIBLE // Show the RecyclerView again
                }
            }
        }

        val datePickerDialog = DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun updateDateInView() {
        val myFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        appointmentDateTextView.text = sdf.format(calendar.time)
    }
}