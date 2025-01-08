package com.example.cloudhealthcareapp.ui.admin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.User
import com.example.cloudhealthcareapp.ui.RegisterActivity
import com.example.cloudhealthcareapp.viewmodel.AdminViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ManageUsersActivity : AppCompatActivity() {

    private val viewModel: AdminViewModel by viewModels()
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var addUserButton: FloatingActionButton
    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        addUserButton = findViewById(R.id.addUserButton)
        searchEditText = findViewById(R.id.searchEditText)
        usersAdapter = UsersAdapter(
            onActivateClick = { user ->
                viewModel.activateUser(user.userId, user.userType)
            },
            onDeactivateClick = { user ->
                viewModel.deactivateUser(user.userId, user.userType)
            },
            onDeleteClick = { user ->
                showDeleteConfirmationDialog(user)
            },
            onItemClick = { user ->
                val intent = Intent(this, UserDetailsActivity::class.java)
                intent.putExtra("userId", user.userId)
                intent.putExtra("userType", user.userType)
                startActivity(intent)
            }
        )

        usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ManageUsersActivity)
            adapter = usersAdapter
        }

        viewModel.users.observe(this) { users ->
            usersAdapter.updateUsers(users)
        }
        viewModel.filteredUsers.observe(this) { users ->
            usersAdapter.updateUsers(users)
        }

        viewModel.activationResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "User activated successfully", Toast.LENGTH_SHORT).show()
                viewModel.getAllUsers() // Refresh the list
            } else {
                Toast.makeText(this, "Failed to activate user", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.deactivationResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "User deactivated successfully", Toast.LENGTH_SHORT).show()
                viewModel.getAllUsers() // Refresh the list
            } else {
                Toast.makeText(this, "Failed to deactivate user", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.deletionResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show()
                viewModel.getAllUsers() // Refresh the list
            } else {
                Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.getAllUsers()

        addUserButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("isAdmin", true)
            startActivity(intent)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterUsers(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun showDeleteConfirmationDialog(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete ${user.fullName} (${user.userType})?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteUser(user.userId, user.userType)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}