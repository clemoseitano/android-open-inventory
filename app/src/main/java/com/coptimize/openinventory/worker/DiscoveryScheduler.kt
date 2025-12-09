package com.coptimize.openinventory.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object DiscoveryScheduler {

    fun scheduleTaskMonitoring(context: Context, taskId: String, productId: String, stockId: String?) {
        val workManager = WorkManager.getInstance(context)

        val inputData = Data.Builder()
            .putString(ProductDiscoveryWorker.KEY_TASK_ID, taskId)
            .putString(ProductDiscoveryWorker.KEY_PRODUCT_ID, productId)
            .putString(ProductDiscoveryWorker.KEY_STOCK_ID, stockId)
            .build()

        // Check 1: 5 Minutes
        val check1 = OneTimeWorkRequest.Builder(ProductDiscoveryWorker::class.java)
            .setInputData(inputData)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        // Check 2: Another 5 Minutes (runs 5 mins after check 1 finishes)
        val check2 = OneTimeWorkRequest.Builder(ProductDiscoveryWorker::class.java)
            .setInputData(inputData)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        // Check 3: 15 Minutes (Final Attempt)
        val finalInputData = Data.Builder()
            .putAll(inputData)
            .putBoolean(ProductDiscoveryWorker.KEY_IS_FINAL_ATTEMPT, true)
            .build()

        val check3 = OneTimeWorkRequest.Builder(ProductDiscoveryWorker::class.java)
            .setInputData(finalInputData)
            .setInitialDelay(3, TimeUnit.MINUTES)
            .build()

        // Chain them: 1 -> 2 -> 3
        // If check 1 finds results and returns SUCCESS, the chain continues?
        // WorkManager chaining: If parent SUCCEEDS, child runs. If parent FAILS, chain stops.
        // In our Worker logic:
        // - If found (SUCCESS): We actually want to STOP the chain.
        //   BUT WorkManager doesn't natively support "Stop chain on Success".
        //
        // Strategy Correction:
        // The Worker logic I wrote above updates the DB.
        // We should modify the worker: If DB says task is already "completed" or "cancelled", return Result.success() immediately.

        workManager.beginUniqueWork(
            "discovery_$taskId",
            ExistingWorkPolicy.REPLACE,
            check1
        )
            .then(check2)
            .then(check3)
            .enqueue()
    }
}