package by.green.simplemail.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import android.util.Log

@Database(entities = arrayOf(EmailAccount::class, EmailFolder::class, Email::class), version = 8)

abstract class EmailsDB : RoomDatabase() {
    abstract fun emailAccounts(): EmailAccountsDao
    abstract fun emailFolders(): EmailFoldersDao
    abstract fun email(): EmailDao

    companion object {
        private var mDB: EmailsDB? = null

        fun getDBInstance(context: Context): EmailsDB? {
            Log.d("DB", "getDBInstance")

            if (mDB == null) {
                synchronized(EmailsDB::class) {
                    mDB = Room.databaseBuilder(
                        context.applicationContext,
                        EmailsDB::class.java,
                        "emails.db"
                    ).fallbackToDestructiveMigration()
                     .build()
                }
            }
            return mDB
        }

        fun destroyInstance() {
            mDB = null
        }

    }
}