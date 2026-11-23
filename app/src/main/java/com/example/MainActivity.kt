package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.data.PendingRegistrationDraft
import com.example.data.UserProfileRepository
import com.example.ui.AuthScreen
import com.example.ui.WelcomeScreen
import com.example.ui.WelcomeViewModel
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent(auth: FirebaseAuth = Firebase.auth) {
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var pendingDraft by remember { mutableStateOf<PendingRegistrationDraft?>(null) }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = this
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onPrepareDraft = { draft -> pendingDraft = draft },
            onAuthSuccess = {}
        )
    } else {
        val uid = user.uid
        key(uid) {
            val viewModel: WelcomeViewModel = viewModel(
                key = uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        WelcomeViewModel(
                            repository = UserProfileRepository(db = db, auth = auth),
                            currentUserId = uid
                        )
                    }
                }
            )
            WelcomeScreen(
                viewModel = viewModel,
                userDisplayName = user.displayName,
                userEmail = user.email,
                pendingDraft = pendingDraft,
                onPendingDraftConsumed = { pendingDraft = null },
                onSignedOut = { pendingDraft = null }
            )
        }
    }
}
