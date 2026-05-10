package com.karthik.nammakelsa

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HirerHomeScreen() {

    val db = FirebaseFirestore.getInstance()

    val context = LocalContext.current

    var workers by remember {
        mutableStateOf(listOf<Worker>())
    }

    var selectedSkill by remember {
        mutableStateOf("")
    }

    var selectedLocation by remember {
        mutableStateOf("")
    }

    var showFilterDialog by remember {
        mutableStateOf(false)
    }

    val listState = rememberLazyListState()

    // LOAD WORKERS
    LaunchedEffect(Unit) {

        db.collection("workers")
            .get()

            .addOnSuccessListener { result ->

                workers =
                    result.toObjects(Worker::class.java)
            }
    }

    // FILTERED LIST
    val filteredWorkers = workers.filter { worker ->

        val skillMatch =

            selectedSkill.isBlank()

                    ||

                    worker.skill.contains(
                        selectedSkill,
                        ignoreCase = true
                    )

                    ||

                    worker.skillsList.any {

                        it["skill"]?.contains(
                            selectedSkill,
                            ignoreCase = true
                        ) == true
                    }

        val locationMatch =

            selectedLocation.isBlank()

                    ||

                    worker.location.contains(
                        selectedLocation,
                        ignoreCase = true
                    )

        skillMatch && locationMatch
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
            .padding(16.dp)
    ) {

        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text = "Available Workers",

                style = MaterialTheme
                    .typography
                    .headlineSmall
            )

            Button(
                onClick = {
                    showFilterDialog = true
                }
            ) {

                Text("Filter")
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // FILTER DIALOG
        if (showFilterDialog) {

            AlertDialog(

                onDismissRequest = {
                    showFilterDialog = false
                },

                title = {
                    Text("Filter Workers")
                },

                text = {

                    Column {

                        OutlinedTextField(
                            value = selectedSkill,

                            onValueChange = {
                                selectedSkill = it
                            },

                            label = {
                                Text("Skill")
                            }
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        OutlinedTextField(
                            value = selectedLocation,

                            onValueChange = {
                                selectedLocation = it
                            },

                            label = {
                                Text("Location")
                            }
                        )
                    }
                },

                confirmButton = {

                    Button(
                        onClick = {
                            showFilterDialog = false
                        }
                    ) {

                        Text("Apply")
                    }
                }
            )
        }

        // WORKERS LIST
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            LazyColumn(

                state = listState,

                modifier = Modifier.fillMaxSize(),

                contentPadding = PaddingValues(
                    bottom = 120.dp
                )
            ) {

                items(filteredWorkers) { worker ->

                    ElevatedCard(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)

                            // OPEN PROFILE
                            .clickable {

                                val intent = Intent(
                                    context,
                                    WorkerDetailActivity::class.java
                                )

                                intent.putExtra(
                                    "workerId",
                                    worker.userId
                                )

                                intent.putExtra(
                                    "name",
                                    worker.name
                                )

                                intent.putExtra(
                                    "skill",
                                    worker.skill
                                )

                                intent.putExtra(
                                    "location",
                                    worker.location
                                )

                                intent.putExtra(
                                    "charge",
                                    worker.chargePerDay
                                )

                                intent.putExtra(
                                    "phone",
                                    worker.phoneNumber
                                )

                                intent.putExtra(
                                    "whatsapp",
                                    worker.whatsappNumber
                                )

                                intent.putExtra(
                                    "imageUrl",
                                    worker.imageUrl
                                )

                                context.startActivity(intent)
                            },

                        shape = RoundedCornerShape(20.dp)
                    ) {

                        Row(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            // PROFILE IMAGE
                            Image(
                                painter =
                                    rememberAsyncImagePainter(
                                        worker.imageUrl
                                    ),

                                contentDescription = null,

                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(
                                        RoundedCornerShape(16.dp)
                                    ),

                                contentScale = ContentScale.Crop
                            )

                            Spacer(
                                modifier = Modifier.width(16.dp)
                            )

                            Column {

                                // NAME
                                Text(
                                    text = worker.name,

                                    style = MaterialTheme
                                        .typography
                                        .titleLarge
                                )

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                // MAIN SKILL
                                Text(
                                    text =
                                        "🛠 ${worker.skill}"
                                )

                                // EXTRA SKILLS
                                if (worker.skillsList.isNotEmpty()) {

                                    Spacer(
                                        modifier = Modifier.height(6.dp)
                                    )

                                    worker.skillsList.forEach {

                                        Text(
                                            text =
                                                "• ${it["skill"]} - ₹${it["charge"]}"
                                        )
                                    }
                                }

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                // LOCATION
                                Text(
                                    text =
                                        "📍 ${worker.location}"
                                )

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                // CHARGE
                                Text(
                                    text =
                                        "₹ ${worker.chargePerDay} per day",

                                    style = MaterialTheme
                                        .typography
                                        .titleMedium
                                )

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                // AVAILABILITY
                                Text(

                                    text = when(worker.availability) {

                                        "Busy" -> "🔴 Busy"

                                        "Offline" -> "⚫ Offline"

                                        else -> "🟢 Available"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}