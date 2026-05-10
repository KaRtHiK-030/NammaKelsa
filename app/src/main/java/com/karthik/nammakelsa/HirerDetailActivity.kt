package com.karthik.nammakelsa

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class HirerDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name =
            intent.getStringExtra("name")
                ?: ""

        val imageUrl =
            intent.getStringExtra("imageUrl")
                ?: ""

        val location =
            intent.getStringExtra("location")
                ?: ""

        val phone =
            intent.getStringExtra("phone")
                ?: ""

        val whatsapp =
            intent.getStringExtra("whatsapp")
                ?: ""

        setContent {

            NammaKelsaTheme {

                HirerDetailScreen(
                    name,
                    imageUrl,
                    location,
                    phone,
                    whatsapp
                )
            }
        }
    }
}

@Composable
fun HirerDetailScreen(
    name: String,
    imageUrl: String,
    location: String,
    phone: String,
    whatsapp: String
) {

    val context = LocalContext.current

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
            .padding(20.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Image(
            painter =
                rememberAsyncImagePainter(
                    imageUrl
                ),

            contentDescription = null,

            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape),

            contentScale = ContentScale.Crop
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = name,

            style = MaterialTheme
                .typography
                .headlineMedium
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        ElevatedCard(

            shape = RoundedCornerShape(20.dp),

            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text =
                        "📍 $location"
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text =
                        "📞 $phone"
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Row(

            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Button(

                onClick = {

                    val intent =
                        Intent(
                            Intent.ACTION_DIAL
                        )

                    intent.data =
                        Uri.parse("tel:$phone")

                    context.startActivity(intent)
                },

                modifier = Modifier.weight(1f)
            ) {

                Text("Call")
            }

            Button(

                onClick = {

                    val url =
                        "https://wa.me/$whatsapp"

                    val intent =
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                        )

                    context.startActivity(intent)
                },

                modifier = Modifier.weight(1f)
            ) {

                Text("WhatsApp")
            }
        }
    }
}