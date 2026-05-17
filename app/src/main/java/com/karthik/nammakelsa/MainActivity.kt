package com.karthik.nammakelsa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val role = intent.getStringExtra("role") ?: "worker"

        // Update online status against the correct collection only.
        updateOnlineStatus(role, true)

        setContent {
            NammaKelsaTheme {
                AuthScreen(role)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val role = intent.getStringExtra("role") ?: "worker"
        updateOnlineStatus(role, false)
    }

    private fun updateOnlineStatus(role: String, online: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val collection = if (role == "worker") "workers" else "hirers"
        FirebaseFirestore.getInstance()
            .collection(collection)
            .document(userId)
            .update("online", online)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(role: String) {

    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isLoginMode by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val emailRequired       = stringResource(R.string.err_email_required)
    val emailInvalid        = stringResource(R.string.err_email_invalid)
    val passwordRequired    = stringResource(R.string.err_password_required)
    val passwordShort       = stringResource(R.string.err_password_short)
    val confirmRequired     = stringResource(R.string.err_confirm_password_required)
    val passwordsMismatch   = stringResource(R.string.err_password_mismatch)
    val resetEmailSent      = stringResource(R.string.auth_reset_email_sent)
    val loginFailed         = stringResource(R.string.msg_login_failed)
    val registerFailed      = stringResource(R.string.msg_register_failed)

    fun validateEmail(value: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$".toRegex()
        return when {
            value.isBlank() -> { emailError = emailRequired; false }
            !emailRegex.matches(value) -> { emailError = emailInvalid; false }
            else -> { emailError = null; true }
        }
    }

    fun validatePassword(value: String): Boolean {
        return when {
            value.isBlank() -> { passwordError = passwordRequired; false }
            !isLoginMode && value.length < 6 -> { passwordError = passwordShort; false }
            else -> { passwordError = null; true }
        }
    }

    fun validateConfirmPassword(p: String, c: String): Boolean {
        return when {
            c.isBlank() -> { confirmPasswordError = confirmRequired; false }
            p != c -> { confirmPasswordError = passwordsMismatch; false }
            else -> { confirmPasswordError = null; true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
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
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = if (role == "worker") Icons.Default.Engineering else Icons.Default.BusinessCenter,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.auth_welcome),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isLoginMode) stringResource(R.string.auth_login_subtitle)
                else stringResource(R.string.auth_register_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Row(modifier = Modifier.fillMaxWidth()) {
                        FilledTonalButton(
                            onClick = {
                                isLoginMode = true
                                confirmPassword = ""
                                confirmPasswordError = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isLoginMode)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) { Text(stringResource(R.string.action_login)) }

                        Spacer(modifier = Modifier.width(12.dp))

                        FilledTonalButton(
                            onClick = { isLoginMode = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (!isLoginMode)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) { Text(stringResource(R.string.action_register)) }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError != null) validateEmail(it)
                        },
                        label = { Text(stringResource(R.string.auth_email)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        enabled = !isLoading,
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordError != null) validatePassword(it)
                        },
                        label = { Text(stringResource(R.string.auth_password)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = stringResource(
                                        if (passwordVisible) R.string.auth_hide_password else R.string.auth_show_password
                                    )
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        enabled = !isLoading,
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (isLoginMode) ImeAction.Done else ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { if (isLoginMode) focusManager.clearFocus() },
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    if (isLoginMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                if (!validateEmail(email)) return@TextButton
                                auth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener { task ->
                                        Toast.makeText(
                                            context,
                                            if (task.isSuccessful) resetEmailSent
                                            else task.exception?.localizedMessage ?: loginFailed,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text(stringResource(R.string.auth_forgot_password)) }
                    }

                    AnimatedVisibility(
                        visible = !isLoginMode,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    if (confirmPasswordError != null) validateConfirmPassword(password, it)
                                },
                                label = { Text(stringResource(R.string.auth_confirm_password)) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = stringResource(
                                                if (confirmPasswordVisible) R.string.auth_hide_password else R.string.auth_show_password
                                            )
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                enabled = !isLoading,
                                isError = confirmPasswordError != null,
                                supportingText = confirmPasswordError?.let { { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            val ok = validateEmail(email) and
                                    validatePassword(password) and
                                    (isLoginMode || validateConfirmPassword(password, confirmPassword))
                            if (!ok) return@Button

                            isLoading = true

                            // NOTE: never trim/transform passwords. Trimming changes user input and silently breaks logins.
                            if (isLoginMode) {
                                auth.signInWithEmailAndPassword(email.trim(), password)
                                    .addOnCompleteListener { task ->
                                        if (!task.isSuccessful) {
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                task.exception?.localizedMessage ?: loginFailed,
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@addOnCompleteListener
                                        }
                                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                                        FirebaseFirestore.getInstance()
                                            .collection("users").document(userId).get()
                                            .addOnSuccessListener { userDoc ->
                                                val savedRole = userDoc.getString("role") ?: role
                                                val collection = if (savedRole == "worker") "workers" else "hirers"

                                                FirebaseFirestore.getInstance()
                                                    .collection(collection).document(userId).get()
                                                    .addOnSuccessListener { profile ->
                                                        isLoading = false
                                                        val next = if (profile.exists())
                                                            Intent(context, HomeActivity::class.java)
                                                        else
                                                            Intent(context, ProfileActivity::class.java)
                                                        next.putExtra("role", savedRole)
                                                        next.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        context.startActivity(next)
                                                    }
                                                    .addOnFailureListener {
                                                        isLoading = false
                                                        Toast.makeText(context, it.localizedMessage ?: loginFailed, Toast.LENGTH_LONG).show()
                                                    }
                                            }
                                    }
                            } else {
                                auth.createUserWithEmailAndPassword(email.trim(), password)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                                            FirebaseFirestore.getInstance()
                                                .collection("users").document(userId)
                                                .set(mapOf("role" to role, "createdAt" to System.currentTimeMillis()))
                                            val intent = Intent(context, ProfileActivity::class.java)
                                                .putExtra("role", role)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            context.startActivity(intent)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                task.exception?.localizedMessage ?: registerFailed,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                if (isLoginMode) stringResource(R.string.action_login) else stringResource(R.string.action_register),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isLoginMode) stringResource(R.string.auth_switch_to_register)
                        else stringResource(R.string.auth_switch_to_login),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
