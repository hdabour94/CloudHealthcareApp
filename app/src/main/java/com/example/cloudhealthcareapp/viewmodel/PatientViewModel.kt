package com.example.cloudhealthcareapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudhealthcareapp.models.Appointment
import com.example.cloudhealthcareapp.models.Doctor
import com.example.cloudhealthcareapp.models.MedicalRecord
import com.example.cloudhealthcareapp.repository.FirebaseRepository
import kotlinx.coroutines.launch

class PatientViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _doctors = MutableLiveData<List<Doctor>>()
    val doctors: LiveData<List<Doctor>> = _doctors

    private val _appointmentBookingResult = MutableLiveData<Boolean>()
    val appointmentBookingResult: LiveData<Boolean> = _appointmentBookingResult

    private val _uploadMedicalRecordResult = MutableLiveData<Boolean>()
    val uploadMedicalRecordResult: LiveData<Boolean> = _uploadMedicalRecordResult

    fun getDoctors() {
        viewModelScope.launch {
            try {
                val fetchedDoctors = repository.getDoctors()
                _doctors.postValue(fetchedDoctors)
            } catch (e: Exception) {
                // Handle error (e.g., show error message)
                Log.e("PatientViewModel", "Error fetching doctors: ${e.message}")
            }
        }
    }

    fun bookAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                val isBooked = repository.bookAppointment(appointment)
                _appointmentBookingResult.postValue(isBooked)
            } catch (e: Exception) {
                _appointmentBookingResult.postValue(false)
                Log.e("PatientViewModel", "Error booking appointment: ${e.message}")
            }
        }
    }

    fun uploadMedicalRecord(record: MedicalRecord) {
        viewModelScope.launch {
            try {
                repository.addMedicalRecord(record)
                _uploadMedicalRecordResult.postValue(true)
            } catch (e: Exception) {
                _uploadMedicalRecordResult.postValue(false)
                Log.e("PatientViewModel", "Error uploading medical record: ${e.message}")
            }
        }
    }
}