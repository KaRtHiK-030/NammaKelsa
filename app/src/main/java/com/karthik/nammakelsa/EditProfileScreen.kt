package com.karthik.nammakelsa

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(role: String) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val collectionName = if (role == "worker") "workers" else "hirers"

    var name by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var charge by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isExistingProfile by remember { mutableStateOf(false) }

    // Validation states
    var nameError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var whatsappError by remember { mutableStateOf<String?>(null) }
    var skillError by remember { mutableStateOf<String?>(null) }
    var chargeError by remember { mutableStateOf<String?>(null) }

    // LOAD EXISTING DATA
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection(collectionName)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    isExistingProfile = true
                    name = document.getString("name") ?: ""
                    location = document.getString("location") ?: ""
                    phone = document.getString("phoneNumber") ?: ""
                    whatsapp = document.getString("whatsappNumber") ?: ""

                    if (role == "worker") {
                        skill = document.getString("skill") ?: ""
                        charge = document.getString("chargePerDay") ?: ""
                    }
                }
            }
    }

    // Validation functions
    fun validateName(): Boolean {
        return when {
            name.isBlank() -> {
                nameError = "Name is required"
                false
            }
            name.length < 2 -> {
                nameError = "Name must be at least 2 characters"
                false
            }
            else -> {
                nameError = null
                true
            }
        }
    }

    fun validateLocation(): Boolean {
        return when {
            location.isBlank() -> {
                locationError = "Location is required"
                false
            }
            else -> {
                locationError = null
                true
            }
        }
    }

    fun validatePhone(): Boolean {
        return when {
            phone.isBlank() -> {
                phoneError = "Phone number is required"
                false
            }
            phone.length < 10 -> {
                phoneError = "Phone number must be at least 10 digits"
                false
            }
            else -> {
                phoneError = null
                true
            }
        }
    }

    fun validateWhatsapp(): Boolean {
        return when {
            whatsapp.isBlank() -> {
                whatsappError = "WhatsApp number is required"
                false
            }
            whatsapp.length < 10 -> {
                whatsappError = "WhatsApp number must be at least 10 digits"
                false
            }
            else -> {
                whatsappError = null
                true
            }
        }
    }

    fun validateSkill(): Boolean {
        if (role != "worker") return true
        return when {
            skill.isBlank() -> {
                skillError = "Skill is required"
                false
            }
            else -> {
                skillError = null
                true
            }
        }
    }

    fun validateCharge(): Boolean {
        if (role != "worker") return true
        return when {
            charge.isBlank() -> {
                chargeError = "Charge per day is required"
                false
            }
            charge.toIntOrNull() == null -> {
                chargeError = "Please enter a valid number"
                false
            }
            charge.toInt() <= 0 -> {
                chargeError = "Charge must be greater than 0"
                false
            }
            else -> {
                chargeError = null
                true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 120.dp
            )
    ) {

        // HEADER
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isExistingProfile) "Edit Profile" else "Create Profile",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Complete your profile information",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // PROFILE FORM CARD
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                // NAME
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) validateName()
                    },
                    label = { Text("Full Name") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // WORKER ONLY - SKILL
                if (role == "worker") {
                    OutlinedTextField(
                        value = skill,
                        onValueChange = {
                            skill = it
                            if (skillError != null) validateSkill()
                        },
                        label = { Text("Primary Skill") },
                        leadingIcon = {
                            Icon(Icons.Default.Work, contentDescription = null)
                        },
                        isError = skillError != null,
                        supportingText = skillError?.let { { Text(it) } },
                        placeholder = { Text("e.g., Carpenter, Plumber, Electrician") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // LOCATION
                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        location = it
                        if (locationError != null) validateLocation()
                    },
                    label = { Text("Location") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    isError = locationError != null,
                    supportingText = locationError?.let { { Text(it) } },
                    placeholder = { Text("e.g., Bangalore, Karnataka") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PHONE
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.length <= 15 && it.all { char -> char.isDigit() }) {
                            phone = it
                            if (phoneError != null) validatePhone()
                        }
                    },
                    label = { Text("Phone Number") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    isError = phoneError != null,
                    supportingText = phoneError?.let { { Text(it) } },
                    prefix = { Text("+91 ") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // WHATSAPP
                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = {
                        if (it.length <= 15 && it.all { char -> char.isDigit() }) {
                            whatsapp = it
                            if (whatsappError != null) validateWhatsapp()
                        }
                    },
                    label = { Text("WhatsApp Number") },
                    leadingIcon = {
                        Icon(Icons.Default.Chat, contentDescription = null)
                    },
                    isError = whatsappError != null,
                    supportingText = whatsappError?.let { { Text(it) } },
                    prefix = { Text("+91 ") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = if (role == "worker") ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                // WORKER ONLY - CHARGE
                if (role == "worker") {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = charge,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                charge = it
                                if (chargeError != null) validateCharge()
                            }
                        },
                        label = { Text("Charge Per Day") },
                        leadingIcon = {
                            Icon(Icons.Default.CurrencyRupee, contentDescription = null)
                        },
                        isError = chargeError != null,
                        supportingText = chargeError?.let { { Text(it) } },
                        placeholder = { Text("e.g., 500, 1000, 1500") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SAVE BUTTON
        Button(
            onClick = {
                val isNameValid = validateName()
                val isLocationValid = validateLocation()
                val isPhoneValid = validatePhone()
                val isWhatsappValid = validateWhatsapp()
                val isSkillValid = validateSkill()
                val isChargeValid = validateCharge()

                if (!isNameValid || !isLocationValid || !isPhoneValid ||
                    !isWhatsappValid || !isSkillValid || !isChargeValid) {
                    Toast.makeText(
                        context,
                        "Please fix all errors",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                isLoading = true

                val updatedData = if (role == "worker") {
                    mapOf(
                        "userId" to userId,
                        "role" to role,
                        "name" to name,
                        "skill" to skill,
                        "location" to location,
                        "chargePerDay" to charge,
                        "phoneNumber" to phone,
                        "whatsappNumber" to whatsapp
                    )
                } else {
                    mapOf(
                        "userId" to userId,
                        "role" to role,
                        "name" to name,
                        "location" to location,
                        "phoneNumber" to phone,
                        "whatsappNumber" to whatsapp
                    )
                }

                FirebaseFirestore.getInstance()
                    .collection(collectionName)
                    .document(userId)
                    .set(updatedData, SetOptions.merge())
                    .addOnSuccessListener {
                        isLoading = false

                        Toast.makeText(
                            context,
                            if (isExistingProfile) "Profile Updated ✅"
                            else "Profile Created ✅",
                            Toast.LENGTH_SHORT
                        ).show()

                        // If new profile, navigate to home
                        if (!isExistingProfile) {
                            val intent = Intent(context, HomeActivity::class.java)
                            intent.putExtra("role", role)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        } else {
                            // If editing, just go back
                            (context as? android.app.Activity)?.finish()
                        }
                    }
                    .addOnFailureListener {
                        isLoading = false
                        Toast.makeText(
                            context,
                            "Error: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isExistingProfile) "Save Changes" else "Create Profile",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // CANCEL BUTTON (only for edit mode)
        if (isExistingProfile) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    (context as? android.app.Activity)?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Cancel", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}