package tech.relaycorp.letro.conversation.attachments.filepicker

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import javax.inject.Inject

interface FileSaver {
    fun save(file: File.FileWithContent): String
}

class FileSaverImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : FileSaver {

    override fun save(file: File.FileWithContent): String {
        val fileOutput = getFileOutput(file)
        fileOutput.writeBytes(file.content)
        return fileOutput.absolutePath
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
