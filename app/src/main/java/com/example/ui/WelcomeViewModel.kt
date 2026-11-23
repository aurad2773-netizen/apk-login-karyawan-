package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PendingRegistrationDraft
import com.example.data.RegistrationMethod
import com.example.data.UserProfile
import com.example.data.UserProfileRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class WelcomeViewModel(
    private val repository: UserProfileRepository,
    private val currentUserId: String
) : ViewModel() {

    val profileState: StateFlow<UiState<UserProfile?>> = repository.observeProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing user profile", rootError)
            emit(UiState.Error(rootError.message ?: "Gagal memuat profil pengguna"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private var draftApplied = false

    fun applyPendingDraftIfNeeded(
        draft: PendingRegistrationDraft?,
        email: String,
        onConsumed: () -> Unit
    ) {
        if (draft == null || draftApplied || _isSaving.value) return
        draftApplied = true
        submitRegistration(
            fullName = draft.fullName,
            username = draft.username,
            email = email,
            phone = draft.phone,
            occupation = draft.occupation,
            bio = draft.bio,
            registrationMethod = RegistrationMethod.MANUAL,
            onSuccess = {
                _actionMessage.value = "Registrasi manual berhasil disimpan! Selamat datang."
                onConsumed()
            }
        )
    }

    fun submitRegistration(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        occupation: String,
        bio: String,
        registrationMethod: RegistrationMethod = RegistrationMethod.MANUAL,
        onSuccess: () -> Unit = {}
    ) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            _actionError.value = null
            val result = repository.createProfile(
                userId = currentUserId,
                fullName = fullName,
                username = username,
                email = email,
                phone = phone,
                occupation = occupation,
                bio = bio,
                registrationMethod = registrationMethod
            )
            _isSaving.value = false
            result.fold(
                onSuccess = {
                    _actionMessage.value = "Selamat datang! Profil registrasi Anda telah aktif."
                    onSuccess()
                },
                onFailure = { err ->
                    _actionError.value = err.localizedMessage ?: "Gagal menyimpan registrasi profil."
                }
            )
        }
    }

    fun updateProfile(
        fullName: String,
        username: String,
        phone: String,
        occupation: String,
        bio: String,
        onSuccess: () -> Unit = {}
    ) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            _actionError.value = null
            val result = repository.updateProfile(
                userId = currentUserId,
                fullName = fullName,
                username = username,
                phone = phone,
                occupation = occupation,
                bio = bio
            )
            _isSaving.value = false
            result.fold(
                onSuccess = {
                    _actionMessage.value = "Perubahan profil berhasil disimpan."
                    onSuccess()
                },
                onFailure = { err ->
                    _actionError.value = err.localizedMessage ?: "Gagal memperbarui profil."
                }
            )
        }
    }

    fun clearFeedback() {
        _actionMessage.value = null
        _actionError.value = null
    }

    private companion object {
        const val TAG = "WelcomeVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
