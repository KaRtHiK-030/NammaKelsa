package com.karthik.nammakelsa

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class EditProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            NammaKelsaTheme {

                EditProfileScreen()
            }
        }
    }
}

@Composable
fun EditProfileScreen() {

    val context =
        LocalContext.current

    val userId =
        FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid ?: ""

    var name by remember {
        mutableStateOf("")
    }

    var location by remember {
        mutableStateOf("")
    }

    var imageUrl by remember {
        mutableStateOf("")
    }

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    // GALLERY PICKER
    val launcher =
        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts.GetContent()

        ) { uri ->

            imageUri = uri
        }

    // LOAD PROFILE
    LaunchedEffect(Unit) {

        FirebaseFirestore
            .getInstance()
            .collection("workers")
            .document(userId)
            .get()

            .addOnSuccessListener {

                name =
                    it.getString("name") ?: ""

                location =
                    it.getString("location") ?: ""

                imageUrl =
                    it.getString("imageUrl") ?: ""
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(20.dp)
    ) {

        Text(
            text = "Edit Profile",

            style = MaterialTheme
                .typography
                .headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        ElevatedCard(
            shape = RoundedCornerShape(20.dp),

            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                // PROFILE IMAGE
                Image(
                    painter =
                        rememberAsyncImagePainter(

                            imageUri ?: imageUrl
                        ),

                    contentDescription = null,

                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape),

                    contentScale = ContentScale.Crop
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // IMAGE PICK BUTTON
                Button(
                    onClick = {

                        launcher.launch(
                            "image/*"
                        )
                    }
                ) {

                    Text("Choose Image")
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                // NAME FIELD
                OutlinedTextField(
                    value = name,

                    onValueChange = {
                        name = it
                    },

                    label = {
                        Text("Name")
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // LOCATION FIELD
                OutlinedTextField(
                    value = location,

                    onValueChange = {
                        location = it
                    },

                    label = {
                        Text("Location")
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                // SAVE BUTTON
                Button(

                    onClick = {

                        // IMAGE UPLOAD
                        if (imageUri != null) {

                            val storageRef =

                                FirebaseStorage
                                    .getInstance()
                                    .reference
                                    .child(
                                        "profileImages/$userId.jpg"
                                    )

                            storageRef.putFile(imageUri!!)

                                .continueWithTask {

                                    storageRef.downloadUrl
                                }

                                .addOnSuccessListener {

                                    val uploadedImageUrl =
                                        it.toString()

                                    FirebaseFirestore
                                        .getInstance()
                                        .collection("workers")
                                        .document(userId)

                                        .update(
                                            mapOf(
                                                "name" to name,
                                                "location" to location,
                                                "imageUrl" to uploadedImageUrl
                                            )
                                        )

                                        .addOnSuccessListener {

                                            Toast.makeText(
                                                context,
                                                "Profile Updated ✅",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            (context as? ComponentActivity)
                                                ?.finish()
                                        }
                                }

                        } else {

                            FirebaseFirestore
                                .getInstance()
                                .collection("workers")
                                .document(userId)

                                .update(
                                    mapOf(
                                        "name" to name,
                                        "location" to location
                                    )
                                )

                                .addOnSuccessListener {

                                    Toast.makeText(
                                        context,
                                        "Profile Updated ✅",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    (context as? ComponentActivity)
                                        ?.finish()
                                }
                        }
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Save Changes")
                }
            }
        }

        Spacer(
            modifier = Modifier.height(100.dp)
        )
    }
}