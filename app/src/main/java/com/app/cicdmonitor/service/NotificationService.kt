package com.app.cicdmonitor.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.cicdmonitor.MainActivity
import com.app.cicdmonitor.data.models.BuildStatus
import com.app.cicdmonitor.data.models.Pipeline
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_PIPELINE_STATUS,
                    "Pipeline Status",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for pipeline status changes"
                },
                NotificationChannel(
                    CHANNEL_SYNC_STATUS,
                    "Sync Status",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Notifications for sync operations"
                }
            )
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
    
    fun showPipelineStatusNotification(pipeline: Pipeline, build: com.app.cicdmonitor.data.models.Build) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("pipeline_id", pipeline.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            pipeline.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val (title, text, icon) = when (build.status) {
            BuildStatus.SUCCESS -> Triple(
                "✅ Build Successful",
                "${pipeline.name} completed successfully",
                android.R.drawable.ic_dialog_info
            )
            BuildStatus.FAILURE -> Triple(
                "❌ Build Failed",
                "${pipeline.name} failed",
                android.R.drawable.ic_dialog_alert
            )
            BuildStatus.RUNNING -> Triple(
                "🔄 Build Started",
                "${pipeline.name} is now running",
                android.R.drawable.ic_popup_sync
            )
            else -> return // Don't notify for other statuses
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_PIPELINE_STATUS)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$text\n\nCommit: ${build.commitMessage}\nAuthor: ${build.commitAuthor}"
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            notificationManager.notify(pipeline.id.hashCode(), notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
        }
    }
    
    fun showSyncNotification(isSuccess: Boolean, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC_STATUS)
            .setSmallIcon(if (isSuccess) android.R.drawable.ic_popup_sync else android.R.drawable.ic_dialog_alert)
            .setContentTitle(if (isSuccess) "Sync Complete" else "Sync Failed")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        try {
            notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
        }
    }
    
    fun cancelPipelineNotification(pipelineId: String) {
        notificationManager.cancel(pipelineId.hashCode())
    }
    
    fun cancelSyncNotification() {
        notificationManager.cancel(SYNC_NOTIFICATION_ID)
    }
    
    companion object {
        private const val CHANNEL_PIPELINE_STATUS = "pipeline_status"
        private const val CHANNEL_SYNC_STATUS = "sync_status"
        private const val SYNC_NOTIFICATION_ID = 1001
    }
}
