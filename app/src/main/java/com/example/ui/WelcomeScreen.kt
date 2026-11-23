package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.auth.signOut
import com.example.data.PendingRegistrationDraft
import com.example.data.RegistrationFormValidator
import com.example.data.RegistrationMethod
import com.example.data.RegistrationValidationErrors
import com.example.data.UserProfile
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel,
    userDisplayName: String?,
    userEmail: String?,
    pendingDraft: PendingRegistrationDraft?,
    onPendingDraftConsumed: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var isEditing by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(profileState, pendingDraft) {
        val currentState = profileState
        if (currentState is UiState.Success && currentState.data == null && pendingDraft != null) {
            viewModel.applyPendingDraftIfNeeded(
                draft = pendingDraft,
                email = userEmail.orEmpty(),
                onConsumed = onPendingDraftConsumed
            )
        } else if (currentState is UiState.Success && currentState.data != null && pendingDraft != null) {
            onPendingDraftConsumed()
        }
    }

    LaunchedEffect(actionMessage, actionError) {
        val msg = actionError ?: actionMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            signOut(
                                credentialManager = credentialManager,
                                scope = scope,
                                onSignOutComplete = onSignedOut
                            )
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.btn_sign_out),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = profileState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Memuat profil Anda...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 520.dp)
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Terjadi Kendala Koneksi",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                signOut(
                                    credentialManager = credentialManager,
                                    scope = scope,
                                    onSignOutComplete = onSignedOut
                                )
                            },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(stringResource(R.string.btn_sign_out))
                        }
                    }
                }

                is UiState.Success -> {
                    val profile = state.data
                    if (profile == null) {
                        CompleteManualRegistrationView(
                            defaultName = userDisplayName.orEmpty(),
                            defaultUsername = RegistrationFormValidator.suggestUsername(
                                userDisplayName,
                                userEmail
                            ),
                            userEmail = userEmail.orEmpty(),
                            isSaving = isSaving,
                            onSubmit = { name, username, phone, occupation, bio ->
                                viewModel.submitRegistration(
                                    fullName = name,
                                    username = username,
                                    email = userEmail.orEmpty(),
                                    phone = phone,
                                    occupation = occupation,
                                    bio = bio,
                                    registrationMethod = RegistrationMethod.MANUAL
                                )
                            }
                        )
                    } else if (isEditing) {
                        BackHandler { isEditing = false }
                        EditProfileView(
                            profile = profile,
                            isSaving = isSaving,
                            onSave = { name, username, phone, occupation, bio ->
                                viewModel.updateProfile(
                                    fullName = name,
                                    username = username,
                                    phone = phone,
                                    occupation = occupation,
                                    bio = bio,
                                    onSuccess = { isEditing = false }
                                )
                            },
                            onCancel = { isEditing = false }
                        )
                    } else {
                        WelcomeDashboardView(
                            profile = profile,
                            onEditClick = { isEditing = true },
                            onSignOutClick = {
                                signOut(
                                    credentialManager = credentialManager,
                                    scope = scope,
                                    onSignOutComplete = onSignedOut
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompleteManualRegistrationView(
    defaultName: String,
    defaultUsername: String,
    userEmail: String,
    isSaving: Boolean,
    onSubmit: (String, String, String, String, String) -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf(defaultName) }
    var username by rememberSaveable { mutableStateOf(defaultUsername) }
    var phone by rememberSaveable { mutableStateOf("") }
    var occupation by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf(RegistrationValidationErrors()) }
    var hasAttempted by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Lengkapi Registrasi Manual",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Akun Google ($userEmail) sudah terverifikasi. Lengkapi data diri Anda untuk masuk ke halaman Selamat Datang.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RegistrationFormFields(
                    fullName = fullName,
                    onFullNameChange = {
                        fullName = it
                        if (hasAttempted) {
                            errors = RegistrationFormValidator.validate(it, username, phone, occupation, bio)
                        }
                    },
                    username = username,
                    onUsernameChange = {
                        username = it
                        if (hasAttempted) {
                            errors = RegistrationFormValidator.validate(fullName, it, phone, occupation, bio)
                        }
                    },
                    phone = phone,
                    onPhoneChange = {
                        phone = it
                        if (hasAttempted) {
                            errors = RegistrationFormValidator.validate(fullName, username, it, occupation, bio)
                        }
                    },
                    occupation = occupation,
                    onOccupationChange = {
                        occupation = it
                        if (hasAttempted) {
                            errors = RegistrationFormValidator.validate(fullName, username, phone, it, bio)
                        }
                    },
                    bio = bio,
                    onBioChange = {
                        bio = it
                        if (hasAttempted) {
                            errors = RegistrationFormValidator.validate(fullName, username, phone, occupation, it)
                        }
                    },
                    errors = errors,
                    enabled = !isSaving
                )

                Button(
                    onClick = {
                        hasAttempted = true
                        val validation = RegistrationFormValidator.validate(
                            fullName, username, phone, occupation, bio
                        )
                        errors = validation
                        if (!validation.hasErrors) {
                            onSubmit(fullName, username, phone, occupation, bio)
                        }
                    },
                    enabled = !isSaving,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("complete_registration_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Menyimpan...")
                    } else {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.btn_save_registration),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeDashboardView(
    profile: UserProfile,
    onEditClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val greeting = remember { timeOfDayGreeting() }
    val initials = remember(profile.fullName) {
        profile.fullName
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "U" }
    }
    val memberSince = remember(profile.createdAt) {
        formatIndonesianDate(profile.createdAt)
    }
    val regMethodLabel = RegistrationMethod.fromWireValue(profile.registrationMethod).label

    Column(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Warm Greeting Header
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.WbSunny,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = "Selamat Datang,\n${profile.fullName}",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Akun Anda aktif dan tersinkronisasi dengan aman.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Digital Member Welcome Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Gradient Identity Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F766E),
                                    Color(0xFF042F2E)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFCCFBF1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF042F2E)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = profile.fullName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Terverifikasi Google",
                                    tint = Color(0xFF5EEAD4),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "@${profile.username}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Surface(
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text(
                                    text = regMethodLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Profile Details Body
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProfileInfoRow(
                        icon = Icons.Outlined.Email,
                        label = "Email Terverifikasi",
                        value = profile.email.ifBlank { "-" }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                    ProfileInfoRow(
                        icon = Icons.Outlined.Phone,
                        label = "Nomor Telepon",
                        value = profile.phone.ifBlank { "Belum ditambahkan" }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                    ProfileInfoRow(
                        icon = Icons.Outlined.WorkOutline,
                        label = "Pekerjaan / Peran",
                        value = profile.occupation.ifBlank { "Belum ditambahkan" }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                    ProfileInfoRow(
                        icon = Icons.Outlined.Notes,
                        label = "Catatan / Bio",
                        value = profile.bio.ifBlank { "Belum ada catatan pribadi" }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                    ProfileInfoRow(
                        icon = Icons.Outlined.CalendarToday,
                        label = "Terdaftar Sejak",
                        value = memberSince
                    )
                }
            }
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onEditClick,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 50.dp)
                    .testTag("edit_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_edit_profile))
            }

            OutlinedButton(
                onClick = onSignOutClick,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 50.dp)
                    .testTag("dashboard_sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_sign_out))
            }
        }
    }
}

@Composable
private fun EditProfileView(
    profile: UserProfile,
    isSaving: Boolean,
    onSave: (String, String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf(profile.fullName) }
    var username by rememberSaveable { mutableStateOf(profile.username) }
    var phone by rememberSaveable { mutableStateOf(profile.phone) }
    var occupation by rememberSaveable { mutableStateOf(profile.occupation) }
    var bio by rememberSaveable { mutableStateOf(profile.bio) }
    var errors by remember { mutableStateOf(RegistrationValidationErrors()) }
    var hasAttempted by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Edit Data Profil",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RegistrationFormFields(
                    fullName = fullName,
                    onFullNameChange = {
                        fullName = it
                        if (hasAttempted) {
                            errors = RegistrationFormValidator.validate(it, username, phone, occupation, bio)
                        }
                    },
                    username = username,
                    onUsernameChange = {
                        username = it
                        if (hasAttempted) {
                            errors = RegistrationFormValidator.validate(fullName, it, phone, occupation, bio)
                        }
                    },
                    phone = phone,
                    onPhoneChange = {
                        phone = it
                        if (hasAttempted) {
                            errors = RegistrationFormValidator.validate(fullName, username, it, occupation, bio)
                        }
                    },
                    occupation = occupation,
                    onOccupationChange = {
                        occupation = it
                        if (hasAttempted) {
                            errors = RegistrationFormValidator.validate(fullName, username, phone, it, bio)
                        }
                    },
                    bio = bio,
                    onBioChange = {
                        bio = it
                        if (hasAttempted) {
                            errors = RegistrationFormValidator.validate(fullName, username, phone, occupation, it)
                        }
                    },
                    errors = errors,
                    enabled = !isSaving
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        enabled = !isSaving,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp)
                            .testTag("cancel_edit_button")
                    ) {
                        Text(stringResource(R.string.btn_cancel))
                    }

                    Button(
                        onClick = {
                            hasAttempted = true
                            val validation = RegistrationFormValidator.validate(
                                fullName, username, phone, occupation, bio
                            )
                            errors = validation
                            if (!validation.hasErrors) {
                                onSave(fullName, username, phone, occupation, bio)
                            }
                        },
                        enabled = !isSaving,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp)
                            .testTag("save_edit_button")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.btn_save_changes))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun timeOfDayGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..10 -> "Selamat Pagi"
        in 11..14 -> "Selamat Siang"
        in 15..18 -> "Selamat Sore"
        else -> "Selamat Malam"
    }
}

private fun formatIndonesianDate(timestamp: Timestamp?): String {
    val date = (timestamp ?: Timestamp.now()).toDate()
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
    return formatter.format(date)
}
