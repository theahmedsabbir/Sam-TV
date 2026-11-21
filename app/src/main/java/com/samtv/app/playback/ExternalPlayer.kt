package com.samtv.app.playback

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.samtv.app.data.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ExternalPlayer {
    suspend fun playFolder(context: Context, folderUrl: String, title: String = "") {
        val videoUrl = withContext(Dispatchers.IO) {
            MediaRepository.videoUrl(folderUrl)
        }

        if (videoUrl.isNullOrBlank()) {
            Toast.makeText(context, "No video file found", Toast.LENGTH_SHORT).show()
            return
        }

        val mime = mimeFor(videoUrl)
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(videoUrl), mime)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (title.isNotBlank()) {
                putExtra("title", title)
            }
        }

        try {
            context.startActivity(
                Intent.createChooser(viewIntent, "Play with").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "Install a video player (VLC / MX Player)",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun mimeFor(url: String): String {
        val lower = url.lowercase()
        return when {
            lower.endsWith(".mkv") -> "video/x-matroska"
            lower.endsWith(".mp4") -> "video/mp4"
            lower.endsWith(".avi") -> "video/x-msvideo"
            lower.endsWith(".webm") -> "video/webm"
            lower.endsWith(".mov") -> "video/quicktime"
            else -> "video/*"
        }
    }
}
