package com.nikkap.calendar.data.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SavePhotoWorker(
    appContext: Context, params: WorkerParameters,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val url = inputData.getString("PHOTO_URL") ?: return@withContext Result.failure()
            val loader = ImageLoader(applicationContext)
            val request = ImageRequest.Builder(applicationContext)
                .data(url)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)

            if (result is SuccessResult) {

                val bitmap = (result.drawable as BitmapDrawable).bitmap

                val fileName = "user_avatar.jpg"
                val file = File(applicationContext.filesDir, fileName)

                FileOutputStream(file).use { outStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
                }
                userPreferencesRepository.saveUserPhoto(file.absolutePath)
                Result.success()
            } else Result.failure()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}