package com.example.daypilot.main.mainZone.rivalry

import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

data class RivalryEntry(
    val uid: String,
    val name: String,
    val username: String,
    val photoUrl: String?,
    val totalPoints: Long,
    val isMe: Boolean
)

data class RivalryResult(
    val me: RivalryEntry,
    val leaderboard: List<RivalryEntry>,
    val myRank: Int,
    val myTodayPoints: Long
)

class RivalryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private suspend fun getTotalPoints(uid: String): Long {
        val snap = firestore.collection("users").document(uid).get().await()
        return (snap.get("totalPoints") as? Number)?.toLong() ?: 0L
    }

    private suspend fun getTodayPoints(uid: String): Long {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now().atStartOfDay(zone).toInstant()
        val end = LocalDate.now().plusDays(1).atStartOfDay(zone).toInstant()

        val startTs = Timestamp(Date.from(start))
        val endTs = Timestamp(Date.from(end))

        val snap = firestore.collection("users")
            .document(uid)
            .collection("pointsLog")
            .whereGreaterThanOrEqualTo("createdAt", startTs)
            .whereLessThan("createdAt", endTs)
            .get()
            .await()

        var sum = 0L
        for (doc in snap.documents) {
            sum += (doc.get("points") as? Number)?.toLong() ?: 0L
        }
        return sum
    }

    suspend fun buildLeaderboard(
        myUid: String,
        authRepo: AuthRepository
    ): RivalryResult = coroutineScope {

        // Yo: perfil + puntos totales + puntos de hoy
        val myProfileDeferred = async { authRepo.getUserProfile(myUid) }
        val myTotalDeferred = async { getTotalPoints(myUid) }
        val myTodayDeferred = async { getTodayPoints(myUid) }

        // Amigos (ya tienes uid/username/name/photoUrl)
        val friends = authRepo.getFriends(myUid)

        val friendEntriesDeferred = friends.map { f ->
            async {
                val points = getTotalPoints(f.uid)
                RivalryEntry(
                    uid = f.uid,
                    name = f.name.ifBlank { f.username.ifBlank { "Amigo" } },
                    username = f.username,
                    photoUrl = f.photoUrl,
                    totalPoints = points,
                    isMe = false
                )
            }
        }

        val myProfile = myProfileDeferred.await()
        val myTotal = myTotalDeferred.await()
        val myToday = myTodayDeferred.await()

        val me = RivalryEntry(
            uid = myUid,
            name = myProfile?.name?.ifBlank { myProfile.username.ifBlank { "Tú" } } ?: "Tú",
            username = myProfile?.username ?: "",
            photoUrl = myProfile?.photoUrl,
            totalPoints = myTotal,
            isMe = true
        )

        val friendEntries = friendEntriesDeferred.awaitAll()

        val all = (listOf(me) + friendEntries)
            .sortedByDescending { it.totalPoints }

        val myRank = all.indexOfFirst { it.uid == myUid }.let { if (it >= 0) it + 1 else 0 }

        RivalryResult(
            me = me,
            leaderboard = all,
            myRank = myRank,
            myTodayPoints = myToday
        )
    }
}