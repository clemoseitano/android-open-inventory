package com.coptimize.openinventory.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.coptimize.openinventory.MainActivity
import kotlin.system.exitProcess

object AppRestartUtil {
    fun restartApp(context: Context) {
        // Create an intent to launch the app's main activity.
        val intent = Intent(context, MainActivity::class.java).apply {
            // Flags to clear the task and start a new one.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Start the new instance of the app.
        context.startActivity(intent)

        // Kill the current process completely.
        // This is the crucial step that destroys the old Hilt dependency graph.
        if (context is Activity) {
            context.finish()
        }
        exitProcess(0)
    }
}