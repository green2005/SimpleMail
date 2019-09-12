package by.green.simplemail

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
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

    override fun getCurrentAccount(): EmailAccount? {
        return App.getEmailsRepository()?.getCurrentEmailAccount()
    }

    override fun onViewActiveAccountChanged(account: EmailAccount) {
        if ((App.getEmailsRepository()?.getCurrentEmailAccount() == null) ||
            (App.getEmailsRepository()?.getCurrentEmailAccount() != account)
        ) {
            account.isActive = 1
            App.getEmailsRepository()?.setCurrentEmailAccount(account)
            onRequestFolders()
        }
    }

    override fun onViewCreated(view: EmailsView) {
        mView = view
        App.getEmailsRepository()?.addEmailAccountObserver(
            mView?.getLifeCycleOwner() ?: return,
            mView?.getAccountsObserver() ?: return
        )
    }

    override fun onViewDestroyed() {
        App.getEmailsRepository()?.removeObservers(mView?.getLifeCycleOwner() ?: return)
        mView = null
    }

    override fun onEmailAccountAdded(account: EmailAccount) {
        App.getEmailsRepository()?.addEmailAccount(account)
        // App.getEmailsRepository()?.setCurrentEmailAccount(account)
    }

    override fun onEmailAccountEdit(account: EmailAccount) {
        App.getEmailsRepository()?.updateAccount(account)
        App.getEmailsRepository()?.setCurrentEmailAccount(account)
    }

    override fun onRequestFolders() {
        App.getEmailsRepository()?.addFoldersObserver(
            mView?.getLifeCycleOwner() ?: return,
            mView?.getFoldersObserver(App.getEmailsRepository()?.getCurrentEmailAccount() ?: return)
                ?: return,
            App.getEmailsRepository()?.getCurrentEmailAccount() ?: return
        )
        App.getEmailsRepository()
            ?.requestFolders(App.getEmailsRepository()?.getCurrentEmailAccount() ?: return)
    }

    override fun onRequestEmails(
        folder: EmailFolder,
        msgsStartFrom: Int,
        onError: (String) -> Unit
    ) {
        val observer = mView?.getEmailsObserver(
            folder
        )
        App.getEmailsRepository()?.addEmailsObserver(
            mView?.getLifeCycleOwner() ?: return,
            observer ?: return, folder
        )

        App.getEmailsRepository()
            ?.requestEmails(
                App.getEmailsRepository()?.getCurrentEmailAccount() ?: return,
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

        App.getEmailsRepository()?.getCurrentEmailAccount()?.let {
            App.getEmailsRepository()?.fillEmailDetails(
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
        App.getEmailsRepository()
            ?.deleteEmail(email, App.getEmailsRepository()?.getCurrentEmailAccount() ?: return)
    }

    override fun setEmailRead(email: Email, isRead: Boolean) {
        App.getEmailsRepository()
            ?.setEmailRead(
                email,
                App.getEmailsRepository()?.getCurrentEmailAccount() ?: return,
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
        App.getEmailsRepository()?.sendEmail(subject, dest, content, onSendResult)
    }

}