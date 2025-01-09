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
import java.text.SimpleDateFormat
import java.util.*

class PatientViewModel : ViewModel() {
    val repository = FirebaseRepository()

    private val _doctors = MutableLiveData<List<Doctor>>()
    val doctors: LiveData<List<Doctor>> = _doctors

    private val _appointmentBookingResult = MutableLiveData<Boolean>()
    val appointmentBookingResult: LiveData<Boolean> = _appointmentBookingResult

    private val _uploadMedicalRecordResult = MutableLiveData<Boolean>()
    val uploadMedicalRecordResult: LiveData<Boolean> = _uploadMedicalRecordResult

    private val _checkAppointmentConflictResult = MutableLiveData<Boolean>()
    val checkAppointmentConflictResult: LiveData<Boolean> = _checkAppointmentConflictResult

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

    fun checkAppointmentConflict(doctorId: String, appointmentDateTime: String): LiveData<Boolean> {
        val conflictResult = MutableLiveData<Boolean>()
        viewModelScope.launch {
            try {
                val isConflict = repository.checkAppointmentConflict(doctorId, appointmentDateTime)
                conflictResult.postValue(isConflict)
            } catch (e: Exception) {
                Log.e("PatientViewModel", "Error checking appointment conflict: ${e.message}")
                conflictResult.postValue(true) // Assume conflict in case of error
            }
        }
        return conflictResult
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
    fun getAvailableTimes(doctorId: String, selectedDate: String): LiveData<List<String>> {
        val availableTimes = MutableLiveData<List<String>>()
        viewModelScope.launch {
            try {
                val bookedTimes = repository.getBookedTimes(doctorId, selectedDate)
                val allTimes = generateAllTimes(selectedDate)

                // Filter out booked times and times that overlap with booked ranges
                val filteredTimes = allTimes.filter { time ->
                    !bookedTimes.any { bookedTime ->
                        isTimeWithinBookedRange(time, bookedTime, 30)
                    }
                }

                availableTimes.postValue(filteredTimes)
            } catch (e: Exception) {
                Log.e("PatientViewModel", "Error fetching available times: ${e.message}")
                availableTimes.postValue(emptyList())
            }
        }
        return availableTimes
    }

    private fun isTimeWithinBookedRange(time: String, bookedTime: String, duration: Int): Boolean {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeCal = Calendar.getInstance()
        val bookedCal = Calendar.getInstance()
        val endBookedCal = Calendar.getInstance() // Calendar for the end of the booked slot

        try {
            timeCal.time = timeFormat.parse(time) ?: return false
            bookedCal.time = timeFormat.parse(bookedTime) ?: return false
            endBookedCal.time = timeFormat.parse(bookedTime) ?: return false // Initialize endBookedCal
        } catch (e: Exception) {
            Log.e("PatientViewModel", "Error parsing time: ${e.message}")
            return false
        }

        endBookedCal.add(Calendar.MINUTE, duration) // Add the duration to the booked time

        // Check if the time slot starts before the booked slot ends AND ends after the booked slot starts
        return !(timeCal.before(bookedCal) && !timeCal.before(endBookedCal) || timeCal.after(endBookedCal))
    }

    private fun generateAllTimes(selectedDate: String): MutableList<String> {
        val times = mutableListOf<String>()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        if (selectedDate == currentDate) {
            // If selected date is today, start from the next time slot
            calendar.add(Calendar.MINUTE, 30 - calendar.get(Calendar.MINUTE) % 30)
        } else {
            // For future dates, start from the beginning of the day
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
        }

        val endTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }

        val appointmentDuration = 30 // Duration in minutes

        while (calendar.before(endTime)) {
            times.add(timeFormat.format(calendar.time))
            calendar.add(Calendar.MINUTE, appointmentDuration)
        }

        return times
    }

    // ... other code
}