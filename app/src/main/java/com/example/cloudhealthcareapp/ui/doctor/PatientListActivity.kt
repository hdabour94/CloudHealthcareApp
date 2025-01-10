package com.example.cloudhealthcareapp.ui.doctor

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

        newPatientsAdapter = PatientsAdapter(emptyList())
        followUpPatientsAdapter = PatientsAdapter(emptyList())

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
            // Assuming 'patients' is a List<Patient> that contains all patients for the doctor
            newPatientsAdapter.updatePatients(patients)
        }

        // Observe LiveData for new patients
        viewModel.newPatients.observe(this) { newPatients ->
            // This is where you update the adapter with the list of new patients
            // Since new patients are a subset of all patients, this may not be necessary
            // newPatientsAdapter.updatePatients(newPatients)
        }

        // Observe LiveData for follow-up patients
        viewModel.followUpPatients.observe(this) { followUpPatients ->
            // This is where you update the adapter with the list of follow-up patients
            // followUpPatientsAdapter.updatePatients(followUpPatients)
        }

        // Fetch all patients for the doctor
        viewModel.getPatientsForDoctor()

        // Fetch new and follow-up patients (these functions need to be implemented in the ViewModel)
        viewModel.getNewPatients()
        viewModel.getFollowUpPatients()
    }
}