package by.green.simplemail

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Handler
import by.green.simplemail.db.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class EmailsDataRepository(val appContext: Context) : EmailsRepository {

    private var mDB: EmailsDB? = null
    private var mEmailsIOHelper: EmailsIOHelper? = null
    private var mFolderObservers: HashMap<Long, Observer<List<EmailFolder>>> = HashMap()
    private var mEmailsObservers: HashMap<Long, Observer<List<Email>>> = HashMap()
    private var mCurrentEmailAccount: EmailAccount? = null

    private fun getDB(): EmailsDB? {
        if (mDB == null) {
            mDB = EmailsDB.getDBInstance(appContext)
        }
        return mDB
    }

    private fun getIOHelper(): EmailsIOHelper? {
        if (mEmailsIOHelper == null) {
            mEmailsIOHelper = EmailsIOHelper()
        }
        return mEmailsIOHelper
    }

    override fun updateAccount(account: EmailAccount) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.async {
            getDB()?.emailAccounts()?.updateAccountSettings(
                account.email ?: "",
                incomingServer = account.incomingServer ?: "",
                incomingPort = account.incomingPort ?: 0,
                outServer = account.outServer ?: "",
                outPort = account.outPort ?: 0,
                pwd = account.pwd
            )
        }
    }

    override fun accountExists(account: EmailAccount): Boolean {
        val eAccount = getDB()?.emailAccounts()?.getAccountData(account.email)
        return ((eAccount != null) || (eAccount?.id ?: 0.toInt() != 0))
    }

    override fun addEmailAccount(account: EmailAccount) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.async {
            account.isActive = 1
            getDB()?.emailAccounts()?.addAccount(account)
        }
    }

    override fun addEmailAccountObserver(
        lifecycleOwner: LifecycleOwner,
        observer: Observer<List<EmailAccount>>
    ) {
        getDB()?.emailAccounts()?.getAccounts()?.observe(lifecycleOwner, observer)
    }

    override fun addEmailsObserver(
        lifecycleOwner: LifecycleOwner,
        observer: Observer<List<Email>>,
        folder: EmailFolder
    ) {
        if (mEmailsObservers.containsKey(folder.id)) return
        for (key in mEmailsObservers.keys) {
            val observer = mEmailsObservers[key]
            if (observer != null) {
                getDB()?.email()?.getEmails(key)?.removeObserver(observer)
            }
        }
        mEmailsObservers.clear()
        mEmailsObservers.put(folder.id ?: 0, observer)
        getDB()?.email()?.getEmails(folder.id ?: 0)?.observe(lifecycleOwner, observer)
    }

    override fun addFoldersObserver(
        lifecycleOwner: LifecycleOwner,
        observer: Observer<List<EmailFolder>>,
        emailAccount: EmailAccount
    ) {
        if (mFolderObservers[emailAccount.id ?: 0] != null) return

        for (key in mFolderObservers.keys) {
            val observer = mFolderObservers.get(key)
            if (observer != null) {
                getDB()?.emailFolders()?.getFolders(key)?.removeObserver(observer)
            }
        }
        mFolderObservers.clear()
        mFolderObservers.put(emailAccount.id ?: 0, observer)
        emailAccount.id?.let {
            getDB()?.emailFolders()?.getFolders(it)?.observe(lifecycleOwner, observer)
        }
    }

    override fun removeObservers(owner: LifecycleOwner) {
        getDB()?.emailAccounts()?.getAccounts()?.removeObservers(owner)
        mFolderObservers.clear()
        mEmailsObservers.clear()
    }

    override fun requestFolders(emailAccount: EmailAccount) {
        val onError = fun(errStr: String) {

        }

        val scope = CoroutineScope(Dispatchers.IO)
        scope.async {
            val folders = getIOHelper()?.getFolders(emailAccount, onError)
            getDB()?.emailFolders()?.updateData(emailAccount.id ?: 0, folders)
        }
    }

    override fun requestEmails(
        account: EmailAccount,
        folder: EmailFolder,
        msgStartFrom: Int,
        onError: (String) -> Unit
    ) {
        val scope = CoroutineScope(Dispatchers.IO)
        val h = Handler()
        scope.async {
            try {


                val emails = getIOHelper()?.getEmails(account, onError, folder, msgStartFrom)
                getDB()?.runInTransaction {
                    if (msgStartFrom == 0) {
                        getDB()?.email()?.deleteEmails(folder.id ?: 0)
                    }
                    getDB()?.email()?.insertEmails(emails ?: return@runInTransaction)
                }
                h.post(Runnable { onError("") })
            } catch (e: Exception) {
                h.post(Runnable { onError(e.message ?: e.toString()) })
            }
        }
    }

    override fun fillEmailDetails(
        email: Email,
        account: EmailAccount,
        onSuccess: (List<EmailContentPart>) -> Unit
    ) {
        val onError = fun(errStr: String) {

        }
        val h = Handler()
        val scope = CoroutineScope(Dispatchers.IO)
        scope.async {
            val list = getIOHelper()?.getEmailDetails(email, account, onError)
            h.post(Runnable { onSuccess(list ?: return@Runnable) })
        }
    }

    override fun setEmailRead(emaill: Email, account: EmailAccount, isRead: Boolean) {
        val onError = fun(errStr: String) {

        }
        val scope = CoroutineScope(Dispatchers.IO)
        scope.async {
            getIOHelper()?.setEmailRead(account, onError, emaill, isRead)
            getDB()?.email()?.setEmailRead(unRead = !isRead, emailId = emaill.id ?: 0)
        }
    }


    override fun deleteEmail(email: Email, account: EmailAccount) {
        val onError = fun(errStr: String) {

        }
        val scope = CoroutineScope(Dispatchers.IO)

        scope.async {
            getIOHelper()?.deleteEmail(account, onError, email)
            getDB()?.email()?.deleteEmail(email.id ?: 0)
        }
    }

    override fun setCurrentEmailAccount(account: EmailAccount) {
        mCurrentEmailAccount = account
        val scope = CoroutineScope(Dispatchers.IO)
        scope.async {
            getDB()?.emailAccounts()?.setActiveAccount(account.id ?: -1)
        }
    }

    override fun getCurrentEmailAccount(): EmailAccount? {
        return mCurrentEmailAccount
    }

    override fun sendEmail(
        subject: String,
        dest: String,
        content: String,
        onSendResult: (String) -> Unit
    ) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.async {
            getIOHelper()?.sendEmail(
                getCurrentEmailAccount() ?: return@async,
                subject,
                dest,
                content,
                onSendResult
            )
        }
    }

}