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
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        // LOGO CARD WITH ENHANCED SHADOW
        Card(
            shape = CircleShape,

            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp
            ),
            
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.logo
                ),

                contentDescription = null,

                modifier = Modifier
                    .size(160.dp)
                    .padding(16.dp)
                    .clip(CircleShape),

                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // APP NAME WITH ICON
        Text(
            text = "Namma Kelsa",

            style = MaterialTheme
                .typography
                .headlineLarge,
                
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // SUBTITLE
        Text(
            text = "Connect • Work • Grow",

            style = MaterialTheme
                .typography
                .titleMedium,
                
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(60.dp))
        
        Text(
            text = "Choose your role",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // WORKER CARD BUTTON
        ElevatedCard(
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

            shape = RoundedCornerShape(24.dp),

            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
                
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp
            ),
            
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "👷 I'm a Worker",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Looking for work opportunities",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                Icon(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // HIRER CARD BUTTON
        ElevatedCard(
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

            shape = RoundedCornerShape(24.dp),

            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
                
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp
            ),
            
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "🏢 I'm Hiring",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Find skilled professionals",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                Icon(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}