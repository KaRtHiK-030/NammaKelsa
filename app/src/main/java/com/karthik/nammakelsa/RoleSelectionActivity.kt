package com.karthik.nammakelsa

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class RoleSelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            NammaKelsaTheme {

                RoleSelectionScreen()
            }
        }
    }
}

@Composable
fun RoleSelectionScreen() {

    val context =
        androidx.compose.ui.platform.LocalContext.current

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
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        // LOGO CARD
        Card(
            shape = CircleShape,

            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.logo
                ),

                contentDescription = null,

                modifier = Modifier
                    .size(140.dp)
                    .padding(12.dp)
                    .clip(CircleShape),

                // FIXED LOGO LOOK
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // APP NAME
        Text(
            text = "NammaKelsa",

            style = MaterialTheme
                .typography
                .headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        // SUBTITLE
        Text(
            text =
                "Find trusted workers near you",

            style = MaterialTheme
                .typography
                .bodyLarge
        )

        Spacer(modifier = Modifier.height(50.dp))

        // WORKER BUTTON
        ElevatedButton(

            onClick = {

                val intent = Intent(
                    context,
                    MainActivity::class.java
                )

                intent.putExtra(
                    "role",
                    "worker"
                )

                context.startActivity(intent)
            },

            shape = RoundedCornerShape(20.dp),

            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
        ) {

            Text(
                text = "Looking for Work"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // HIRER BUTTON
        ElevatedButton(

            onClick = {

                val intent = Intent(
                    context,
                    MainActivity::class.java
                )

                intent.putExtra(
                    "role",
                    "hirer"
                )

                context.startActivity(intent)
            },

            shape = RoundedCornerShape(20.dp),

            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
        ) {

            Text(
                text = "Hiring Workers"
            )
        }
    }
}