package com.example.fit5046_lab4_group3_ass2.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Server-generated task shown in Rewards */
data class GeneratedTask(
    val templateId: String = "",
    val title: String = "",
    val description: String = "",
    val points: Int = 0,
    val cadence: String = "daily",    // "daily" | "weekly"
    val periodKey: String = "",       // "yyyy-MM-dd" or "yyyy-Www"
    val claimed: Boolean = false
)

/** A claim record written by the app when the user taps "Claim" */
data class RewardEntry(
    val taskId: String = "",
    val taskTitle: String = "",
    val points: Int = 0,
    val timestamp: Long = 0L,         // millis since epoch
    val key: String = ""              // doc id in users/{uid}/rewards
)

class RewardsRepo(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) {
    /* ---------------------- period keys (must match CF) -------------------- */
    fun todayKey(date: Date = Date()): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)

    fun weekKey(date: Date = Date()): String =
        SimpleDateFormat("yyyy-'W'ww", Locale.getDefault()).format(date)

    /* ------------------------------ listeners ------------------------------ */

    /**
     * Listen to claim documents under users/{uid}/rewards and map to RewardEntry list.
     * Returns a cancel() lambda you must call on dispose.
     */
    fun listenClaims(
        onChange: (List<RewardEntry>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): () -> Unit {
        val uid = auth.currentUser?.uid ?: return { }
        val reg: ListenerRegistration = db.collection("users").document(uid)
            .collection("rewards")
            .addSnapshotListener { snap, e ->
                if (e != null) { onError(e); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { d ->
                    val rawTs = d.get("timestamp")
                    val tsMillis: Long = when (rawTs) {
                        is Long -> rawTs
                        is Timestamp -> rawTs.toDate().time
                        else -> 0L
                    }
                    val taskId = d.getString("taskId") ?: return@mapNotNull null
                    RewardEntry(
                        taskId = taskId,
                        taskTitle = d.getString("taskTitle") ?: "",
                        points = (d.getLong("points") ?: 0L).toInt(),
                        timestamp = tsMillis,
                        key = d.id
                    )
                } ?: emptyList()
                onChange(list)
            }
        return { reg.remove() }
    }

    /**
     * Listen to server-generated tasks under users/{uid}/rewardTasks for today and current week.
     * Returns a cancel() lambda you must call on dispose.
     */
    fun listenTasks(
        onChange: (List<GeneratedTask>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): () -> Unit {
        val uid = auth.currentUser?.uid ?: return { }
        val dayRef = db.collection("users").document(uid)
            .collection("rewardTasks").document(todayKey())
        val weekRef = db.collection("users").document(uid)
            .collection("rewardTasks").document(weekKey())

        var dayTasks: List<GeneratedTask> = emptyList()
        var weekTasks: List<GeneratedTask> = emptyList()

        fun publish() = onChange(dayTasks + weekTasks)

        val reg1: ListenerRegistration = dayRef.addSnapshotListener { snap, e ->
            if (e != null) { onError(e); return@addSnapshotListener }
            // IMPORTANT: non-KTX -> use getData() (not .data)
            dayTasks = parseTasks(snap?.getData(), defaultCadence = "daily")
            publish()
        }
        val reg2: ListenerRegistration = weekRef.addSnapshotListener { snap, e ->
            if (e != null) { onError(e); return@addSnapshotListener }
            weekTasks = parseTasks(snap?.getData(), defaultCadence = "weekly")
            publish()
        }

        return { reg1.remove(); reg2.remove() }
    }

    private fun parseTasks(
        data: Map<String, Any?>?,
        defaultCadence: String
    ): List<GeneratedTask> {
        val raw = data?.get("tasks") as? List<*> ?: emptyList<Any?>()
        return raw.mapNotNull { any ->
            val m = any as? Map<*, *> ?: return@mapNotNull null
            GeneratedTask(
                templateId = (m["templateId"] as? String).orEmpty(),
                title = (m["title"] as? String).orEmpty(),
                description = (m["description"] as? String).orEmpty(),
                points = (m["points"] as? Number)?.toInt() ?: 0,
                cadence = (m["cadence"] as? String) ?: defaultCadence,
                periodKey = (m["periodKey"] as? String) ?: "",
                claimed = (m["claimed"] as? Boolean) ?: false
            )
        }
    }

    /* ------------------------------ actions -------------------------------- */

    /** Write a claim doc; Cloud Function will mark the corresponding task as claimed=true. */
    fun claimTask(
        t: GeneratedTask,
        onResult: (ok: Boolean, message: String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) { onResult(false, "Not signed in."); return }

        val docId = "${t.templateId}_${t.periodKey}"
        val ref = db.collection("users").document(uid)
            .collection("rewards").document(docId)

        ref.get()
            .addOnSuccessListener { snap ->
                if (snap.exists()) {
                    onResult(false, "Already claimed.")
                } else {
                    ref.set(
                        mapOf(
                            "taskId" to t.templateId,
                            "taskTitle" to t.title,
                            "points" to t.points,
                            "timestamp" to FieldValue.serverTimestamp(),
                            "periodKey" to t.periodKey
                        )
                    ).addOnSuccessListener { onResult(true, "You earned +${t.points} EcoPoints!") }
                        .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
                }
            }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    /** Optional: manually generate today's + this week's tasks for the signed-in user. */
    fun seedNow(onResult: (ok: Boolean, message: String?) -> Unit) {
        // Non-KTX callable
        functions.getHttpsCallable("seedMyRewards")
            .call()
            .addOnSuccessListener { onResult(true, "Seeded tasks for today & this week.") }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }
}
