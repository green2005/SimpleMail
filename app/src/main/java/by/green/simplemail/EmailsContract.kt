package by.green.simplemail

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import android.content.Context
import by.green.simplemail.db.Email
import by.green.simplemail.db.EmailAccount
import by.green.simplemail.db.EmailContentPart
import by.green.simplemail.db.EmailFolder

interface EmailsPresenter {
    fun onViewCreated(view: EmailsView)
    fun onViewDestroyed()

    fun onEmailDetailsViewCreated(view: EmailDetailsView, email: Email)
    fun onEmailDetailsViewDestroyed()

    fun onEmailAccountAdded(account: EmailAccount)
    fun onEmailAccountEdit(account: EmailAccount)

    fun onRequestFolders()
    fun onRequestEmails(folder: EmailFolder, msgStartFrom: Int, onError: (String) -> Unit)
    fun onViewActiveAccountChanged(account: EmailAccount)
    fun getCurrentAccount(): EmailAccount?
    fun deleteEmail(email: Email)
    fun setEmailRead(email: Email, isRead: Boolean)
    fun showEmailDetails(email: Email, context: Context)

    fun showEmailAttachment(context: Context, email: Email, attachmentId:Int, onResult:(String) ->Unit)

    fun sendEmail(
        subject: String,
        dest: String,
        content: String,
        onSendResult: (String) -> Unit
    )
}

interface EmailsView {
    fun getAccountsObserver(): Observer<List<EmailAccount>>
    fun getFoldersObserver(emailAccount: EmailAccount): Observer<List<EmailFolder>>
    fun getEmailsObserver(folder: EmailFolder): Observer<List<Email>>?
    fun getLifeCycleOwner(): LifecycleOwner
}

interface EmailDetailsView {
    fun showEmailDetails(content: List<EmailContentPart>)
}

interface EmailsRepository {
    fun setCurrentEmailAccount(account: EmailAccount?)
    fun getCurrentEmailAccount(): EmailAccount?

    fun addEmailAccount(account: EmailAccount)
    fun updateAccount(account: EmailAccount)

    fun addEmailAccountObserver(
        lifecycleOwner: LifecycleOwner,
        observer: Observer<List<EmailAccount>>
    )

    fun requestFolders(emailAccount: EmailAccount)

    fun requestEmails(
        emailAccount: EmailAccount,
        folder: EmailFolder,
        msgStartFrom: Int,
        onError: (String) -> Unit
    )

    fun addEmailsObserver(
        lifecycleOwner: LifecycleOwner,
        observer: Observer<List<Email>>,
        folder: EmailFolder
    )

    fun addFoldersObserver(
        lifecycleOwner: LifecycleOwner,
        observer: Observer<List<EmailFolder>>,
        emailAccount: EmailAccount
    )

    fun removeObservers(owner: LifecycleOwner)
    fun deleteEmail(email: Email, account: EmailAccount)
    fun setEmailRead(emaill: Email, account: EmailAccount, isRead: Boolean = false)

    fun fillEmailDetails(
        email: Email,
        account: EmailAccount,
        onSuccess: (List<EmailContentPart>) -> Unit
    )

    fun accountExists(account: EmailAccount): Boolean

    fun sendEmail(
        subject: String,
        dest: String,
        content: String,
        onSendResult: (String) -> Unit
    )

    fun showEmailAttachment(context: Context, email:Email, account: EmailAccount, attachmentId: Int, onResult:(String) ->Unit )

}