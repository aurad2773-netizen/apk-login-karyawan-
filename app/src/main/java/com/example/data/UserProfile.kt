package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class RegistrationMethod(val wireValue: String, val label: String) {
    MANUAL("manual", "Registrasi Manual"),
    GOOGLE_QUICK("google_quick", "Login Google");

    companion object {
        fun fromWireValue(raw: String?): RegistrationMethod =
            entries.firstOrNull { it.wireValue == raw } ?: MANUAL
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val occupation: String = "",
    val bio: String = "",
    val registrationMethod: String = RegistrationMethod.MANUAL.wireValue,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class PendingRegistrationDraft(
    val fullName: String,
    val username: String,
    val phone: String,
    val occupation: String,
    val bio: String
)

data class RegistrationValidationErrors(
    val fullNameError: String? = null,
    val usernameError: String? = null,
    val phoneError: String? = null,
    val occupationError: String? = null,
    val bioError: String? = null
) {
    val hasErrors: Boolean
        get() = fullNameError != null ||
            usernameError != null ||
            phoneError != null ||
            occupationError != null ||
            bioError != null
}

object RegistrationFormValidator {
    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_.]+$")

    fun validate(
        fullName: String,
        username: String,
        phone: String,
        occupation: String,
        bio: String
    ): RegistrationValidationErrors {
        val trimmedName = fullName.trim()
        val trimmedUsername = username.trim()
        val trimmedPhone = phone.trim()
        val trimmedOccupation = occupation.trim()
        val trimmedBio = bio.trim()

        val fullNameError = when {
            trimmedName.length < 2 -> "Nama lengkap minimal 2 karakter"
            trimmedName.length > 60 -> "Nama lengkap maksimal 60 karakter"
            else -> null
        }

        val usernameError = when {
            trimmedUsername.length < 3 -> "Username minimal 3 karakter"
            trimmedUsername.length > 30 -> "Username maksimal 30 karakter"
            !USERNAME_REGEX.matches(trimmedUsername) ->
                "Username hanya boleh berisi huruf, angka, titik (.), atau garis bawah (_)"
            else -> null
        }

        val phoneError = if (trimmedPhone.length > 20) {
            "Nomor telepon maksimal 20 karakter"
        } else null

        val occupationError = if (trimmedOccupation.length > 50) {
            "Pekerjaan / peran maksimal 50 karakter"
        } else null

        val bioError = if (trimmedBio.length > 200) {
            "Catatan bio maksimal 200 karakter"
        } else null

        return RegistrationValidationErrors(
            fullNameError = fullNameError,
            usernameError = usernameError,
            phoneError = phoneError,
            occupationError = occupationError,
            bioError = bioError
        )
    }

    fun suggestUsername(displayName: String?, email: String?): String {
        val base = when {
            !email.isNullOrBlank() -> email.substringBefore("@")
            !displayName.isNullOrBlank() -> displayName.lowercase()
            else -> "pengguna"
        }
        val sanitized = base.replace(Regex("[^a-zA-Z0-9_.]"), "_").trim('_')
        return when {
            sanitized.length >= 3 -> sanitized.take(24)
            else -> "user_${sanitized}".take(24)
        }
    }
}
