package com.example.cloudhealthcareapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudhealthcareapp.models.Appointment
import com.example.cloudhealthcareapp.models.Doctor
import com.example.cloudhealthcareapp.models.MedicalRecord
import com.example.cloudhealthcareapp.models.Patient
import com.example.cloudhealthcareapp.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DoctorViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    private val _patients = MutableLiveData<List<Patient>>()
    val patients: LiveData<List<Patient>> = _patients

    private val _newPatients = MutableLiveData<List<Patient>>()
    val newPatients: LiveData<List<Patient>> = _newPatients

    private val _followUpPatients = MutableLiveData<List<Patient>>()
    val followUpPatients: LiveData<List<Patient>> = _followUpPatients

    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments

    private val _addDiagnosisResult = MutableLiveData<Boolean>()
    val addDiagnosisResult: LiveData<Boolean> = _addDiagnosisResult

    private val _appointmentRequests = MutableLiveData<List<Appointment>>()
    val appointmentRequests: LiveData<List<Appointment>> = _appointmentRequests

    private val _appointmentAcceptanceResult = MutableLiveData<Boolean>()
    val appointmentAcceptanceResult: LiveData<Boolean> = _appointmentAcceptanceResult

    private val _appointmentRejectionResult = MutableLiveData<Boolean>()
    val appointmentRejectionResult: LiveData<Boolean> = _appointmentRejectionResult

    private val _prescriptionSaveResult = MutableLiveData<Boolean>()
    val prescriptionSaveResult: LiveData<Boolean> = _prescriptionSaveResult

    private val _medicalRecords = MutableLiveData<List<MedicalRecord>>()
    val medicalRecords: LiveData<List<MedicalRecord>> = _medicalRecords

    fun getAppointmentsForDoctor() {
        val doctorId = FirebaseAuth.getInstance().currentUser?.uid
        if (doctorId != null) {
            viewModelScope.launch {
                try {
                    val appointments = repository.getAppointmentsForDoctor(doctorId, "upcoming")
                    _appointments.postValue(appointments)
                    Log.d("DoctorViewModel", "Appointments fetched: ${appointments.size}")
                } catch (e: Exception) {
                    Log.e("DoctorViewModel", "Error fetching appointments: ${e.message}")
                }
            }
        }
    }

    fun getPatientsForDoctor() {
        val doctorId = FirebaseAuth.getInstance().currentUser?.uid
        if (doctorId != null) {
            viewModelScope.launch {
                try {
                    val patients = repository.getPatientsForDoctor(doctorId)
                    _patients.postValue(patients)
                } catch (e: Exception) {
                    Log.e("DoctorViewModel", "Error fetching patients: ${e.message}")
                }
            }
        }
    }

    fun getNewPatients() {
        val doctorId = FirebaseAuth.getInstance().currentUser?.uid
        if (doctorId != null) {
            viewModelScope.launch {
                try {
                    val newPatients = repository.getNewPatients(doctorId)
                    _newPatients.postValue(newPatients)
                } catch (e: Exception) {
                    Log.e("DoctorViewModel", "Error fetching new patients: ${e.message}")
                }
            }
        }
    }

    fun getFollowUpPatients() {
        val doctorId = FirebaseAuth.getInstance().currentUser?.uid
        if (doctorId != null) {
            viewModelScope.launch {
                try {
                    val followUpPatients = repository.getFollowUpPatients(doctorId)
                    _followUpPatients.postValue(followUpPatients)
                } catch (e: Exception) {
                    Log.e("DoctorViewModel", "Error fetching follow-up patients: ${e.message}")
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
    fun getAppointmentRequests() {
        val doctorId = FirebaseAuth.getInstance().currentUser?.uid
        if (doctorId != null) {
            viewModelScope.launch {
                try {
                    val requests = repository.getAppointmentRequests(doctorId)
                    _appointmentRequests.postValue(requests)
                } catch (e: Exception) {
                    Log.e("DoctorViewModel", "Error fetching appointment requests: ${e.message}")
                }
            }
        }
    }

    fun acceptAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                repository.updateAppointmentStatus(appointment.appointmentId!!, "accepted")
                _appointmentAcceptanceResult.postValue(true)
                // Send notification to patient
                val patient = repository.getPatient(appointment.patientId!!) // Assuming you have this function
                if (patient != null && patient.fcmToken != null) {
                    repository.sendNotificationToPatient(
                        patient.fcmToken!!,
                        "Appointment Accepted",
                        "Your appointment on ${appointment.appointmentDateTime} has been accepted."
                    )
                }
            } catch (e: Exception) {
                Log.e("DoctorViewModel", "Error accepting appointment: ${e.message}")
                _appointmentAcceptanceResult.postValue(false)
            }
        }
    }

    fun rejectAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                repository.updateAppointmentStatus(appointment.appointmentId!!, "rejected")
                _appointmentRejectionResult.postValue(true)
                // Send notification to patient
                val patient = repository.getPatient(appointment.patientId!!) // Assuming you have this function
                if (patient != null && patient.fcmToken != null) {
                    repository.sendNotificationToPatient(
                        patient.fcmToken!!,
                        "Appointment Rejected",
                        "Your appointment on ${appointment.appointmentDateTime} has been rejected."
                    )
                }
            } catch (e: Exception) {
                Log.e("DoctorViewModel", "Error rejecting appointment: ${e.message}")
                _appointmentRejectionResult.postValue(false)
            }
        }
    }
    fun savePrescription(patientId: String, prescriptionText: String) {
        viewModelScope.launch {
            try {
                repository.savePrescription(patientId, prescriptionText)
                _prescriptionSaveResult.postValue(true)
            } catch (e: Exception) {
                _prescriptionSaveResult.postValue(false)
                Log.e("DoctorViewModel", "Error saving prescription: ${e.message}")
            }
        }
    }
    fun fetchMedicalRecords(patientId: String) {
        viewModelScope.launch {
            try {
                val records = repository.getMedicalRecordsForPatient(patientId)
                _medicalRecords.postValue(records)
            } catch (e: Exception) {
                Log.e("DoctorViewModel", "Error fetching medical records: ${e.message}")
            }
        }
    }
}