package com.coptimize.openinventory.data.repository

import android.net.Uri
import com.coptimize.openinventory.data.api.PollStatusResponse

interface ProductAnalysisRepository {
    suspend fun pollInferenceResults(taskId: String): PollStatusResponse
    suspend fun performRemoteOcrAndInference(uris: List<Uri>): String
    suspend fun performLocalOcrAndRemoteInference(uris: List<Uri>): String
}