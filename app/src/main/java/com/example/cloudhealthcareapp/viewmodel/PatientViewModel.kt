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
import com.google.firebase.auth.FirebaseAuth
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

    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments

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
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        viewModelScope.launch {
            try {
                val doctor = repository.getDoctor(doctorId)
                val startTimeString = doctor?.startTime ?: "09:00" // Default start time
                val endTimeString = doctor?.endTime ?: "17:00" // Default end time
                val bookedTimes = repository.getBookedTimes(doctorId, selectedDate)

                val startTime = timeFormat.parse(startTimeString)
                val endTime = timeFormat.parse(endTimeString)

                if (startTime == null || endTime == null) {
                    Log.e("PatientViewModel", "Error parsing start/end time for doctor: $doctorId")
                    availableTimes.postValue(emptyList())
                    return@launch
                }

                val startCalendar = Calendar.getInstance().apply {
                    time = startTime
                }
                val endCalendar = Calendar.getInstance().apply {
                    time = endTime
                }

                // Check if the selected date is a day off or a vacation day
                if (isDayOffOrVacation(selectedDate, doctor)) {
                    availableTimes.postValue(emptyList()) // No times available on days off
                } else {
                    val allTimes = generateAllTimes(selectedDate, startCalendar, endCalendar)
                    val filteredTimes = allTimes.filter { time ->
                        !bookedTimes.any { bookedTime ->
                            isTimeWithinBookedRange(time, bookedTime, 30)
                        }
                    }
                    availableTimes.postValue(filteredTimes)
                }
            } catch (e: Exception) {
                Log.e("PatientViewModel", "Error fetching available times: ${e.message}")
                availableTimes.postValue(emptyList())
            }
        }
        return availableTimes
    }


    private fun isDayOffOrVacation(selectedDate: String, doctor: Doctor?): Boolean {
        if (doctor == null) return false

        // Check against the doctor's vacation days
        doctor.vacationDays?.let { vacationDays ->
            if (vacationDays.contains(selectedDate)) {
                return true
            }
        }

        // Check against the doctor's weekly days off
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        calendar.time = sdf.parse(selectedDate) ?: return false
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        doctor.weeklyDaysOff?.let { weeklyDaysOff ->
            if (weeklyDaysOff.contains(dayOfWeek)) {
                return true
            }
        }

        return false
    }

    private fun isTimeWithinBookedRange(time: String, bookedTime: String, duration: Int): Boolean {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeCal = Calendar.getInstance()
        val bookedCal = Calendar.getInstance()
        val endBookedCal = Calendar.getInstance()

        try {
            timeCal.time = timeFormat.parse(time) ?: return false
            bookedCal.time = timeFormat.parse(bookedTime) ?: return false
            endBookedCal.time = timeFormat.parse(bookedTime) ?: return false
            endBookedCal.add(Calendar.MINUTE, duration)
        } catch (e: Exception) {
            Log.e("PatientViewModel", "Error parsing time: ${e.message}")
            return false
        }

        // Check if the start of the time slot is before the end of the booked slot
        // AND the end of the time slot is after the start of the booked slot
        return timeCal.before(endBookedCal) && !timeCal.before(bookedCal)
                || !timeCal.after(endBookedCal) && timeCal.after(bookedCal)
                || timeCal.compareTo(bookedCal) == 0
                || timeCal.compareTo(endBookedCal) == 0
    }

    private fun generateAllTimes(selectedDate: String, startCalendar: Calendar, endCalendar: Calendar): MutableList<String> {
        val times = mutableListOf<String>()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        // Set the calendar to the start of the doctor's working hours for the selected date
        calendar.set(Calendar.HOUR_OF_DAY, startCalendar.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, startCalendar.get(Calendar.MINUTE))
        calendar.set(Calendar.SECOND, 0)

        if (selectedDate == currentDate) {
            val currentTime = Calendar.getInstance()
            // If it's today, start from the next upcoming slot that is at least 30 minutes from now
            if (currentTime.after(calendar)) {
                calendar.time = currentTime.time
                calendar.add(Calendar.MINUTE, 30 - calendar.get(Calendar.MINUTE) % 30)
            }
        }

        // Ensure we don't go past the end time
        val endTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, endCalendar.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
        }

        val appointmentDuration = 30 // Duration in minutes

        // Generate times from the adjusted start time until the end time
        while (calendar.before(endTime)) {
            times.add(timeFormat.format(calendar.time))
            calendar.add(Calendar.MINUTE, appointmentDuration)
        }

        return times
    }

    fun getAppointmentsForPatient() {
        val patientId = FirebaseAuth.getInstance().currentUser?.uid
        if (patientId != null) {
            viewModelScope.launch {
                try {
                    val appointments = repository.getAppointmentsForPatient(patientId)
                    _appointments.postValue(appointments)
                } catch (e: Exception) {
                    Log.e("PatientViewModel", "Error fetching appointments: ${e.message}")
                }
            }
        }
    }

}