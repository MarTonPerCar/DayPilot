package com.example.daypilot.firebaseLogic.authLogic

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // ========== AUTH BÁSICO ==========

    suspend fun register(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== USERNAME ÚNICO ==========

    suspend fun isUsernameAvailable(username: String): Boolean {
        val clean = username.trim().lowercase(Locale.getDefault())
        if (clean.isBlank()) return false

        val snap = firestore.collection("users")
            .whereEqualTo("usernameLower", clean)
            .limit(1)
            .get()
            .await()

        return snap.isEmpty
    }

    suspend fun findUserByUsername(username: String): UserProfile? {
        val clean = username.trim().lowercase(Locale.getDefault())
        if (clean.isBlank()) return null

        val snap = firestore.collection("users")
            .whereEqualTo("usernameLower", clean)
            .limit(1)
            .get()
            .await()

        val doc = snap.documents.firstOrNull() ?: return null
        return doc.toObject(UserProfile::class.java)
    }

    // ========== PERFIL DE USUARIO ==========

    suspend fun saveUserProfile(
        uid: String,
        name: String,
        email: String,
        username: String,
        region: String? = null,
        photoUrl: String? = null
    ) {
        val cleanUsername = username.trim()
        val profile = UserProfile(
            name = name,
            email = email,
            username = cleanUsername,
            usernameLower = cleanUsername.lowercase(Locale.getDefault()),
            region = region ?: "",
            photoUrl = photoUrl
        )

        try {
            firestore.collection("users")
                .document(uid)
                .set(profile, SetOptions.merge())
                .await()

            Log.d("AuthRepo", "Perfil guardado correctamente para $uid")

        } catch (e: Exception) {
            Log.e("AuthRepo", "Error guardando perfil", e)
            throw e
        }
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val snap = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            snap.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error obteniendo perfil", e)
            null
        }
    }

    // ========== AMIGOS ==========

    suspend fun searchUsersByUsernamePrefix(
        query: String,
        limit: Long = 20
    ): List<SearchUserResult> {
        val clean = query.trim().lowercase()
        if (clean.isBlank()) return emptyList()

        val end = clean + '\uf8ff'

        val snap = firestore.collection("users")
            .whereGreaterThanOrEqualTo("usernameLower", clean)
            .whereLessThanOrEqualTo("usernameLower", end)
            .limit(limit)
            .get()
            .await()

        return snap.documents.mapNotNull { doc ->
            val profile = doc.toObject(UserProfile::class.java) ?: return@mapNotNull null
            SearchUserResult(
                uid = doc.id,
                profile = profile
            )
        }
    }

    suspend fun addFriend(currentUid: String, friend: FriendInfo) {
        val userFriendsRef = firestore.collection("users")
            .document(currentUid)
            .collection("friends")
            .document(friend.uid)

        val data = mapOf(
            "friendUid" to friend.uid,
            "friendUsername" to friend.username,
            "friendName" to friend.name,
            "status" to "accepted",
            "since" to System.currentTimeMillis()
        )

        userFriendsRef.set(data).await()
    }

    suspend fun getFriends(uid: String): List<FriendInfo> {
        val snap = firestore.collection("users")
            .document(uid)
            .collection("friends")
            .get()
            .await()

        val basicFriends = snap.documents.mapNotNull { doc ->
            val friendUid = doc.getString("friendUid") ?: return@mapNotNull null
            val username = doc.getString("friendUsername") ?: ""
            val name = doc.getString("friendName") ?: ""

            FriendInfo(
                uid = friendUid,
                username = username,
                name = name
                // SIN photoUrl aquí todavía
            )
        }

        val result = mutableListOf<FriendInfo>()
        for (friend in basicFriends) {
            val profile = getUserProfile(friend.uid)
            result += friend.copy(photoUrl = profile?.photoUrl)
        }

        return result
    }

    // ========== SOLICITUDES DE AMISTAD ==========

    suspend fun sendFriendRequest(
        fromUid: String,
        toUid: String,
        fromUsername: String,
        fromName: String
    ) {
        val requestRef = firestore.collection("users")
            .document(toUid)
            .collection("friendRequests")
            .document(fromUid)

        val data = mapOf(
            "fromUid" to fromUid,
            "fromUsername" to fromUsername,
            "fromName" to fromName,
            "since" to System.currentTimeMillis(),
            "status" to "pending"
        )

        requestRef.set(data).await()
    }

    suspend fun getIncomingFriendRequests(uid: String): List<FriendRequest> {
        val snap = firestore.collection("users")
            .document(uid)
            .collection("friendRequests")
            .get()
            .await()

        return snap.documents.mapNotNull { doc ->
            val base = doc.toObject(FriendRequest::class.java) ?: return@mapNotNull null
            base.copy(fromUid = base.fromUid.ifBlank { doc.id })
        }
    }

    suspend fun acceptFriendRequest(currentUid: String, request: FriendRequest) {
        val currentProfile = getUserProfile(currentUid)
        val requesterProfile = getUserProfile(request.fromUid)

        if (currentProfile == null || requesterProfile == null) {
            throw IllegalStateException("No se pueden obtener los perfiles para aceptar la solicitud")
        }

        addFriend(
            currentUid,
            FriendInfo(
                uid = request.fromUid,
                username = requesterProfile.username,
                name = requesterProfile.name
            )
        )

        addFriend(
            request.fromUid,
            FriendInfo(
                uid = currentUid,
                username = currentProfile.username,
                name = currentProfile.name
            )
        )

        val requestRef = firestore.collection("users")
            .document(currentUid)
            .collection("friendRequests")
            .document(request.fromUid)

        requestRef.delete().await()
    }

    suspend fun declineFriendRequest(currentUid: String, fromUid: String) {
        val requestRef = firestore.collection("users")
            .document(currentUid)
            .collection("friendRequests")
            .document(fromUid)

        requestRef.delete().await()
    }

    // ========== PREFERENCIAS DE USUARIO ==========

    suspend fun updateUserRegion(uid: String, regionId: String) {
        firestore.collection("users")
            .document(uid)
            .update("region", regionId)
            .await()
    }

    suspend fun uploadProfilePhotoFromUri(uid: String, uri: Uri): String {
        val ref = storage.reference.child("profilePhotos/$uid.jpg")

        return try {
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            firestore.collection("users")
                .document(uid)
                .set(
                    mapOf("photoUrl" to downloadUrl),
                    SetOptions.merge()
                )
                .await()

            downloadUrl
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error subiendo foto desde Uri", e)
            throw e
        }
    }

    suspend fun uploadProfilePhotoFromBitmap(uid: String, bitmap: Bitmap): String {
        val ref = storage.reference.child("profilePhotos/$uid.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val data = baos.toByteArray()

        return try {
            ref.putBytes(data).await()

            val downloadUrl = ref.downloadUrl.await().toString()

            firestore.collection("users")
                .document(uid)
                .set(
                    mapOf("photoUrl" to downloadUrl),
                    SetOptions.merge()
                )
                .await()

            downloadUrl
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error subiendo foto desde Bitmap", e)
            throw e
        }
    }

    // ========== RESET / LOGOUT ==========

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(System.currentTimeMillis())

    suspend fun uploadTodayStepsToFirestore(stepsToday: Int, goalSteps: Int? = null): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No hay usuario logueado"))

        return try {
            val day = todayKey()

            val data = mutableMapOf<String, Any>(
                "dayKey" to day,
                "steps" to stepsToday,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (goalSteps != null) data["goalSteps"] = goalSteps

            firestore
                .collection("users")
                .document(uid)
                .collection("daily_steps")
                .document(day)
                .set(data, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}