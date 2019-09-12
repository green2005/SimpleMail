package by.green.simplemail

import by.green.simplemail.db.*
import com.sun.mail.imap.IMAPMessage
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import kotlin.collections.ArrayList


class EmailsIOHelper() {
    private val IMAPS = "imaps"
    private val MSGS_COUNT = 15
    private val VIEW_PORT_TAG = "<meta name=\"viewport\" content='width=device-width, initial-scale=1.0,text/html,charset=utf-8'>"

    private fun getSMTPSession(emailAccount: EmailAccount, isIncoming: Boolean = true): Session? {
        val props = System.getProperties()
        if (isIncoming) {
            props["mail.smtp.host"] = emailAccount.incomingServer
            props["mail.smtp.port"] = emailAccount.incomingPort.toString()
            props["mail.smtp.socketFactory.port"] = emailAccount.incomingPort.toString()
        } else {

            props.put("mail.smtp.starttls.enable", "true")
            props["mail.smtp.auth"] = "true"

            props["mail.smtp.host"] = emailAccount.outServer         //"smtp.yandex.com"
            props["mail.smtp.port"] = emailAccount.outPort.toString() //"587"
            props["mail.smtp.socketFactory.port"] = emailAccount.outPort.toString()  //"587"
            return Session.getInstance(props, object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    val pwd = emailAccount.pwd
                    val userName = emailAccount.email
                    return PasswordAuthentication(userName, pwd)
                }
            })
        }

        props["mail.smtp.auth"] = "true"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"

        return Session.getInstance(
            props,
            AccountSettingsActivity.SMTPAuthenticator(emailAccount.email, emailAccount.pwd)
        )
    }

    private fun getPOP3Session(emailAccount: EmailAccount): Session? {
        val pop3Props = Properties()
        pop3Props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
        pop3Props.setProperty("mail.pop3.socketFactory.fallback", "false")
        pop3Props.setProperty("mail.pop3.port", emailAccount.outPort.toString())
        pop3Props.setProperty("mail.pop3.socketFactory.port", emailAccount.outPort.toString())

        val properties = Properties()
        properties.setProperty("mail.store.protocol", "imaps")
        return Session.getDefaultInstance(properties)
    }

    fun getFolders(account: EmailAccount, onError: (errStr: String) -> Unit): List<EmailFolder> {
        val list = ArrayList<EmailFolder>()
        var store: Store? = null
        try {
            val session = getSMTPSession(emailAccount = account)
            store = session?.getStore(IMAPS)
            store?.connect(account.incomingServer, account.email, account.pwd)
            val folders = store?.defaultFolder?.list("*") ?: return list
            for (folder in folders) {
                val fullName = account.email + folder.urlName.toString()
                val emailFolder = EmailFolder(
                    id = fullName.hashCode().toLong(),
                    emailAccountId = account.id ?: 0,
                    folder_id = folder.urlName.file,
                    email = account.email,
                    name = folder.name,
                    //type = folder.
                    show = true
                )
                list.add(emailFolder)
            }
            return list
        } finally {
            store?.close()
        }
    }

    fun setEmailRead(
        account: EmailAccount,
        onError: (errStr: String) -> Unit,
        email: Email,
        isRead: Boolean = true
    ) {
        var session: Session? = null
        var store: Store? = null
        try {
            session = getSMTPSession(emailAccount = account)
            store = session?.getStore(IMAPS)
            store?.connect(account.incomingServer, account.email, account.pwd)
            val f = store?.getFolder(email.folderUrl) ?: return
            f.open(Folder.READ_WRITE)
            val uf = f as UIDFolder
            val msg = uf.getMessageByUID(email.email_id.toString().toLong())
            msg.setFlag(Flags.Flag.SEEN, isRead)
            f.close(true)
        } catch (e: java.lang.Exception) {
            onError(e.message ?: "Error setting read flag:$e")
        }
        store?.close()
    }

    fun deleteEmail(
        account: EmailAccount,
        onError: (errStr: String) -> Unit,
        email: Email
    ) {
        var session: Session? = null
        var store: Store? = null
        try {
            session = getSMTPSession(emailAccount = account)
            store = session?.getStore(IMAPS)
            store?.connect(account.incomingServer, account.email, account.pwd)
            val f = store?.getFolder(email.folderUrl) ?: return
            f.open(Folder.READ_WRITE)
            val uf = f as UIDFolder
            val msg = uf.getMessageByUID(email.email_id.toString().toLong())
            msg.setFlag(Flags.Flag.DELETED, true)
            f.close(true)
        } catch (e: java.lang.Exception) {
            onError(e.message ?: "Error deleting email:$e")
        }
        store?.close()
    }


    fun getEmails(
        account: EmailAccount,
        onError: (errStr: String) -> Unit,
        folder: EmailFolder,
        msgStartFrom: Int
    ): List<Email> {
        val list = ArrayList<Email>()
        var store: Store? = null
        var f: Folder? = null
        try {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy")
            val session = getSMTPSession(emailAccount = account)
            store = session?.getStore(IMAPS)
            store?.connect(account.incomingServer, account.email, account.pwd)
            f = store?.getFolder(folder.folder_id)
            if (f == null) {
                return emptyList()
            }
            f.open(Folder.READ_ONLY)
            val uf = f as UIDFolder
            var j = -1
            val i = if (msgStartFrom == 0) {
                f.messageCount
            } else {
                msgStartFrom
            }
//store?.defaultFolder?.list("*").get(5).urlName.file.toString()
            while (j++ < MSGS_COUNT) {
                val msgNom = i - j
                if (msgNom > 0) {
                    val msg = f.getMessage(msgNom)
                    var from = ""
                    var from_email = ""
                    if (msg.from.size > 0) {
                        from = (msg.from[0] as InternetAddress).toUnicodeString()
                        from_email = (msg.from[0] as InternetAddress).address
                        //(msg.from.get(0) as IMAPAddress).address
                    }

                    var subj: String

                    if ((msg as IMAPMessage).encoding?.toLowerCase().equals("7bit")) {
                        subj =
                            String(msg.subject.toByteArray(charset("ISO-8859-1")), charset("UTF-8"))
                    } else {
                        subj = String(msg.subject.toByteArray(charset("UTF-8")), charset("UTF-8"))
                    }

                    val email = Email(
                        folderId = folder.id ?: 0,
                        folderUrl = folder.folder_id,
                        folderName = folder.name,
                        email_id = uf.getUID(msg).toString(), //folder.email,
                        subject = subj,
                        date = dateFormat.format(msg.sentDate),
                        from_title = from,
                        from_email = from_email,
                        msgNum = msg.messageNumber,
                        unread = !msg.isSet(Flags.Flag.SEEN)
                    )
                    list.add(email)
                } else
                    break
            }
        } finally {
            f?.close(false)
            store?.close()
        }
        return list
    }

    fun getEmailDetails(
        email: Email,
        account: EmailAccount,
        onError: (errStr: String) -> Unit
    ): ArrayList<EmailContentPart>? {
        var store: Store? = null
        val list = ArrayList<EmailContentPart>()
        try {
            val session = getSMTPSession(emailAccount = account)
            store = session?.getStore(IMAPS)
            store?.connect(account.incomingServer, account.email, account.pwd)
            val f = store?.getFolder(email.folderUrl) ?: return null
            f.open(Folder.READ_ONLY)
            val uf = f as UIDFolder
            val emailID = email.email_id.toString().toLong()
            val msg = uf.getMessageByUID(emailID)
            var s: String = ""
            if (msg.isMimeType("text/html")) {
                s = msg.getContent().toString()
                list.add(EmailContentPart(s, EmailContentType.HTML))
            } else
                if (msg.isMimeType("text/plain")) {
                    list.add(EmailContentPart(s, EmailContentType.TXT))
                } else if (msg.isMimeType("multipart/*")) {
                    val mimeMultipart = msg.getContent() as MimeMultipart
                    addEmailFromMimeMultipart(mimeMultipart, list)
                    //s = addEmailFromMimeMultipart(mimeMultipart)
                }
            f.close(true)
        } catch (e: Exception) {
            onError(e.message ?: e.toString())
        }
        store?.close()
        return list
    }

    private fun addEmailFromMimeMultipart(
        mimeMultipart: MimeMultipart,
        list: ArrayList<EmailContentPart>
    ): String {
        var result = ""
        val count = mimeMultipart.count
        for (i in count - 1 downTo 0) {
            val bodyPart = mimeMultipart.getBodyPart(i)
            if (bodyPart.isMimeType("text/plain")) {
                list.add(EmailContentPart(bodyPart.content.toString(), EmailContentType.TXT))
            } else if (bodyPart.isMimeType("text/html")) {
                val html =  getViewPortOptimized(bodyPart.content as String)
                list.add(EmailContentPart(html, EmailContentType.HTML))
                break
            } else if (bodyPart.content is MimeMultipart) {
                result += addEmailFromMimeMultipart(bodyPart.content as MimeMultipart, list)
            }
        }
        return result
    }

    private fun getViewPortOptimized(html:String):String{
        if (html.contains("\"viewport\"")){
            return html
        } else
            return html.replace("<head>", "<head>$VIEW_PORT_TAG")
    }

    fun checkAccount(account: EmailAccount, onError: (errStr: String) -> Unit): Boolean {
        var store: Store? = null
        try {
            try {
                val session = getSMTPSession(emailAccount = account)
                store = session?.getStore(IMAPS)
                store?.connect(account.incomingServer, account.email, account.pwd)
                //val folders = store?.defaultFolder?.list("*") ?: return false
            } catch (e: Exception) {
                if (e is javax.mail.AuthenticationFailedException) {
                    onError("Wrong credentials")
                } else {
                    onError(e.toString())
                }
                return false
            }
            return store?.isConnected ?: false
        } finally {

            store?.close()
        }
    }

    fun sendEmail(
        account: EmailAccount,
        subject: String,
        dest: String,
        content: String,
        onSendResult: (String) -> Unit
    ) {
        val session = getSMTPSession(emailAccount = account, isIncoming = false)
        if (session != null) {
            val msg = MimeMessage(session)
            msg.setFrom(InternetAddress(account.email))
            msg.setRecipient(Message.RecipientType.TO, InternetAddress(dest, dest))
            msg.setSubject(subject, "UTF-8")
            val mp = MimeBodyPart()
            //mp.setContent(content, "text/html")
            mp.setText(content, "UTF-8")
            val multipart = MimeMultipart()
            multipart.addBodyPart(mp)
            msg.setContent(multipart)
            try {
                Transport.send(msg)
                onSendResult("")
            } catch (e: java.lang.Exception) {
                onSendResult(e.toString())
            }
        }
    }

}