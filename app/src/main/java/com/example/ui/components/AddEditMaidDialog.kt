package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Maid
import com.example.domain.SalaryCalculator
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LeaveRed
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AddEditMaidDialog(
    maidToEdit: Maid?,
    onDismiss: () -> Unit,
    onSave: (id: Long, name: String, role: String, salary: Double, freeLeaves: Int) -> Boolean
) {
    var name by remember(maidToEdit) { mutableStateOf(maidToEdit?.name ?: "") }
    var role by remember(maidToEdit) { mutableStateOf(maidToEdit?.role ?: "Cooking & Cleaning") }
    var salaryStr by remember(maidToEdit) {
        mutableStateOf(maidToEdit?.monthlySalary?.toInt()?.toString() ?: "5000")
    }
    var freeLeavesStr by remember(maidToEdit) {
        mutableStateOf(maidToEdit?.freeLeavesPerMonth?.toString() ?: "2")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_edit_maid_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (maidToEdit == null) "Add New Maid" else "Edit Maid Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Maid Name *") },
                    singleLine = true,
                    colors = textFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("maid_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Role field
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role / Tasks") },
                    placeholder = { Text("e.g. Cooking, Housekeeping") },
                    singleLine = true,
                    colors = textFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("maid_role_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Monthly Salary in INR
                OutlinedTextField(
                    value = salaryStr,
                    onValueChange = {
                        salaryStr = it
                        errorMessage = null
                    },
                    label = { Text("Monthly Salary (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = textFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("maid_salary_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Free Leaves Per Month (0 to 30)
                OutlinedTextField(
                    value = freeLeavesStr,
                    onValueChange = {
                        freeLeavesStr = it
                        errorMessage = null
                    },
                    label = { Text("Free Leaves Per Month (0 - 30) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = "Leaves allowed per month without salary deduction (0-30)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    },
                    colors = textFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("maid_free_leaves_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = LeaveRed
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val salary = salaryStr.toDoubleOrNull()
                            if (salary == null || salary < 0) {
                                errorMessage = "Please enter a valid non-negative salary."
                                return@Button
                            }
                            val freeLeaves = freeLeavesStr.toIntOrNull()
                            if (freeLeaves == null || freeLeaves < 0 || freeLeaves > 30) {
                                errorMessage = "Free leaves must be a whole number between 0 and 30."
                                return@Button
                            }
                            if (name.isBlank()) {
                                errorMessage = "Maid name cannot be empty."
                                return@Button
                            }

                            val success = onSave(
                                maidToEdit?.id ?: 0L,
                                name,
                                role,
                                salary,
                                freeLeaves
                            )
                            if (!success) {
                                // Error will be set or snackbar shown by caller
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryAccent,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.testTag("save_maid_button")
                    ) {
                        Text("Save Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryAccent,
    unfocusedBorderColor = DarkOutline,
    focusedLabelColor = PrimaryAccent,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = PrimaryAccent
)
