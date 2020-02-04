package com.dasbikash.cs_android_sdk.android_sdk.firebase

import com.dasbikash.cs_android_sdk.android_sdk.exception.FbDocumentReadException
import com.dasbikash.cs_android_sdk.android_sdk.exception.FbDocumentWriteException
import com.dasbikash.cs_android_sdk.android_sdk.utils.LoggerUtils
import com.dasbikash.cs_android_sdk.android_sdk.utils.debugLog
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal object FireStoreUtils {

    suspend fun <T> readDocument(path: String?, type: Class<T>): T? {
        return suspendCoroutine {
            LoggerUtils.debugLog(
                "readDocument: $path",
                this::class.java
            )
            val continuation = it
            if (path !=null) {
                FireStoreConUtils.getFsDocument(
                    path
                )
                    .get().addOnCompleteListener {
                        if (it.isSuccessful) {
                            LoggerUtils.debugLog(
                                "Fb document read is Successful.",
                                this::class.java
                            )
                            continuation.resume(it.result?.toObject(type))
                        } else {
                            LoggerUtils.debugLog("Fb document read failed.", this::class.java)
                            continuation.resumeWithException(FbDocumentReadException(it.exception))
                        }
                    }
            }else{
                continuation.resume(null)
            }
        }
    }

    suspend fun <T : Any> writeDocument(path: String?, payload: T?): T? {
        return suspendCoroutine {
            debugLog("Going to write: $payload at $path")
            val continuation = it
            if (path!=null && payload!=null) {
                FireStoreConUtils.getFsDocument(
                    path
                )
                    .set(payload)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) {
                            continuation.resumeWithException(FbDocumentWriteException(it.exception))
                        } else {
                            continuation.resume(payload)
                        }
                    }
            }else{
                it.resume(null)
            }

        }
    }
}