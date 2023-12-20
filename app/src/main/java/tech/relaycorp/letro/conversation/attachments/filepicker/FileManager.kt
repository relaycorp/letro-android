package tech.relaycorp.letro.conversation.attachments.filepicker

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import javax.inject.Inject

interface FileManager {
    fun save(file: File.FileWithContent): String
    fun delete(path: String)
}

class FileManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : FileManager {

    override fun save(file: File.FileWithContent): String {
        val fileOutput = getFileOutput(file)
        fileOutput.writeBytes(file.content)
        return fileOutput.absolutePath
    }

    override fun delete(path: String) {
        val file = java.io.File(path)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun getFileOutput(file: File.FileWithContent): java.io.File {
        var fileName = file.name
        var duplicates = 1
        while (java.io.File(context.filesDir, fileName).exists()) {
            fileName = "${duplicates}_${file.name}"
            duplicates++
        }
        return java.io.File(context.filesDir, fileName).apply {
            createNewFile()
        }
    }
}
