package com.karthik.nammakelsa

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class AddSkillActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NammaKelsaTheme {
                AddSkillScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSkillScreen() {

    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    if (currentUser == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(
                context,
                "Please login first",
                Toast.LENGTH_LONG
            ).show()
            activity?.finish()
        }
        return
    }

    val userId = currentUser.uid

    var skill by remember { mutableStateOf("") }
    var charge by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var roleChecked by remember { mutableStateOf(false) }
    var isWorker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                isWorker = doc.getString("role") == "worker"
                roleChecked = true

                if (!isWorker) {
                    Toast.makeText(
                        context,
                        "Only workers can add skills",
                        Toast.LENGTH_LONG
                    ).show()
                    activity?.finish()
                }
            }
            .addOnFailureListener {
                roleChecked = true
                Toast.makeText(
                    context,
                    "Failed to verify user role",
                    Toast.LENGTH_LONG
                ).show()
                activity?.finish()
            }
    }

    if (!roleChecked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBgBrush()),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add New Skill")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            activity?.finish()
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBgBrush())
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(20.dp)
        ) {

            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    OutlinedTextField(
                        value = skill,
                        onValueChange = {
                            skill = it
                        },
                        label = {
                            Text("Skill")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = charge,
                        onValueChange = {
                            charge = it.filter { char ->
                                char.isDigit()
                            }
                        },
                        label = {
                            Text("Charge Per Day")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {

                            if (skill.trim().isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Enter a skill",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            if (charge.trim().isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Enter charge amount",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            isSaving = true

                            firestore.collection("workers")
                                .document(userId)
                                .get()
                                .addOnSuccessListener { doc ->

                                    val rawSkills =
                                        doc.get("skillsList") as? List<*>

                                    val existingSkills =
                                        rawSkills?.mapNotNull { item ->
                                            item as? Map<String, String>
                                        } ?: emptyList()

                                    val duplicate =
                                        existingSkills.any {
                                            it["skill"]
                                                ?.equals(
                                                    skill.trim(),
                                                    ignoreCase = true
                                                ) == true
                                        }

                                    if (duplicate) {
                                        isSaving = false

                                        Toast.makeText(
                                            context,
                                            "Skill already exists",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        return@addOnSuccessListener
                                    }

                                    val updatedSkills =
                                        existingSkills + mapOf(
                                            "skill" to skill.trim(),
                                            "charge" to charge.trim()
                                        )

                                    firestore.collection("workers")
                                        .document(userId)
                                        .set(
                                            mapOf(
                                                "skillsList" to updatedSkills
                                            ),
                                            SetOptions.merge()
                                        )
                                        .addOnSuccessListener {
                                            isSaving = false

                                            Toast.makeText(
                                                context,
                                                "Skill Added Successfully ✅",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            activity?.finish()
                                        }
                                        .addOnFailureListener {
                                            isSaving = false

                                            Toast.makeText(
                                                context,
                                                "Failed to save skill",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                                .addOnFailureListener {
                                    isSaving = false

                                    Toast.makeText(
                                        context,
                                        "Failed to load existing skills",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    ) {

                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Skill")
                        }
                    }
                }
            }
        }
    }
}