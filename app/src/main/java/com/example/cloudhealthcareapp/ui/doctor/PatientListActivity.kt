package com.example.cloudhealthcareapp.ui.doctor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.viewmodel.DoctorViewModel

class PatientListActivity : AppCompatActivity() {

    private val viewModel: DoctorViewModel by viewModels()
    private lateinit var newPatientsRecyclerView: RecyclerView
    private lateinit var followUpPatientsRecyclerView: RecyclerView
    private lateinit var newPatientsAdapter: PatientsAdapter
    private lateinit var followUpPatientsAdapter: PatientsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)

        newPatientsRecyclerView = findViewById(R.id.newPatientsRecyclerView)
        followUpPatientsRecyclerView = findViewById(R.id.followUpPatientsRecyclerView)

        // Initialize the adapters with an empty list and a click listener
        newPatientsAdapter = PatientsAdapter(emptyList()) { patient ->
            // Start UserDetailsActivity with patientId and userType
            val intent = Intent(this, UserDetailsActivity::class.java)
            intent.putExtra("userId", patient.userId)
            intent.putExtra("userType", "Patient")
            startActivity(intent)
        }

        followUpPatientsAdapter = PatientsAdapter(emptyList()) { patient ->
            // Start UserDetailsActivity with patientId and userType
            val intent = Intent(this, UserDetailsActivity::class.java)
            intent.putExtra("userId", patient.userId)
            intent.putExtra("userType", "Patient")
            startActivity(intent)
        }

        newPatientsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientListActivity)
            adapter = newPatientsAdapter
        }

        followUpPatientsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientListActivity)
            adapter = followUpPatientsAdapter
        }

        // Observe LiveData for all patients
        viewModel.patients.observe(this) { patients ->
            // Log the number of patients fetched
            Log.d("PatientListActivity", "All patients fetched: ${patients.size}")
            newPatientsAdapter.updatePatients(patients)
        }

        // Observe LiveData for new patients
        viewModel.newPatients.observe(this) { newPatients ->
            // Log the number of new patients fetched
            Log.d("PatientListActivity", "New patients fetched: ${newPatients.size}")
            // newPatientsAdapter.updatePatients(newPatients)
        }

        // Observe LiveData for follow-up patients
        viewModel.followUpPatients.observe(this) { followUpPatients ->
            // Log the number of follow-up patients fetched
            Log.d("PatientListActivity", "Follow-up patients fetched: ${followUpPatients.size}")
            // followUpPatientsAdapter.updatePatients(followUpPatients)
        }

        // Fetch all patients for the doctor
        viewModel.getPatientsForDoctor()

        // Fetch new and follow-up patients (these functions need to be implemented in the ViewModel)
        viewModel.getNewPatients()
        viewModel.getFollowUpPatients()
    }
}