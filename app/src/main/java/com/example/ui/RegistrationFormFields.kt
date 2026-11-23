package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.RegistrationValidationErrors

@Composable
fun RegistrationFormFields(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    occupation: String,
    onOccupationChange: (String) -> Unit,
    bio: String,
    onBioChange: (String) -> Unit,
    errors: RegistrationValidationErrors,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = fullName,
            onValueChange = { if (it.length <= 60) onFullNameChange(it) },
            label = { Text("Nama Lengkap *") },
            placeholder = { Text("Contoh: Budi Santoso") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Badge,
                    contentDescription = null
                )
            },
            isError = errors.fullNameError != null,
            supportingText = {
                Text(errors.fullNameError ?: "Minimal 2 karakter (${fullName.trim().length}/60)")
            },
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_full_name")
        )

        OutlinedTextField(
            value = username,
            onValueChange = { if (it.length <= 30) onUsernameChange(it) },
            label = { Text("Username *") },
            placeholder = { Text("Contoh: budi_santoso") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.AlternateEmail,
                    contentDescription = null
                )
            },
            isError = errors.usernameError != null,
            supportingText = {
                Text(errors.usernameError ?: "Huruf, angka, titik, atau garis bawah (${username.trim().length}/30)")
            },
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_username")
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 20) onPhoneChange(it) },
            label = { Text("Nomor Telepon (Opsional)") },
            placeholder = { Text("Contoh: 081234567890") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = null
                )
            },
            isError = errors.phoneError != null,
            supportingText = errors.phoneError?.let { msg -> { Text(msg) } },
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_phone")
        )

        OutlinedTextField(
            value = occupation,
            onValueChange = { if (it.length <= 50) onOccupationChange(it) },
            label = { Text("Pekerjaan / Peran (Opsional)") },
            placeholder = { Text("Contoh: Mahasiswa / Pengembang") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.WorkOutline,
                    contentDescription = null
                )
            },
            isError = errors.occupationError != null,
            supportingText = errors.occupationError?.let { msg -> { Text(msg) } },
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_occupation")
        )

        OutlinedTextField(
            value = bio,
            onValueChange = { if (it.length <= 200) onBioChange(it) },
            label = { Text("Catatan / Bio Singkat (Opsional)") },
            placeholder = { Text("Tulis sapaan atau deskripsi singkat tentang Anda...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.EditNote,
                    contentDescription = null
                )
            },
            isError = errors.bioError != null,
            supportingText = {
                Text(errors.bioError ?: "${bio.trim().length}/200 karakter")
            },
            minLines = 2,
            maxLines = 4,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_bio")
        )
    }
}
