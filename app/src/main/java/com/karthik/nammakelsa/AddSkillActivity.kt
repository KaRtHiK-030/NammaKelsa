package com.karthik.nammakelsa

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSkillScreen() {

    val context = LocalContext.current
    val activity = context as? Activity
    val focusManager = LocalFocusManager.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var skill by remember { mutableStateOf("") }
    var charge by remember { mutableStateOf("") }
    var skillError by remember { mutableStateOf<String?>(null) }
    var chargeError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val errSkill = stringResource(R.string.err_skill_required)
    val errCharge = stringResource(R.string.err_charge_required)
    val errChargeInvalid = stringResource(R.string.err_charge_invalid)
    val errChargePositive = stringResource(R.string.err_charge_positive)
    val errChargeTooLarge = stringResource(R.string.err_charge_too_large)
    val msgAdded = stringResource(R.string.msg_skill_added)

    fun validateSkill(): Boolean = when {
        skill.isBlank() -> { skillError = errSkill; false }
        else -> { skillError = null; true }
    }
    fun validateCharge(): Boolean {
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
                title = { Text(stringResource(R.string.title_add_skill)) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
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
                .padding(20.dp)
        ) {

            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {

                    OutlinedTextField(
                        value = skill,
                        onValueChange = {
                            skill = it
                            if (skillError != null) validateSkill()
                        },
                        label = { Text(stringResource(R.string.hint_skill)) },
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                        placeholder = { Text(stringResource(R.string.profile_primary_skill_hint)) },
                        isError = skillError != null,
                        supportingText = skillError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

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
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val ok = validateSkill() and validateCharge()
                            if (!ok) return@Button
                            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                            isSaving = true
                            FirebaseFirestore.getInstance()
                                .collection("workers").document(userId)
                                .update(
                                    "skillsList",
                                    FieldValue.arrayUnion(mapOf("skill" to skill.trim(), "charge" to charge.trim()))
                                )
                                .addOnSuccessListener {
                                    isSaving = false
                                    scope.launch { snackbar.showSnackbar(msgAdded) }
                                    activity?.finish()
                                }
                                .addOnFailureListener {
                                    isSaving = false
                                    scope.launch { snackbar.showSnackbar(it.localizedMessage ?: "Failed to add skill") }
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.action_save))
                        }
                    }
                }
            }
        }
    }
}
