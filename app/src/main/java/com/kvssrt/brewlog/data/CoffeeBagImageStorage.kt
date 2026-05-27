package com.kvssrt.brewlog.data

import android.content.Context
import android.net.Uri
import java.io.File

class CoffeeBagImageStorage(
    private val context: Context,
) {
    fun copyToAppStorage(source: Uri): String {
        val directory = File(context.filesDir, "coffee-bags").apply { mkdirs() }
        val target = File(directory, "coffee-bag-${System.currentTimeMillis()}.jpg")

        context.contentResolver.openInputStream(source).use { input ->
            requireNotNull(input) { "Could not open selected image." }
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return target.absolutePath
    }
}
