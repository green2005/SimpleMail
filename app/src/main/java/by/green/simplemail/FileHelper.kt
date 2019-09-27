package by.green.simplemail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File


fun File.setDataTypeByUri(intent: Intent, uri: Uri) {
    if (uri.toString().contains(".doc") || uri.toString().contains(".docx")) {
        // Word document
        intent.setDataAndType(uri, "application/msword");
    } else if (uri.toString().contains(".pdf")) {
        // PDF file
        intent.setDataAndType(uri, "application/pdf");
    } else if (uri.toString().contains(".ppt") || uri.toString().contains(".pptx")) {
        // Powerpoint file
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
    } else if (uri.toString().contains(".xls") || uri.toString().contains(".xlsx")) {
        // Excel file
        intent.setDataAndType(uri, "application/vnd.ms-excel");
    } else if (uri.toString().contains(".zip") || uri.toString().contains(".rar")) {
        // WAV audio file
        intent.setDataAndType(uri, "application/x-wav");
    } else if (uri.toString().contains(".rtf")) {
        // RTF file
        intent.setDataAndType(uri, "application/rtf");
    } else if (uri.toString().contains(".wav") || uri.toString().contains(".mp3")) {
        // WAV audio file
        intent.setDataAndType(uri, "audio/x-wav");
    } else if (uri.toString().contains(".gif")) {
        // GIF file
        intent.setDataAndType(uri, "image/gif");
    } else if (uri.toString().contains(".jpg") || uri.toString().contains(".jpeg") || uri.toString().contains(
            ".png"
        )
    ) {
        // JPG file
        intent.setDataAndType(uri, "image/jpeg");
    } else if (uri.toString().contains(".txt")) {
        // Text file
        intent.setDataAndType(uri, "text/plain");
    } else if (uri.toString().contains(".3gp") || uri.toString().contains(".mpg") || uri.toString().contains(
            ".mpeg"
        ) || uri.toString().contains(".mpe") || uri.toString().contains(".mp4") || uri.toString().contains(
            ".avi"
        )
    ) {
        // Video files
        intent.setDataAndType(uri, "video/*");
    } else {
        // Other files
        intent.setDataAndType(uri, "*/*");
    }
}

fun File.open(context: Context, onError: (String) -> Unit) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            this
        )
        val i2 = Intent(Intent.ACTION_VIEW)
        setDataTypeByUri(i2, uri)
        i2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i2.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(i2)
    } catch (e: Exception) {
        onError(e.toString())
    }

}