package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserProfileRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk terlebih dahulu sebelum mengakses data.")
    }

    fun observeProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Sesi pengguna berubah; menolak membaca profil pengguna lain." }
        val docRef = db.collection(COLLECTION_USERS).document(userId)
        val path = docRef.path
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createProfile(
        userId: String,
        fullName: String,
        username: String,
        email: String,
        phone: String,
        occupation: String,
        bio: String,
        registrationMethod: RegistrationMethod
    ): Result<Unit> {
        var docPath = "$COLLECTION_USERS/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Sesi pengguna tidak sesuai; gagal menyimpan registrasi." }

            val cleanName = fullName.trim()
            val cleanUsername = username.trim()
            val cleanEmail = email.trim().take(120)
            val cleanPhone = phone.trim()
            val cleanOccupation = occupation.trim()
            val cleanBio = bio.trim()

            val validation = RegistrationFormValidator.validate(
                fullName = cleanName,
                username = cleanUsername,
                phone = cleanPhone,
                occupation = cleanOccupation,
                bio = cleanBio
            )
            require(!validation.hasErrors) {
                listOfNotNull(
                    validation.fullNameError,
                    validation.usernameError,
                    validation.phoneError,
                    validation.occupationError,
                    validation.bioError
                ).first()
            }

            val docRef = db.collection(COLLECTION_USERS).document(uid)
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "fullName" to cleanName,
                "username" to cleanUsername,
                "email" to cleanEmail,
                "phone" to cleanPhone,
                "occupation" to cleanOccupation,
                "bio" to cleanBio,
                "registrationMethod" to registrationMethod.wireValue,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        userId: String,
        fullName: String,
        username: String,
        phone: String,
        occupation: String,
        bio: String
    ): Result<Unit> {
        var docPath = "$COLLECTION_USERS/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Sesi pengguna tidak sesuai; gagal memperbarui profil." }

            val cleanName = fullName.trim()
            val cleanUsername = username.trim()
            val cleanPhone = phone.trim()
            val cleanOccupation = occupation.trim()
            val cleanBio = bio.trim()

            val validation = RegistrationFormValidator.validate(
                fullName = cleanName,
                username = cleanUsername,
                phone = cleanPhone,
                occupation = cleanOccupation,
                bio = cleanBio
            )
            require(!validation.hasErrors) {
                listOfNotNull(
                    validation.fullNameError,
                    validation.usernameError,
                    validation.phoneError,
                    validation.occupationError,
                    validation.bioError
                ).first()
            }

            val docRef = db.collection(COLLECTION_USERS).document(uid)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "fullName" to cleanName,
                    "username" to cleanUsername,
                    "phone" to cleanPhone,
                    "occupation" to cleanOccupation,
                    "bio" to cleanBio,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    companion object {
        const val COLLECTION_USERS = "users"
    }
}
