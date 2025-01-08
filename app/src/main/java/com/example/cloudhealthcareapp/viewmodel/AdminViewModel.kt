package com.example.cloudhealthcareapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudhealthcareapp.models.User
import com.example.cloudhealthcareapp.repository.FirebaseRepository
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _filteredUsers = MutableLiveData<List<User>>()
    val filteredUsers: LiveData<List<User>> = _filteredUsers

    private val _activationResult = MutableLiveData<Boolean>()
    val activationResult: LiveData<Boolean> = _activationResult

    private val _deactivationResult = MutableLiveData<Boolean>()
    val deactivationResult: LiveData<Boolean> = _deactivationResult

    private val _deletionResult = MutableLiveData<Boolean>()
    val deletionResult: LiveData<Boolean> = _deletionResult

    private val _userDetails = MutableLiveData<Any>()
    val userDetails: LiveData<Any> = _userDetails

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> = _updateResult

    fun getAllUsers() {
        viewModelScope.launch {
            try {
                val fetchedUsers = repository.getAllUsers()
                _users.postValue(fetchedUsers)
                _filteredUsers.postValue(fetchedUsers) // Initialize filteredUsers with all users
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error fetching users: ${e.message}")
            }
        }
    }

    fun filterUsers(query: String) {
        val allUsers = _users.value ?: emptyList()
        if (query.isEmpty()) {
            _filteredUsers.postValue(allUsers)
        } else {
            val filteredList = allUsers.filter {
                it.fullName.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
            }
            _filteredUsers.postValue(filteredList)
        }
    }

    fun activateUser(userId: String, userType: String) {
        viewModelScope.launch {
            try {
                repository.activateUser(userId, userType)
                _activationResult.postValue(true)
                Log.d("AdminViewModel", "User activated successfully")
            } catch (e: Exception) {
                _activationResult.postValue(false)
                Log.e("AdminViewModel", "Error activating user: ${e.message}")
            }
        }
    }

    fun deactivateUser(userId: String, userType: String) {
        viewModelScope.launch {
            try {
                repository.deactivateUser(userId, userType)
                _deactivationResult.postValue(true)
                Log.d("AdminViewModel", "User deactivated successfully")
            } catch (e: Exception) {
                _deactivationResult.postValue(false)
                Log.e("AdminViewModel", "Error deactivating user: ${e.message}")
            }
        }
    }

    fun deleteUser(userId: String, userType: String) {
        viewModelScope.launch {
            try {
                repository.deleteUser(userId, userType)
                _deletionResult.postValue(true)
                Log.d("AdminViewModel", "User deleted successfully")
            } catch (e: Exception) {
                _deletionResult.postValue(false)
                Log.e("AdminViewModel", "Error deleting user: ${e.message}")
            }
        }
    }
    fun getUserDetails(userId: String, userType: String) {
        viewModelScope.launch {
            try {
                val details = repository.getUserDetails(userId, userType)
                _userDetails.postValue(details)
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error fetching user details: ${e.message}")
            }
        }
    }

    fun updateUser(user: Any, userId: String, userType: String) {
        viewModelScope.launch {
            try {
                repository.updateUser(user, userId, userType)
                _updateResult.postValue(true)
            } catch (e: Exception) {
                _updateResult.postValue(false)
                Log.e("AdminViewModel", "Error updating user: ${e.message}")
            }
        }
    }

}