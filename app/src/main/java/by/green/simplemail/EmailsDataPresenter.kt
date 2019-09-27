package by.green.simplemail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.green.simplemail.db.Email
import by.green.simplemail.db.EmailAccount
import by.green.simplemail.db.EmailContentPart
import by.green.simplemail.db.EmailFolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class EmailsDataPresenter : EmailsPresenter, ViewModel() {


    private var mView: EmailsView? = null
    private var mDetailsView: EmailDetailsView? = null

    private fun getEmailsRepository(): EmailsRepository? {
        return App.getEmailsRepository()
    }

    override fun getCurrentAccount(): EmailAccount? {
        return getEmailsRepository()?.getCurrentEmailAccount()
    }

    override fun onViewActiveAccountChanged(account: EmailAccount) {
        if ((getEmailsRepository()?.getCurrentEmailAccount() == null) ||
            (getEmailsRepository()?.getCurrentEmailAccount() != account)
        ) {
            account.isActive = 1
            getEmailsRepository()?.setCurrentEmailAccount(account)
            onRequestFolders()
        }
    }

    override fun onViewCreated(view: EmailsView) {
        mView = view
        getEmailsRepository()?.addEmailAccountObserver(
            mView?.getLifeCycleOwner() ?: return,
            mView?.getAccountsObserver() ?: return
        )
    }

    override fun onViewDestroyed() {
        getEmailsRepository()?.removeObservers(mView?.getLifeCycleOwner() ?: return)
        getEmailsRepository()?.setCurrentEmailAccount(null)
        mView = null
    }

    override fun onEmailAccountAdded(account: EmailAccount) {
        getEmailsRepository()?.addEmailAccount(account)
        // getEmailsRepository()?.setCurrentEmailAccount(account)
    }

    override fun onEmailAccountEdit(account: EmailAccount) {
        getEmailsRepository()?.updateAccount(account)
        getEmailsRepository()?.setCurrentEmailAccount(account)
    }

    override fun onRequestFolders() {
        getEmailsRepository()?.addFoldersObserver(
            mView?.getLifeCycleOwner() ?: return,
            mView?.getFoldersObserver(getEmailsRepository()?.getCurrentEmailAccount() ?: return)
                ?: return,
            getEmailsRepository()?.getCurrentEmailAccount() ?: return
        )


        getEmailsRepository()
            ?.requestFolders(getEmailsRepository()?.getCurrentEmailAccount() ?: return)
    }

    override fun onRequestEmails(
        folder: EmailFolder,
        msgsStartFrom: Int,
        onError: (String) -> Unit
    ) {
        val observer = mView?.getEmailsObserver(
            folder
        ) ?: return

        getEmailsRepository()?.addEmailsObserver(
            mView?.getLifeCycleOwner() ?: return,
            observer ?: return, folder
        )

        getEmailsRepository()
            ?.requestEmails(
                getEmailsRepository()?.getCurrentEmailAccount() ?: return,
                folder,
                msgsStartFrom,
                onError
            )
    }

    override fun onEmailDetailsViewCreated(view: EmailDetailsView, email: Email) {
        mDetailsView = view
        setEmailDetailsToView(email)
        setEmailRead(email, true)
    }

    private fun setEmailDetailsToView(email: Email) {
        val scope = CoroutineScope(Dispatchers.IO)
        val onSuccess = fun(lst: List<EmailContentPart>) {
            mDetailsView?.showEmailDetails(lst)
        }

        getEmailsRepository()?.getCurrentEmailAccount()?.let {
            getEmailsRepository()?.fillEmailDetails(
                email,
                it,
                onSuccess
            )
        }
    }

    override fun onEmailDetailsViewDestroyed() {
        mDetailsView = null
    }

    override fun deleteEmail(email: Email) {
        getEmailsRepository()
            ?.deleteEmail(email, getEmailsRepository()?.getCurrentEmailAccount() ?: return)
    }

    override fun setEmailRead(email: Email, isRead: Boolean) {
        getEmailsRepository()
            ?.setEmailRead(
                email,
                getEmailsRepository()?.getCurrentEmailAccount() ?: return,
                isRead
            )
    }

    override fun showEmailDetails(email: Email, context: Context) {
        val intent = Intent(context, EmailDetailActivity::class.java)
        intent.putExtra(EmailDetailActivity.EMAIL, email)
        context.startActivity(intent)
    }

    override fun sendEmail(
        subject: String,
        dest: String,
        content: String,
        onSendResult: (String) -> Unit
    ) {
        getEmailsRepository()?.sendEmail(subject, dest, content, onSendResult)
    }

    override fun showEmailAttachment(
        context: Context,
        email: Email,
        attachmentId: Int,
        onResult: (String) -> Unit
    ) {
        getEmailsRepository()?.showEmailAttachment(
            context,
            email,
            getEmailsRepository()?.getCurrentEmailAccount() ?: return,
            attachmentId,
            onResult
        )
    }

}