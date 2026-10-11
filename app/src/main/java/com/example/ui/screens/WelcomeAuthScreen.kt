package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FGGreenDark
import com.example.ui.theme.FGGreenPrimary
import com.example.ui.theme.FGGreenSecondary
import com.example.viewmodel.FGTimeViewModel
import com.example.viewmodel.Language

@Composable
fun WelcomeAuthScreen(
    viewModel: FGTimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isHindi = uiState.language == Language.HINDI

    var isLoginMode by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("2001-08-15") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var recoveryEmail by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    fun validateAndSubmit() {
        errorMessage = null
        if (isLoginMode) {
            if (contact.isBlank() || password.isBlank()) {
                errorMessage = if (isHindi)
                    "कृपया ईमेल/फ़ोन और पासवर्ड दर्ज करें / Please enter contact and password."
                else
                    "Please enter your email or phone and password."
                return
            }
            viewModel.performLogin(
                name = fullName.ifBlank { "FG Member" },
                username = username.ifBlank { "member_fg" },
                contact = contact,
                dob = dateOfBirth
            )
        } else {
            // Sign Up validation
            if (fullName.isBlank() || username.isBlank() || contact.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                errorMessage = if (isHindi)
                    "कृपया सभी आवश्यक फ़ील्ड भरें / Please fill all required fields."
                else
                    "Please fill in all required fields."
                return
            }
            if (password.length < 6) {
                errorMessage = if (isHindi)
                    "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए / Password must be at least 6 characters."
                else
                    "Password must be at least 6 characters long."
                return
            }
            if (password != confirmPassword) {
                errorMessage = if (isHindi)
                    "पासवर्ड मेल नहीं खा रहे हैं / Passwords do not match."
                else
                    "Passwords do not match. Please check again."
                return
            }
            if (!agreedToTerms) {
                errorMessage = if (isHindi)
                    "कृपया जारी रखने के लिए नियमों और शर्तों को स्वीकार करें / Please agree to Terms & Privacy."
                else
                    "Please accept the Terms of Service and Privacy Policy to continue."
                return
            }
            viewModel.performLogin(fullName, username, contact, dateOfBirth)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (uiState.isDarkMode) listOf(
                        Color(0xFF07150F),
                        Color(0xFF0F261B),
                        Color(0xFF133626)
                    ) else listOf(
                        Color(0xFFE8F6EE),
                        Color(0xFFF4FAF6),
                        Color(0xFFFFFFFF)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Controls: Language & Theme
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Switcher
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.clickable { viewModel.toggleLanguage() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            tint = FGGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "English" else "हिन्दी",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Theme Switcher
                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier.testTag("auth_theme_toggle")
                ) {
                    Icon(
                        imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = FGGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // FG TIME Leaf Logo Emblem
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(FGGreenPrimary.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
                    .border(2.dp, FGGreenPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.fgtime_leaf_logo),
                    contentDescription = "FG TIME Logo",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Brand Title & Creator Note
            Text(
                text = "FG TIME",
                style = MaterialTheme.typography.headlineMedium.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Black
                ),
                color = if (uiState.isDarkMode) Color.White else FGGreenDark
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Created by Muksidul",
                    style = MaterialTheme.typography.labelSmall,
                    color = FGGreenSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Verified, contentDescription = null, tint = FGGreenPrimary, modifier = Modifier.size(14.dp))
            }

            Text(
                text = if (isHindi) "सकारात्मक जुड़ाव और रचनात्मकता का मंच 🌿" else "Growth, Creativity & Authentic Connection 🌿",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Demo Mode Notice Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FGGreenPrimary.copy(alpha = 0.12f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(FGGreenPrimary, FGGreenSecondary))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Demo Mode",
                        tint = FGGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi)
                            "डेमो मोड: स्थानीय सुरक्षित सत्र। सभी सामाजिक सुविधाएं, कॉल्स और रीमिक्स सक्रिय हैं।"
                        else
                            "Interactive Demo: Full audio/video calling, reels, remix studio, and AI features active.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Selector Tabs (Sign Up vs Login)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .padding(4.dp)
            ) {
                Button(
                    onClick = {
                        isLoginMode = false
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isLoginMode) FGGreenPrimary else Color.Transparent,
                        contentColor = if (!isLoginMode) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        text = if (isHindi) "खाता बनाएं" else "Create Account",
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        isLoginMode = true
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLoginMode) FGGreenPrimary else Color.Transparent,
                        contentColor = if (isLoginMode) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        text = if (isHindi) "लॉग इन करें" else "Log In",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Error Message Banner
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { error ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // FORM FIELDS
            if (!isLoginMode) {
                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(if (isHindi) "पूरा नाम (Full Name)" else "Full Name") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Badge, contentDescription = null, tint = FGGreenPrimary)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_full_name"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' } },
                    label = { Text(if (isHindi) "उपयोगकर्ता नाम (Username)" else "Username") },
                    prefix = { Text("@", color = FGGreenPrimary) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = FGGreenPrimary)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_username"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date of Birth
                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = { Text(if (isHindi) "जन्म तिथि (Date of Birth)" else "Date of Birth (YYYY-MM-DD)") },
                    leadingIcon = {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = FGGreenPrimary)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_dob"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Email or Phone
            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text(if (isHindi) "ईमेल या फ़ोन नंबर (Email or Phone)" else "Email or Phone Number") },
                leadingIcon = {
                    Icon(Icons.Outlined.Email, contentDescription = null, tint = FGGreenPrimary)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_contact"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(if (isHindi) "पासवर्ड (Password)" else "Password") },
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = FGGreenPrimary)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_password"),
                shape = RoundedCornerShape(14.dp)
            )

            if (isLoginMode) {
                // Forgot Password link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = if (isHindi) "पासवर्ड भूल गए? (Forgot Password)" else "Forgot Password?",
                        style = MaterialTheme.typography.labelMedium,
                        color = FGGreenSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            recoveryEmail = contact
                            showForgotPasswordDialog = true
                        }
                    )
                }
            }

            if (!isLoginMode) {
                Spacer(modifier = Modifier.height(10.dp))

                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(if (isHindi) "पासवर्ड की पुष्टि करें (Confirm Password)" else "Confirm Password") },
                    leadingIcon = {
                        Icon(Icons.Outlined.LockReset, contentDescription = null, tint = FGGreenPrimary)
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_confirm_password"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Terms and Privacy Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { agreedToTerms = !agreedToTerms },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agreedToTerms,
                        onCheckedChange = { agreedToTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = FGGreenPrimary),
                        modifier = Modifier.testTag("checkbox_terms")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHindi)
                            "मैं FG TIME के नियमों और गोपनीयता नीति से सहमत हूँ"
                        else
                            "I agree to FG TIME Terms and Privacy Policy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Action Button
            Button(
                onClick = { validateAndSubmit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_auth_submit"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FGGreenPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (isLoginMode) Icons.Default.Login else Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLoginMode) {
                        if (isHindi) "लॉग इन करें" else "Log In"
                    } else {
                        if (isHindi) "खाता बनाएं और शुरू करें" else "Create Account"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Founder Review Button (Log in as Muksidul)
            Button(
                onClick = {
                    viewModel.performLogin("Muksidul", "muksidul", "muksidul@fgtime.com")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_login_muksidul"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = FGGreenPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = FGGreenPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sign in as Creator Muksidul (Founder Access)",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Guest Preview Button
            OutlinedButton(
                onClick = {
                    viewModel.performLogin("Aarav Sharma", "aarav_fg", "aarav@fgtime.com")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_guest_preview"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = FGGreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isHindi) "त्वरित डेमो पूर्वावलोकन (Guest Demo)" else "Explore as Guest Preview",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Forgot Password Dialog
        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = { Text("Account Recovery & Password Reset", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Enter your registered email address or phone number to receive a secure recovery verification code.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = recoveryEmail,
                            onValueChange = { recoveryEmail = it },
                            label = { Text("Email or Phone") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (recoveryEmail.isNotBlank()) {
                                viewModel.recoverAccount(recoveryEmail)
                                showForgotPasswordDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FGGreenPrimary)
                    ) {
                        Text("Send Reset Link", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
