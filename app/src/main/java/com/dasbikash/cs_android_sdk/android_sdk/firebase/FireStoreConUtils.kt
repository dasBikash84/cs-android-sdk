package com.dasbikash.cs_android_sdk.android_sdk.firebase

import android.app.Activity
import com.dasbikash.cs_android_sdk.android_sdk.exception.FbSignInException
import com.dasbikash.cs_android_sdk.android_sdk.model.FbAccessTokenReqResponse
import com.dasbikash.cs_android_sdk.android_sdk.utils.LoggerUtils
import com.dasbikash.cs_android_sdk.android_sdk.utils.debugLog
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal object FireStoreConUtils {

    private lateinit var firebaseFirestore: FirebaseFirestore

    private fun getDbConnection(): FirebaseFirestore {
        if (!FireStoreConUtils::firebaseFirestore.isInitialized) {
            firebaseFirestore = FirebaseFirestore.getInstance()
            val settings =
                FirebaseFirestoreSettings
                    .Builder()
                    .setPersistenceEnabled(false) //cache diabled
                    .build()
            firebaseFirestore.firestoreSettings = settings
        }
        return firebaseFirestore
    }

    fun getFsDocument(path: String) = getDbConnection().document(path)

    suspend fun loginForManualChat(activity: Activity,
                                   fbAccessTokenReqResponse: FbAccessTokenReqResponse
    )
            : AuthResult? {

        //Fresh login for general session
        logOutIfNotAutoChat()

        //Below block will be effective only for auto-chat
        FirebaseAuth
            .getInstance().currentUser?.let {
            return null
        }
        return loginWithAccessToken(activity, fbAccessTokenReqResponse.token!!)
    }

    suspend fun loginForAutoChat(activity: Activity, loginToken: String)
            : AuthResult? {
        logOut()
        return loginWithAccessToken(activity, loginToken)
    }

    private suspend fun loginWithAccessToken(activity: Activity, loginToken: String)
            : AuthResult? {

        return suspendCoroutine {
            val continuation = it
            FirebaseAuth
                .getInstance()
                .signInWithCustomToken(loginToken)
                .addOnCompleteListener(activity) {
                    if (it.isSuccessful) {
                        LoggerUtils.debugLog("Fb log-in Task isSuccessful", this::class.java)
                        continuation.resume(it.result)
                    } else {
                        LoggerUtils.debugLog("Fb log-in Task failed", this::class.java)
                        continuation.resumeWithException(FbSignInException(it.exception))
                    }
                }
        }
    }

    private fun logOut() {
        FirebaseAuth.getInstance().signOut()
    }

    private suspend fun logOutIfNotAutoChat() {
        return suspendCoroutine {
            val continuation = it
            FirebaseAuth
                .getInstance()
                .currentUser.apply {
                if (this!=null){
                    this.getIdToken(true).addOnCompleteListener {
                        if (it.isComplete &&
                            it.result?.claims?.get(AUTO_CHAT_CLAIM_KEY) == null
                        ) {
                            debugLog("Not auto chat so logging out.")
                            logOut()
                        }
                        continuation.resume(Unit)
                    }
                }else{
                    continuation.resume(Unit)
                }
            }
        }
    }

    private const val AUTO_CHAT_CLAIM_KEY = "autoChat"
}