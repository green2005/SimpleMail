package by.green.simplemail

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.net.URI

class App : Application() {
    companion object {
        private var appInstance: App? = null
        private var mEmailsRepository: EmailsRepository? = null

        fun getEmailsRepository(): EmailsRepository? {
            if (mEmailsRepository == null) {
                mEmailsRepository = appInstance?.let { EmailsDataRepository(it) }
            }
            return mEmailsRepository
        }

        fun getCacheDir():File?{

            return appInstance?.cacheDir
        }

        fun openTempFile(f:File){
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.fromFile(f))
            val intent2 = Intent.createChooser(intent, "Choose an application")
            appInstance?.startActivity(intent2)
        }

    }

    override fun onCreate() {
        super.onCreate()
        appInstance = this
    }

}