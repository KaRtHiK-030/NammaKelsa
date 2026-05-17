package com.karthik.nammakelsa

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(role: String) {

    val context = LocalContext.current
    val activity = context as? Activity
    val focusManager = LocalFocusManager.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val collectionName = if (role == "worker") "workers" else "hirers"

    var name by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var charge by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isExisting by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var whatsappError by remember { mutableStateOf<String?>(null) }
    var skillError by remember { mutableStateOf<String?>(null) }
    var chargeError by remember { mutableStateOf<String?>(null) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val errName = stringResource(R.string.err_name_required)
    val errNameShort = stringResource(R.string.err_name_short)
    val errLoc = stringResource(R.string.err_location_required)
    val errPhone = stringResource(R.string.err_phone_required)
    val errPhoneShort = stringResource(R.string.err_phone_short)
    val errWa = stringResource(R.string.err_whatsapp_required)
    val errWaShort = stringResource(R.string.err_whatsapp_short)
    val errSkill = stringResource(R.string.err_skill_required)
    val errCharge = stringResource(R.string.err_charge_required)
    val errChargeInvalid = stringResource(R.string.err_charge_invalid)
    val errChargePositive = stringResource(R.string.err_charge_positive)
    val errChargeTooLarge = stringResource(R.string.err_charge_too_large)
    val msgCreated = stringResource(R.string.msg_profile_created)
    val msgUpdated = stringResource(R.string.msg_profile_updated)

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    LaunchedEffect(userId) {
        if (userId.isBlank()) return@LaunchedEffect
        FirebaseFirestore.getInstance()
            .collection(collectionName).document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    isExisting = true
                    name = document.getString("name") ?: ""
                    location = document.getString("location") ?: ""
                    phone = document.getString("phoneNumber") ?: ""
                    whatsapp = document.getString("whatsappNumber") ?: ""
                    imageUrl = document.getString("imageUrl") ?: ""
                    if (role == "worker") {
                        skill = document.getString("skill") ?: ""
                        charge = document.getString("chargePerDay") ?: ""
                    }
                }
            }
    }

    fun validateName(): Boolean = when {
        name.isBlank() -> { nameError = errName; false }
        name.length < 2 -> { nameError = errNameShort; false }
        else -> { nameError = null; true }
    }
    fun validateLocation(): Boolean = when {
        location.isBlank() -> { locationError = errLoc; false }
        else -> { locationError = null; true }
    }
    fun validatePhone(): Boolean = when {
        phone.isBlank() -> { phoneError = errPhone; false }
        phone.length < 10 -> { phoneError = errPhoneShort; false }
        else -> { phoneError = null; true }
    }
    fun validateWhatsapp(): Boolean = when {
        whatsapp.isBlank() -> { whatsappError = errWa; false }
        whatsapp.length < 10 -> { whatsappError = errWaShort; false }
        else -> { whatsappError = null; true }
    }
    fun validateSkill(): Boolean {
        if (role != "worker") return true
        return when {
            skill.isBlank() -> { skillError = errSkill; false }
            else -> { skillError = null; true }
        }
    }
    fun validateCharge(): Boolean {
        if (role != "worker") return true
        val asInt = charge.toIntOrNull()
        return when {
            charge.isBlank() -> { chargeError = errCharge; false }
            asInt == null -> { chargeError = errChargeInvalid; false }
            asInt <= 0 -> { chargeError = errChargePositive; false }
            asInt > 1_000_000 -> { chargeError = errChargeTooLarge; false }
            else -> { chargeError = null; true }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isExisting) stringResource(R.string.profile_edit_title)
                        else stringResource(R.string.profile_create_title)
                    )
                },
                navigationIcon = {
                    if (isExisting) {
                        IconButton(onClick = { activity?.finish() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { padding ->
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
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {

            Text(
                text = stringResource(R.string.profile_complete_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Photo
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val displayUri = imageUri ?: if (imageUrl.isNotBlank()) Uri.parse(imageUrl) else null
                    if (displayUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(displayUri),
                            contentDescription = "Profile photo",
                            modifier = Modifier.size(140.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(140.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { pickImage.launch("image/*") },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.profile_choose_image))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Form
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (nameError != null) validateName()
                        },
                        label = { Text(stringResource(R.string.profile_full_name)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (role == "worker") {
                        OutlinedTextField(
                            value = skill,
                            onValueChange = {
                                skill = it
                                if (skillError != null) validateSkill()
                            },
                            label = { Text(stringResource(R.string.profile_primary_skill)) },
                            leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                            placeholder = { Text(stringResource(R.string.profile_primary_skill_hint)) },
                            isError = skillError != null,
                            supportingText = skillError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    OutlinedTextField(
                        value = location,
                        onValueChange = {
                            location = it
                            if (locationError != null) validateLocation()
                        },
                        label = { Text(stringResource(R.string.profile_location)) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        placeholder = { Text(stringResource(R.string.profile_location_hint)) },
                        isError = locationError != null,
                        supportingText = locationError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            if (it.length <= 15 && it.all { ch -> ch.isDigit() }) {
                                phone = it
                                if (phoneError != null) validatePhone()
                            }
                        },
                        label = { Text(stringResource(R.string.profile_phone)) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        prefix = { Text(stringResource(R.string.profile_phone_prefix)) },
                        isError = phoneError != null,
                        supportingText = phoneError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = {
                            if (it.length <= 15 && it.all { ch -> ch.isDigit() }) {
                                whatsapp = it
                                if (whatsappError != null) validateWhatsapp()
                            }
                        },
                        label = { Text(stringResource(R.string.profile_whatsapp)) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                        prefix = { Text(stringResource(R.string.profile_phone_prefix)) },
                        isError = whatsappError != null,
                        supportingText = whatsappError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = if (role == "worker") ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    if (role == "worker") {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = charge,
                            onValueChange = {
                                if (it.isEmpty() || it.all { ch -> ch.isDigit() }) {
                                    charge = it
                                    if (chargeError != null) validateCharge()
                                }
                            },
                            label = { Text(stringResource(R.string.profile_charge)) },
                            leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                            placeholder = { Text(stringResource(R.string.profile_charge_hint)) },
                            isError = chargeError != null,
                            supportingText = chargeError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val ok = listOf(
                        validateName(), validateLocation(), validatePhone(),
                        validateWhatsapp(), validateSkill(), validateCharge()
                    ).all { it }
                    if (!ok) {
                        scope.launch { snackbar.showSnackbar("Please fix the highlighted fields") }
                        return@Button
                    }
                    if (userId.isBlank()) return@Button
                    isSaving = true
                    saveProfile(
                        userId = userId,
                        collectionName = collectionName,
                        role = role,
                        name = name.trim(),
                        skill = skill.trim(),
                        location = location.trim(),
                        charge = charge.trim(),
                        phone = phone.trim(),
                        whatsapp = whatsapp.trim(),
                        imageUri = imageUri,
                        existingImageUrl = imageUrl,
                        onSuccess = {
                            isSaving = false
                            scope.launch { snackbar.showSnackbar(if (isExisting) msgUpdated else msgCreated) }
                            if (!isExisting) {
                                val intent = Intent(context, HomeActivity::class.java)
                                    .putExtra("role", role)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                            } else {
                                activity?.finish()
                            }
                        },
                        onError = {
                            isSaving = false
                            scope.launch { snackbar.showSnackbar(it) }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isSaving,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isExisting) stringResource(R.string.action_save_changes)
                        else stringResource(R.string.profile_create_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (isExisting) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { activity?.finish() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { Text(stringResource(R.string.action_cancel)) }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun saveProfile(
    userId: String,
    collectionName: String,
    role: String,
    name: String,
    skill: String,
    location: String,
    charge: String,
    phone: String,
    whatsapp: String,
    imageUri: Uri?,
    existingImageUrl: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    fun writeDoc(finalImageUrl: String) {
        val base = mutableMapOf<String, Any>(
            "userId" to userId,
            "role" to role,
            "name" to name,
            "location" to location,
            "phoneNumber" to phone,
            "whatsappNumber" to whatsapp,
            "imageUrl" to finalImageUrl,
            "updatedAt" to System.currentTimeMillis()
        )
        if (role == "worker") {
            base["skill"] = skill
            base["chargePerDay"] = charge
        }
        db.collection(collectionName).document(userId)
            .set(base, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.localizedMessage ?: "Save failed") }
    }

    if (imageUri == null) {
        writeDoc(existingImageUrl)
        return
    }
    val ref = FirebaseStorage.getInstance().reference.child("profileImages/$userId.jpg")
    ref.putFile(imageUri)
        .continueWithTask { ref.downloadUrl }
        .addOnSuccessListener { url -> writeDoc(url.toString()) }
        .addOnFailureListener {
            // If upload fails we still save text fields with the previous URL.
            writeDoc(existingImageUrl)
        }
}
