package com.example.cloudhealthcareapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudhealthcareapp.models.Appointment
import com.example.cloudhealthcareapp.models.MedicalRecord
import com.example.cloudhealthcareapp.repository.FirebaseRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DoctorViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments

    private val _addDiagnosisResult = MutableLiveData<Boolean>()
    val addDiagnosisResult: LiveData<Boolean> = _addDiagnosisResult

    fun getDoctorAppointments(doctorId: String) {
        viewModelScope.launch {
            try {
                val fetchedAppointments = repository.getDoctorAppointments(doctorId)
                _appointments.postValue(fetchedAppointments)
            } catch (e: Exception) {
                Log.e("DoctorViewModel", "Error fetching appointments: ${e.message}")
            }
        }
    }

    fun getAppointmentsForDoctor() {
        val doctorId = FirebaseAuth.getInstance().currentUser?.uid
        if (doctorId != null) {
            viewModelScope.launch {
                try {
                    val appointments = repository.getAppointmentsForDoctor(doctorId)
                    _appointments.postValue(appointments)
                } catch (e: Exception) {
                    Log.e("DoctorViewModel", "Error fetching appointments: ${e.message}")
                }
            }
        }
    }

    fun addDiagnosis(record: MedicalRecord) {
        viewModelScope.launch {
            try {
                repository.addMedicalRecord(record)
                _addDiagnosisResult.postValue(true)
            } catch (e: Exception) {
                _addDiagnosisResult.postValue(false)
                Log.e("DoctorViewModel", "Error adding diagnosis: ${e.message}")
            }
        }
    }
}