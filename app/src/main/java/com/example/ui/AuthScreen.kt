package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.onGoogleSignInClicked
import com.example.data.PendingRegistrationDraft
import com.example.data.RegistrationFormValidator
import com.example.data.RegistrationValidationErrors

private enum class AuthTab {
    LOGIN,
    REGISTER_MANUAL
}

@Composable
fun AuthScreen(
    onPrepareDraft: (PendingRegistrationDraft?) -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var selectedTab by rememberSaveable { mutableStateOf(AuthTab.LOGIN) }
    var isLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    // Manual registration fields
    var fullName by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var occupation by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var validationErrors by remember { mutableStateOf(RegistrationValidationErrors()) }
    var hasAttemptedSubmit by rememberSaveable { mutableStateOf(false) }

    fun triggerGoogleSignIn(draft: PendingRegistrationDraft?) {
        authError = null
        isLoading = true
        onPrepareDraft(draft)
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
            },
            onAuthError = { msg ->
                isLoading = false
                onPrepareDraft(null)
                authError = msg
            },
            scope = scope,
            onAuthCancelled = {
                isLoading = false
                onPrepareDraft(null)
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.16f)
                        )
                    )
                )
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Hero Illustration Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(MaterialTheme.shapes.large)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_welcome_hero),
                        contentDescription = stringResource(R.string.welcome_hero_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xB8042F2E)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = "Portal Anggota",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "Selamat Datang",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White
                        )
                        Text(
                            text = "Masuk cepat atau lengkapi registrasi manual Anda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // Mode Switcher Pill
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AuthModePill(
                            label = stringResource(R.string.tab_login),
                            icon = Icons.AutoMirrored.Filled.Login,
                            selected = selectedTab == AuthTab.LOGIN,
                            onClick = {
                                if (!isLoading) {
                                    selectedTab = AuthTab.LOGIN
                                    authError = null
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_login")
                        )
                        AuthModePill(
                            label = stringResource(R.string.tab_register_manual),
                            icon = Icons.Filled.PersonAdd,
                            selected = selectedTab == AuthTab.REGISTER_MANUAL,
                            onClick = {
                                if (!isLoading) {
                                    selectedTab = AuthTab.REGISTER_MANUAL
                                    authError = null
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_register_manual")
                        )
                    }
                }

                // Error Banner
                AnimatedVisibility(visible = authError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = authError.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Tab Content
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "auth_tab_transition"
                    ) { tab ->
                        when (tab) {
                            AuthTab.LOGIN -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(22.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Masuk ke Akun Anda",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Gunakan akun Google untuk masuk secara aman. Jika Anda pengguna baru, Anda akan langsung diarahkan untuk melengkapi profil.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        BenefitRow(
                                            icon = Icons.Filled.Shield,
                                            text = "Autentikasi aman tanpa perlu menghafal kata sandi"
                                        )
                                        BenefitRow(
                                            icon = Icons.Filled.CloudDone,
                                            text = "Data registrasi tersimpan otomatis di Cloud Firestore"
                                        )
                                        BenefitRow(
                                            icon = Icons.Filled.CheckCircle,
                                            text = "Langsung terhubung ke halaman Selamat Datang pribadi"
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Button(
                                        onClick = { triggerGoogleSignIn(draft = null) },
                                        enabled = !isLoading,
                                        shape = MaterialTheme.shapes.medium,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 52.dp)
                                            .testTag("google_sign_in_button")
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(22.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.5.dp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Menghubungkan...")
                                        } else {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Login,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.btn_sign_in_google),
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            selectedTab = AuthTab.REGISTER_MANUAL
                                            authError = null
                                        },
                                        enabled = !isLoading,
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .minimumInteractiveComponentSize()
                                            .testTag("switch_to_register_button")
                                    ) {
                                        Text("Ingin isi data manual dulu? Daftar Manual")
                                    }
                                }
                            }

                            AuthTab.REGISTER_MANUAL -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(22.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Registrasi Manual",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Lengkapi data profil Anda di bawah ini, lalu verifikasi akun dengan Google untuk menyimpannya.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    RegistrationFormFields(
                                        fullName = fullName,
                                        onFullNameChange = {
                                            fullName = it
                                            if (hasAttemptedSubmit) {
                                                validationErrors = RegistrationFormValidator.validate(
                                                    it, username, phone, occupation, bio
                                                )
                                            }
                                        },
                                        username = username,
                                        onUsernameChange = {
                                            username = it
                                            if (hasAttemptedSubmit) {
                                                validationErrors = RegistrationFormValidator.validate(
                                                    fullName, it, phone, occupation, bio
                                                )
                                            }
                                        },
                                        phone = phone,
                                        onPhoneChange = {
                                            phone = it
                                            if (hasAttemptedSubmit) {
                                                validationErrors = RegistrationFormValidator.validate(
                                                    fullName, username, it, occupation, bio
                                                )
                                            }
                                        },
                                        occupation = occupation,
                                        onOccupationChange = {
                                            occupation = it
                                            if (hasAttemptedSubmit) {
                                                validationErrors = RegistrationFormValidator.validate(
                                                    fullName, username, phone, it, bio
                                                )
                                            }
                                        },
                                        bio = bio,
                                        onBioChange = {
                                            bio = it
                                            if (hasAttemptedSubmit) {
                                                validationErrors = RegistrationFormValidator.validate(
                                                    fullName, username, phone, occupation, it
                                                )
                                            }
                                        },
                                        errors = validationErrors,
                                        enabled = !isLoading
                                    )

                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                        shape = MaterialTheme.shapes.small,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Info,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Verifikasi identitas menggunakan Google Sign-In memastikan data registrasi manual Anda hanya dapat diakses oleh Anda.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            hasAttemptedSubmit = true
                                            val errors = RegistrationFormValidator.validate(
                                                fullName = fullName,
                                                username = username,
                                                phone = phone,
                                                occupation = occupation,
                                                bio = bio
                                            )
                                            validationErrors = errors
                                            if (!errors.hasErrors) {
                                                val draft = PendingRegistrationDraft(
                                                    fullName = fullName.trim(),
                                                    username = username.trim(),
                                                    phone = phone.trim(),
                                                    occupation = occupation.trim(),
                                                    bio = bio.trim()
                                                )
                                                triggerGoogleSignIn(draft = draft)
                                            }
                                        },
                                        enabled = !isLoading,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = MaterialTheme.shapes.medium,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 52.dp)
                                            .testTag("manual_register_button")
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(22.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.5.dp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Memverifikasi...")
                                        } else {
                                            Icon(
                                                imageVector = Icons.Filled.PersonAdd,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.btn_register_and_verify),
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthModePill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.surface
    } else {
        Color.Transparent
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        shadowElevation = if (selected) 2.dp else 0.dp,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun BenefitRow(
    icon: ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
