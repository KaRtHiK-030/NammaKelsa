package com.karthik.nammakelsa

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun WorkerListScreen() {

    val db = FirebaseFirestore.getInstance()

    val context = LocalContext.current

    var workers by remember {
        mutableStateOf(listOf<Worker>())
    }

    var filteredWorkers by remember {
        mutableStateOf(listOf<Worker>())
    }

    var searchText by remember {
        mutableStateOf("")
    }

    val listState = rememberLazyListState()

    // LOAD WORKERS
    LaunchedEffect(Unit) {

        db.collection("workers")
            .get()
            .addOnSuccessListener { result ->

                workers =
                    result.toObjects(Worker::class.java)

                filteredWorkers = workers
            }
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

        // TITLE
        Text(
            text = "Available Workers",

            style = MaterialTheme
                .typography
                .headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // SEARCH BAR
        OutlinedTextField(

            value = searchText,

            onValueChange = {

                searchText = it

                filteredWorkers =
                    workers.filter { worker ->

                        worker.name.contains(
                            searchText,
                            ignoreCase = true
                        )

                                ||

                                worker.skill.contains(
                                    searchText,
                                    ignoreCase = true
                                )

                                ||

                                worker.location.contains(
                                    searchText,
                                    ignoreCase = true
                                )

                                ||

                                worker.skillsList.any {

                                    it["skill"]?.contains(
                                        searchText,
                                        ignoreCase = true
                                    ) == true
                                }
                    }
            },

            leadingIcon = {

                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },

            label = {
                Text("Search by name, skill, location")
            },

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Box(
            modifier = Modifier.weight(1f)
        ) {
            // WORKERS LIST
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = 120.dp
                )
            ) {

                items(filteredWorkers) { worker ->

                ElevatedCard(

                    shape = RoundedCornerShape(20.dp),

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {

                    Column(

                        modifier = Modifier
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
                            }

                            .padding(16.dp)
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            // PROFILE IMAGE
                            Image(
                                painter =
                                    rememberAsyncImagePainter(

                                        if (worker.imageUrl.isNotEmpty())
                                            worker.imageUrl
                                        else
                                            "https://i.imgur.com/8Km9tLL.png"
                                    ),

                                contentDescription = null,

                                modifier = Modifier
                                    .size(100.dp)
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
                                    modifier = Modifier.height(8.dp)
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
                                        "₹ ${worker.chargePerDay}/day",

                                    style = MaterialTheme
                                        .typography
                                        .titleMedium
                                )

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                // AVAILABILITY
                                Text(

                                    text = when (
                                        worker.availability
                                    ) {

                                        "Busy" ->
                                            "🔴 Busy"

                                        "Offline" ->
                                            "⚫ Offline"

                                        else ->
                                            "🟢 Available"
                                    }
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        // PHONE
                        Text(
                            text =
                                "📞 ${worker.phoneNumber}"
                        )

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        // BUTTONS
                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {

                            // CALL BUTTON
                            FilledTonalButton(
                                onClick = {

                                    val intent = Intent(
                                        Intent.ACTION_DIAL
                                    )

                                    intent.data = Uri.parse(
                                        "tel:${worker.phoneNumber}"
                                    )

                                    context.startActivity(intent)
                                }
                            ) {

                                Text("Call")
                            }

                            // WHATSAPP BUTTON
                            FilledTonalButton(
                                onClick = {

                                    val url =
                                        "https://wa.me/${worker.whatsappNumber}"

                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(url)
                                    )

                                    context.startActivity(intent)
                                }
                            ) {

                                Text("WhatsApp")
                            }
                        }
                    }
                }
                }
            }
}
    }
}