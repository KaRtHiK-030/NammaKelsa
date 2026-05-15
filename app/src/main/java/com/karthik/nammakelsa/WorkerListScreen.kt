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
import androidx.compose.ui.graphics.Color
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
    
    var isLoading by remember {
        mutableStateOf(true)
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
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(16.dp)
    ) {

        // TITLE WITH ICON
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = "👷 ",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Column {
                Text(
                    text = "Available Workers",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "${filteredWorkers.size} skilled professionals",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // SEARCH BAR WITH ENHANCED STYLING
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },

            label = {
                Text("Search by name, skill, location")
            },

            modifier = Modifier.fillMaxWidth(),
            
            shape = RoundedCornerShape(20.dp),
            
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
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
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                if (isLoading) {
                    items(3) {
                        // Loading shimmer cards
                        ElevatedCard(
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            )
                                        )
                                    )
                            )
                        }
                    }
                } else {

                items(filteredWorkers) { worker ->

                ElevatedCard(

                    shape = RoundedCornerShape(24.dp),

                    modifier = Modifier.fillMaxWidth(),
                    
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp
                    ),
                    
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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

                            .padding(20.dp)
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.Top
                        ) {

                            // PROFILE IMAGE WITH BORDER
                            Box {
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
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer
                                        ),

                                    contentScale = ContentScale.Crop
                                )
                                
                                // AVAILABILITY BADGE
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = when (worker.availability) {
                                        "Busy" -> MaterialTheme.colorScheme.errorContainer
                                        "Offline" -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> Color(0xFF00C853).copy(alpha = 0.9f)
                                    }
                                ) {
                                    Text(
                                        text = when (worker.availability) {
                                            "Busy" -> "🔴"
                                            "Offline" -> "⚫"
                                            else -> "🟢"
                                        },
                                        modifier = Modifier.padding(4.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Spacer(
                                modifier = Modifier.width(16.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                // NAME WITH BADGE
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = worker.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // MAIN SKILL WITH BADGE
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "🛠 ${worker.skill}",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                // EXTRA SKILLS
                                if (worker.skillsList.isNotEmpty()) {

                                    Spacer(modifier = Modifier.height(6.dp))

                                    worker.skillsList.take(2).forEach {

                                        Text(
                                            text = "• ${it["skill"]} - ₹${it["charge"]}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // LOCATION
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📍 ${worker.location}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // CHARGE AND CONTACT ROW
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            
                            // CHARGE
                            Column {
                                Text(
                                    text = "Daily Rate",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${worker.chargePerDay}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // BUTTONS
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("📞 Call")
                                }

                                // WHATSAPP BUTTON
                                Button(
                                    onClick = {

                                        val url =
                                            "https://wa.me/${worker.whatsappNumber}"

                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(url)
                                        )

                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF25D366)
                                    )
                                ) {
                                    Text("💬 Chat")
                                }
                            }
                        }
                    }
                }
                }
                }
            }
}
    }
}