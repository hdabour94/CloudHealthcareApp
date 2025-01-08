package com.example.cloudhealthcareapp.ui.patient

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.Doctor
import com.example.cloudhealthcareapp.viewmodel.PatientViewModel
import androidx.activity.viewModels

class PatientHomeActivity : AppCompatActivity() {

    private val viewModel: PatientViewModel by viewModels()
    private lateinit var doctorsRecyclerView: RecyclerView
    private lateinit var doctorsAdapter: DoctorsAdapter
    private lateinit var bookAppointmentButton: Button
    private lateinit var uploadMedicalRecordButton: Button
    private lateinit var viewAppointmentsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_home)

        doctorsRecyclerView = findViewById(R.id.doctorsRecyclerView)
        bookAppointmentButton = findViewById(R.id.bookAppointmentButton)
        uploadMedicalRecordButton = findViewById(R.id.uploadMedicalRecordButton)
        viewAppointmentsButton = findViewById(R.id.viewAppointmentsButton)

        doctorsAdapter = DoctorsAdapter(emptyList()) { doctor ->
            // Handle doctor selection (e.g., start BookAppointmentActivity with doctor's ID)
            val intent = Intent(this, BookAppointmentActivity::class.java)
            intent.putExtra("doctorId", doctor.userId)
            intent.putExtra("doctorName", doctor.fullName)
            startActivity(intent)
        }

        doctorsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientHomeActivity)
            adapter = doctorsAdapter
        }

        viewModel.doctors.observe(this) { doctors ->
            doctorsAdapter.updateDoctors(doctors)
        }

        viewModel.getDoctors() // Fetch doctors on activity start

        bookAppointmentButton.setOnClickListener {
            startActivity(Intent(this, BookAppointmentActivity::class.java))
        }

        uploadMedicalRecordButton.setOnClickListener {
            startActivity(Intent(this, UploadMedicalRecordActivity::class.java))
        }

        viewAppointmentsButton.setOnClickListener {
            // TODO: Implement View Appointments (start an activity to list patient's appointments)
            Toast.makeText(this, "View Appointments - Not yet implemented", Toast.LENGTH_SHORT).show()
        }
    }
}

// Separate Adapter for Doctors (you can create a new file for this)
class DoctorsAdapter(
    private var doctors: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorsAdapter.DoctorViewHolder>() {

    class DoctorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val doctorNameTextView: TextView = view.findViewById(R.id.doctorNameTextView)
        val doctorSpecialtyTextView: TextView = view.findViewById(R.id.doctorSpecialtyTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctors[position]
        holder.doctorNameTextView.text = doctor.fullName
        holder.doctorSpecialtyTextView.text = doctor.specialty
        holder.itemView.setOnClickListener { onDoctorClick(doctor) }
    }

    override fun getItemCount() = doctors.size

    fun updateDoctors(newDoctors: List<Doctor>) {
        doctors = newDoctors
        notifyDataSetChanged()
    }
}