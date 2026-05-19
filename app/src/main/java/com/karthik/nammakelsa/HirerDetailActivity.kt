package com.karthik.nammakelsa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class HirerDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hirerId = intent.getStringExtra("hirerId") ?: ""

        setContent {
            NammaKelsaTheme {
                HirerDetailScreen(hirerId)
            }
        }
    }
}

@Composable
fun HirerDetailScreen(hirerId: String) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(hirerId) {
        db.collection("hirers")
            .document(hirerId)
            .get()
            .addOnSuccessListener { doc ->

                name = doc.getString("name") ?: "Hirer"
                location = doc.getString("location") ?: ""
                phone = doc.getString("phoneNumber") ?: ""
                whatsapp = doc.getString("whatsappNumber") ?: ""

                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    if (isLoading) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        InitialAvatar(
            name = name,
            size = 180.dp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text("📍 Location: $location")

                Spacer(modifier = Modifier.height(12.dp))

                Text("📞 Phone: $phone")

                Spacer(modifier = Modifier.height(12.dp))

                Text("💬 WhatsApp: $whatsapp")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = {
                    if (phone.isNotBlank()) {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:$phone")
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Call")
            }

            Button(
                onClick = {
                    if (whatsapp.isNotBlank()) {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://wa.me/$whatsapp")
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("WhatsApp")
            }
        }
    }
}