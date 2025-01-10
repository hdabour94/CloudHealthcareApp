package com.example.cloudhealthcareapp.services

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.cloudhealthcareapp.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppointmentCleanupJobService : JobService() {

    private val jobScope = CoroutineScope(Dispatchers.Main + Job())
    private val repository = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Appointment cleanup job started")

        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            jobScope.launch {
                try {
                    val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
                        Calendar.getInstance().time
                    )
                    // Get expired appointments for both doctors and patients
                    val expiredDoctorAppointments = repository.getExpiredAppointments(currentUserId, "doctorId", currentTime)
                    val expiredPatientAppointments = repository.getExpiredAppointments(currentUserId, "patientId", currentTime)

                    // Update status to "completed" or delete them
                    expiredDoctorAppointments.forEach { appointment ->
                        if (appointment.appointmentId != null) {
                            repository.updateAppointmentStatus(appointment.appointmentId!!, "completed")
                        } else {
                            Log.e(TAG, "Appointment ID is null for an expired appointment")
                        }
                        // Alternatively, delete the appointment: repository.deleteAppointment(appointment.appointmentId!!)
                    }
                    expiredPatientAppointments.forEach { appointment ->
                        if (appointment.appointmentId != null) {
                            repository.updateAppointmentStatus(appointment.appointmentId!!, "completed")
                        } else {
                            Log.e(TAG, "Appointment ID is null for an expired appointment")
                        }
                        // Alternatively, delete the appointment: repository.deleteAppointment(appointment.appointmentId!!)
                    }

                    Log.d(TAG, "Appointment cleanup job completed")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during appointment cleanup: ${e.message}")
                } finally {
                    jobFinished(params, false)
                }
            }
        }

        return true // Indicate that the job is still running
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Appointment cleanup job stopped")
        return true // Indicate whether the job should be rescheduled
    }

    companion object {
        private const val TAG = "AppointmentCleanupJob"
        private const val JOB_ID = 1001 // Unique job ID

        fun scheduleJob(context: Context) {
            val componentName = ComponentName(context, AppointmentCleanupJobService::class.java)
            val builder = JobInfo.Builder(JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)

            // Set periodic execution for every 15 minutes (minimum interval)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setMinimumLatency(15 * 60 * 1000) // 15 minutes for Nougat and above
            } else {
                builder.setPeriodic(15 * 60 * 1000) // 15 minutes for older versions
            }

            val jobScheduler = context.getSystemService(JobScheduler::class.java)
            val resultCode = jobScheduler.schedule(builder.build())
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "Job scheduled!")
            } else {
                Log.e(TAG, "Job scheduling failed")
            }
        }
    }
}