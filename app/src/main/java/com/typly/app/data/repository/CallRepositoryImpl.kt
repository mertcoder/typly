package com.typly.app.data.repository

import android.util.Log
import com.typly.app.data.remote.dto.Call
import com.typly.app.data.remote.dto.CallStatus
import com.typly.app.domain.repository.AuthRepository
import com.typly.app.domain.repository.CallRepository
import com.typly.app.domain.repository.CallResult
import com.typly.app.domain.repository.CallType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages all call-related data operations with Firebase Firestore.
 *
 * This repository is the single source of truth for creating, updating, and listening to
 * call states within the application. It interacts directly with the "calls" collection in Firestore.
 *
 * @property authRepository Provides access to the current authenticated user's data.
 * @property firestore The instance of Firebase Firestore used for database operations.
 */
@Singleton
class CallRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
): CallRepository {
    /**
     * Initiates a new call by creating a call document in Firestore.
     *
     * This function generates a unique call ID and creates a new document in the "calls" collection
     * with the status set to `PENDING`.
     *
     * @param targetUserId The ID of the user who is being called.
     * @param callType The type of the call (e.g., AUDIO, VIDEO).
     * @return A [Flow] that emits the [CallResult]:
     * - [CallResult.Loading] immediately.
     * - [CallResult.Success] with the generated `callId` upon successful creation.
     * - [CallResult.Error] if the operation fails.
     */

    override suspend fun initiateCall(
        targetUserId: String,
        callType: CallType
    ): Flow<CallResult<String>> = flow{
        emit(CallResult.Loading)
        try{
            val callId = generateCallId()
            val currentUserId = authRepository.getCurrentUser()?.uid ?: ""
            val call = Call(
                callId = callId,
                callerId = currentUserId,
                recieverId = targetUserId,
                callType = callType,
                status = CallStatus.PENDING,
                timeStamp = System.currentTimeMillis())

            firestore.collection("calls").document(callId)
                .set(call)
                .await()
            emit(CallResult.Success(callId))
        }catch (e: Exception) {
            emit(CallResult.Error("Failed to initiate call: ${e.message}"))
        }
    }

    /**
     * Generates a unique, time-based ID for a call session.
     * @return A unique [String] identifier for the call.
     */
    private fun generateCallId(): String = "agoracall${System.currentTimeMillis().toString().takeLast(8)}${(100..999).random()}"


    /**
     * Updates the status of a call to `ACTIVE` in Firestore.
     *
     * @param callId The ID of the call to be answered.
     * @return A [Flow] emitting [CallResult.Success] on completion or [CallResult.Error] on failure.
     */
    override suspend fun answerCall(callId: String): Flow<CallResult<Unit>> = flow{
        emit(CallResult.Loading)
        try{
            firestore.collection("calls").document(callId).update(
                "status", CallStatus.ACTIVE,
                "timeStamp", System.currentTimeMillis()
            ).await()
            emit(CallResult.Success(Unit))


        }catch (e : Exception) {
            emit(CallResult.Error("Failed to answer call: ${e.message}"))
        }
    }


    /**
     * Updates the status of a call to `REJECTED` in Firestore.
     *
     * @param callId The ID of the call to be rejected.
     * @return A [Flow] emitting [CallResult.Success] on completion or [CallResult.Error] on failure.
     */
    override suspend fun rejectCall(callId: String): Flow<CallResult<Unit>> = flow{
        emit(CallResult.Loading)
        try{
            firestore.collection("calls").document(callId).update(
                "status", CallStatus.REJECTED,
                "timeStamp", System.currentTimeMillis()
            ).await()
            emit(CallResult.Success(Unit))
        }catch (e: Exception) {
            emit(CallResult.Error("Failed to reject call: ${e.message}"))
        }
    }

    /**
     * Updates the status of a call to `ENDED` in Firestore.
     *
     * @param callId The ID of the call to be ended.
     * @return A [Flow] emitting [CallResult.Success] on completion or [CallResult.Error] on failure.
     */
    override suspend fun endCall(callId: String): Flow<CallResult<Unit>> = flow{
        emit(CallResult.Loading)
        try{
            firestore.collection("calls").document(callId).update(
                "status", CallStatus.ENDED,
                "timeStamp", System.currentTimeMillis()
            ).await()
            emit(CallResult.Success(Unit))
        }catch (e: Exception) {
            emit(CallResult.Error("Failed to end call: ${e.message}"))
        }
    }


    /**
     * Listens for incoming calls for a specific user in real-time.
     *
     * This function sets up a Firestore snapshot listener that queries for calls where the
     * user is the receiver and the status is `PENDING`.
     *
     * @param userId The ID of the user to listen for incoming calls.
     * @return A [Flow] that emits a [CallResult] containing the incoming [Call] object, or `null` if there's no incoming call.
     */
    override suspend fun listenForIncomingCalls(userId: String): Flow<CallResult<Call?>> = callbackFlow {
        val listener = firestore.collection("calls")
            .whereEqualTo("recieverId", userId)
            .whereEqualTo("status", CallStatus.PENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(CallResult.Error("Error listening for calls: ${e.message}"))
                    return@addSnapshotListener
                }

                val incomingCall = snapshot?.documents?.firstOrNull()?.toObject(Call::class.java)
                trySend(CallResult.Success(incomingCall))
            }

        awaitClose { listener.remove() }
    }



    /**
     * Listens for an incoming call for a specific user and emits the call object.
     *
     * It sets up a real-time listener on Firestore for pending calls directed at the given user.
     *
     * @param userId The ID of the user.
     * @return A [Flow] that emits the incoming [Call] object if one exists, otherwise `null`.
     * On failure, it emits `null`.
     */
    override suspend fun getIncomingCall(userId: String): Flow<Call?> = callbackFlow {
        val listener = firestore.collection("calls")
            .whereEqualTo("recieverId", userId)
            .whereEqualTo("status", CallStatus.PENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("CallRepository", "Error getting incoming call: ${e.message}", e)
                    trySend(null)
                    return@addSnapshotListener
                }

                val incomingCall = snapshot?.documents?.firstOrNull()?.toObject(Call::class.java)
                Log.d("CallRepository", "Incoming call detected: $incomingCall")
                trySend(incomingCall)
            }

        awaitClose { listener.remove() }
    }


    /**
     * Listens to real-time status changes of a specific call document.
     *
     * @param callId The ID of the call to monitor.
     * @return A [Flow] that emits the updated [Call] object whenever its data changes in Firestore.
     * Emits `null` on listener error.
     */
    override suspend fun listenToCallStatus(callId: String): Flow<Call?> = callbackFlow {
        val listener = firestore.collection("calls")
            .document(callId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("CallRepository", "Error listening to call status: ${e.message}", e)
                    trySend(null)
                    return@addSnapshotListener
                }

                val call = snapshot?.toObject(Call::class.java)
                Log.d("CallRepository", "Call status update: $call")
                trySend(call)
            }

        awaitClose { listener.remove() }
    }
}
