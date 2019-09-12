package by.green.simplemail

import android.app.Application

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
    }

    override fun onCreate() {
        super.onCreate()
        appInstance = this
    }

}