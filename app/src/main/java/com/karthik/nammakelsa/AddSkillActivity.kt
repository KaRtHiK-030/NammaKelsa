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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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

@Composable
fun AddSkillScreen() {

    val context =
        androidx.compose.ui.platform.LocalContext.current

    var skill by remember {
        mutableStateOf("")
    }

    var charge by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Text(
            text = "Add New Skill",

            style = MaterialTheme
                .typography
                .headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            shape = RoundedCornerShape(20.dp),

            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                // SKILL FIELD
                OutlinedTextField(
                    value = skill,

                    onValueChange = {
                        skill = it
                    },

                    label = {
                        Text("Skill")
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // CHARGE FIELD
                OutlinedTextField(
                    value = charge,

                    onValueChange = {
                        charge = it
                    },

                    label = {
                        Text("Charge Per Day")
                    },

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),

                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // SAVE BUTTON
                Button(
                    onClick = {

                        val userId =
                            FirebaseAuth
                                .getInstance()
                                .currentUser
                                ?.uid ?: ""

                        val skillData = mapOf(
                            "skill" to skill,
                            "charge" to charge
                        )

                        FirebaseFirestore
                            .getInstance()
                            .collection("workers")
                            .document(userId)

                            .update(
                                "skillsList",
                                FieldValue.arrayUnion(skillData)
                            )

                            .addOnSuccessListener {

                                Toast.makeText(
                                    context,
                                    "Skill Added ✅",
                                    Toast.LENGTH_LONG
                                ).show()

                                // AUTO CLOSE
                                (context as? ComponentActivity)
                                    ?.finish()
                            }
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Save Skill")
                }
            }
        }
    }
}
