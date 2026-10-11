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
    var phoneNumber by remember { mutableStateOf("+91 98765 43210") }
    var dateOfBirth by remember { mutableStateOf("2001-08-15") }
    var enteredOtp by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }
    var selectedAvatarRes by remember { mutableIntStateOf(R.drawable.fgtime_creator) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showTermsDialog by remember { mutableStateOf(false) }

    // Forgot Password with OTP state
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetPhone by remember { mutableStateOf("") }
    var resetOtpCode by remember { mutableStateOf("") }
    var resetNewPassword by remember { mutableStateOf("") }
    var resetOtpSent by remember { mutableStateOf(false) }
    var resetError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    val avatarOptions = listOf(
        Pair("Creator", R.drawable.fgtime_creator),
        Pair("Leaf Brand", R.drawable.fgtime_leaf_logo),
        Pair("Studio", R.drawable.fgtime_reel),
        Pair("Banner", R.drawable.fgtime_banner)
    )

    fun handleLogin() {
        errorMessage = null
        if (phoneNumber.isBlank() || password.isBlank()) {
            errorMessage = if (isHindi)
                "कृपया मोबाइल/उपयोगकर्ता नाम और पासवर्ड दर्ज करें"
            else
                "Please enter your mobile/username and password."
            return
        }
        viewModel.loginWithCredentials(
            identifier = phoneNumber,
            password = password,
            onError = { err -> errorMessage = err }
        )
    }

    fun handleSignup() {
        errorMessage = null
        if (fullName.isBlank() || username.isBlank() || phoneNumber.isBlank()) {
            errorMessage = if (isHindi)
                "कृपया सभी विवरण भरें"
            else
                "Please fill in all personal details."
            return
        }
        if (!uiState.isOtpVerified) {
            errorMessage = if (isHindi)
                "कृपया पहले मोबाइल ओटीपी सत्यापित करें"
            else
                "Please verify your mobile number with OTP first."
            return
        }
        if (password.length < 6) {
            errorMessage = if (isHindi)
                "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए"
            else
                "Password must be at least 6 characters long."
            return
        }
        if (password != confirmPassword) {
            errorMessage = if (isHindi)
                "पासवर्ड मेल नहीं खा रहे हैं"
            else
                "Passwords do not match. Please verify."
            return
        }
        if (!agreedToTerms) {
            errorMessage = if (isHindi)
                "कृपया नियमों और गोपनीयता नीति को स्वीकार करें"
            else
                "Please agree to the Terms & Privacy Policy to continue."
            return
        }

        viewModel.completeSignup(
            name = fullName,
            username = username,
            phone = phoneNumber,
            password = password,
            dob = dateOfBirth,
            avatarRes = selectedAvatarRes
        )
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
                .padding(horizontal = 22.dp, vertical = 14.dp),
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

            // SIMULATED INCOMING SMS OTP BANNER
            AnimatedVisibility(visible = uiState.activeOtpNotification != null) {
                uiState.activeOtpNotification?.let { notifText ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = FGGreenDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .border(1.5.dp, FGGreenPrimary, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(FGGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Sms, contentDescription = null, tint = Color.Black)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SMS Dispatch Notification",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = FGGreenSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = notifText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(onClick = { viewModel.clearOtpNotification() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // BRAND LOGO & TITLE
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(FGGreenSecondary.copy(alpha = 0.35f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.fgtime_leaf_logo),
                    contentDescription = "FG TIME Brand Logo",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, FGGreenPrimary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "FG TIME",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = FGGreenPrimary
            )

            Text(
                text = if (isHindi)
                    "सच्ची रचनात्मकता और सुरक्षित जुड़ाव का मंच 🌿"
                else
                    "Where genuine creativity & green connections grow 🌿",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // MODE SWITCHER: SIGN UP vs LOG IN
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                        text = if (isHindi) "खाता बनाएं (Sign Up)" else "Sign Up",
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
                        text = if (isHindi) "लॉग इन (Log In)" else "Log In",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Error Message Banner
            AnimatedVisibility(visible = errorMessage != null || uiState.otpError != null) {
                val err = errorMessage ?: uiState.otpError
                err?.let { error ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
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
                // Sign Up: Avatar Selector
                Text(
                    text = if (isHindi) "अपना प्रोफाइल अवतार चुनें" else "Choose Your Profile Avatar",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    avatarOptions.forEach { (label, resId) ->
                        val isSelected = selectedAvatarRes == resId
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedAvatarRes = resId }
                                .padding(4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = label,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) FGGreenPrimary else Color.Gray.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    ),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) FGGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(if (isHindi) "पूरा नाम (Full Name)" else "Full Name") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Badge, contentDescription = null, tint = FGGreenPrimary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mobile Number
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text(if (isHindi) "मोबाइल नंबर (Mobile Number)" else "Mobile Number") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Phone, contentDescription = null, tint = FGGreenPrimary)
                    },
                    trailingIcon = {
                        if (uiState.isOtpVerified) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = FGGreenPrimary)
                        } else {
                            TextButton(
                                onClick = { viewModel.requestSignupOtp(phoneNumber) },
                                enabled = phoneNumber.isNotBlank() && !uiState.isOtpLoading
                            ) {
                                Text("Get OTP", fontWeight = FontWeight.Bold, color = FGGreenPrimary)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // OTP Verification Section
                if (!uiState.isOtpVerified) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Enter 6-Digit OTP Sent to Mobile",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = enteredOtp,
                                    onValueChange = { enteredOtp = it.take(6) },
                                    placeholder = { Text("000000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = { viewModel.verifySignupOtp(enteredOtp) },
                                    enabled = enteredOtp.length == 6 && !uiState.isOtpLoading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FGGreenPrimary,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Verify", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    Surface(
                        color = FGGreenPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = FGGreenPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mobile verified with authentic OTP! ✅",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = FGGreenPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isHindi) "पासवर्ड (Password)" else "Create Password") },
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(if (isHindi) "पासवर्ड की पुष्टि करें" else "Confirm Password") },
                    leadingIcon = {
                        Icon(Icons.Outlined.LockClock, contentDescription = null, tint = FGGreenPrimary)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Terms and Conditions Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agreedToTerms,
                        onCheckedChange = { agreedToTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = FGGreenPrimary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isHindi)
                            "मैं FG TIME के नियमों और गोपनीयता नीति से सहमत हूँ"
                        else
                            "I agree to the Terms of Service & Privacy Policy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { showTermsDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Create Account Button
                Button(
                    onClick = { handleSignup() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_create_account"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FGGreenPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "खाता बनाएं (Create Account)" else "Create FG TIME Account",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

            } else {
                // LOG IN MODE
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text(if (isHindi) "मोबाइल नंबर या उपयोगकर्ता नाम" else "Mobile Number or @Username") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = FGGreenPrimary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Forgot Password link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        resetPhone = phoneNumber
                        showForgotPasswordDialog = true
                    }) {
                        Text(
                            text = if (isHindi) "पासवर्ड भूल गए? (Reset via OTP)" else "Forgot Password? (Reset via OTP)",
                            style = MaterialTheme.typography.bodySmall,
                            color = FGGreenPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { handleLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_login_submit"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FGGreenPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "लॉग इन करें" else "Log In to FG TIME",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Guest / Founder Demo Login
            OutlinedButton(
                onClick = {
                    viewModel.loginWithCredentials(
                        identifier = "muksidul",
                        password = "FGTime@123",
                        onError = {
                            viewModel.completeSignup(
                                name = "Muksidul",
                                username = "muksidul",
                                phone = "+919876543210",
                                password = "FGTime@123",
                                dob = "1999-05-20",
                                avatarRes = R.drawable.fgtime_leaf_logo
                            )
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_founder_demo"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = FGGreenPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(FGGreenPrimary, FGGreenSecondary))
                )
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = FGGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quick Demo / Founder Login (Muksidul)",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // FORGOT PASSWORD WITH REAL OTP DIALOG
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
                resetOtpSent = false
                resetError = null
            },
            title = {
                Text(
                    text = "Reset Password with Mobile OTP",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter your registered mobile number. We will dispatch a 6-digit OTP to verify your identity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = resetPhone,
                        onValueChange = { resetPhone = it },
                        label = { Text("Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = FGGreenPrimary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!resetOtpSent) {
                        Button(
                            onClick = {
                                viewModel.requestPasswordResetOtp(resetPhone) {
                                    resetOtpSent = true
                                }
                            },
                            enabled = resetPhone.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = FGGreenPrimary, contentColor = Color.Black)
                        ) {
                            Text("Send Reset OTP 📲", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedTextField(
                            value = resetOtpCode,
                            onValueChange = { resetOtpCode = it.take(6) },
                            label = { Text("6-Digit OTP Code") },
                            leadingIcon = { Icon(Icons.Default.Sms, contentDescription = null, tint = FGGreenPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = resetNewPassword,
                            onValueChange = { resetNewPassword = it },
                            label = { Text("New Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FGGreenPrimary) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (resetError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = resetError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.verifyAndResetPassword(
                                    phone = resetPhone,
                                    otpCode = resetOtpCode,
                                    newPassword = resetNewPassword,
                                    onSuccess = {
                                        showForgotPasswordDialog = false
                                        resetOtpSent = false
                                    },
                                    onError = { err -> resetError = err }
                                )
                            },
                            enabled = resetOtpCode.length == 6 && resetNewPassword.length >= 6,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = FGGreenPrimary, contentColor = Color.Black)
                        ) {
                            Text("Verify OTP & Set Password 🔒", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Terms of Service & Privacy Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = "FG TIME Privacy & Terms 🌿",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = """
                            Welcome to FG TIME!

                            1. Account Security: Your passwords are encrypted with SHA-256 and never stored in plain text. Mobile verification uses secure 6-digit OTP codes.

                            2. Data Storage: Your profile, posts, photos, video remixes, bookmarks, and private/group messages are persistently stored in your SQLite database.

                            3. Respect & Community: FG TIME is designed as a peaceful, creative, eco-inspired social space. Harassment, spam, and copyright infringement are strictly prohibited.

                            4. Media & Video Remixing: The Remix Studio allows cutting and syncing licensed lo-fi and ambient music tracks. You retain ownership of your authentic original content.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        agreedToTerms = true
                        showTermsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FGGreenPrimary, contentColor = Color.Black)
                ) {
                    Text("Accept & Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
