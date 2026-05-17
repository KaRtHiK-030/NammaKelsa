package com.karthik.nammakelsa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme
import com.karthik.nammakelsa.ui.theme.brandBackground

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val role = intent.getStringExtra("role") ?: "worker"
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
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brandBackground())
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ─── Logo medallion (replaces the old Person icon) ───────────
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier.size(108.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.auth_welcome),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isLoginMode) stringResource(R.string.auth_login_subtitle)
                else stringResource(R.string.auth_register_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // Segmented mode toggle
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            SegmentTab(
                                selected = isLoginMode,
                                label = stringResource(R.string.action_login),
                                onClick = {
                                    isLoginMode = true
                                    confirmPassword = ""
                                    confirmPasswordError = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                            SegmentTab(
                                selected = !isLoginMode,
                                label = stringResource(R.string.action_register),
                                onClick = { isLoginMode = false },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

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
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    if (isLoginMode) {
                        TextButton(
                            onClick = {
                                if (!validateEmail(email)) return@TextButton
                                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
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
                            Spacer(modifier = Modifier.height(12.dp))
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
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── Submit button — solid emerald, white text, ALWAYS visible ──
                    Button(
                        onClick = {
                            val ok = validateEmail(email) and
                                    validatePassword(password) and
                                    (isLoginMode || validateConfirmPassword(password, confirmPassword))
                            if (!ok) return@Button

                            isLoading = true

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor   = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                text = if (isLoginMode) stringResource(R.string.action_login)
                                else stringResource(R.string.action_register),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SegmentTab(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (selected) MaterialTheme.colorScheme.primary
                    else androidx.compose.ui.graphics.Color.Transparent
    val content   = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = container,
        contentColor = content,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
