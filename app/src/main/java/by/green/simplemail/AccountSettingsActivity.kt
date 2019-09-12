package by.green.simplemail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import by.green.simplemail.db.EmailAccount
import by.green.simplemail.db.EmailDomains
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import java.util.regex.Pattern
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication


class AccountSettingsActivity : AppCompatActivity() {

    private class EmailEditWatcher(val textChangedListener: (String) -> Unit) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            textChangedListener(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    companion object {
        const val C_ADD_ACCOUNT = 101
        const val C_EDIT_ACCOUNT = 102
        const val C_ACCOUNT = "account"
        const val C_REQUEST_CODE = "request_code"
    }

    private lateinit var mEmailEdit: EditText
    private lateinit var mPwdEdit: EditText
    private lateinit var mInServerEdit: EditText
    private lateinit var mInPortEdit: EditText
    private lateinit var mOutServerEdit: EditText
    private lateinit var mOutPortEdit: EditText

    private val mPattern = Pattern.compile("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val drawable = ContextCompat.getDrawable(this, R.drawable.cancel_circle)
        toolbar.navigationIcon = drawable
        setSupportActionBar(toolbar)

        mEmailEdit = findViewById(R.id.emailEdit)
        mPwdEdit = findViewById(R.id.pwdEdit)
        mInServerEdit = findViewById(R.id.inServerNameEdit)
        mInPortEdit = findViewById(R.id.inServerPortEdit)
        mOutServerEdit = findViewById(R.id.outServerNameEdit)
        mOutPortEdit = findViewById(R.id.outServerPortEdit)
        val requestCode = intent.getIntExtra(C_REQUEST_CODE, C_ADD_ACCOUNT)

        var d: EmailDomains? = null
        val textChangedListener = fun(email: String) {
            val i = email.indexOf("@")
            if (i > 0) {
                val domain = email.substring(i + 1)
                for (s in EmailDomains.values()) {
                    if (s.domain.toUpperCase().contentEquals(domain.toUpperCase())) {
                        d = s
                        break
                    }
                }
            }

            if (d != null) {
                mInServerEdit.setText(d?.incomingServer)
                mInPortEdit.setText(d?.incomingPort)
                mOutServerEdit.setText(d?.outServer)
                mOutPortEdit.setText(d?.outPort)
            }
        }
        if (requestCode != C_EDIT_ACCOUNT) {
            mEmailEdit.addTextChangedListener(EmailEditWatcher(textChangedListener))
        }

        if (requestCode == C_EDIT_ACCOUNT) {
            val account = intent.getParcelableExtra<EmailAccount>(C_ACCOUNT)
            mEmailEdit.setText(account.email)
            mPwdEdit.setText(account.pwd)
            mInServerEdit.setText(account.incomingServer)
            mInPortEdit.setText(account.incomingPort.toString())
            mOutServerEdit.setText(account.outServer)
            mOutPortEdit.setText(account.outPort.toString())
        }
    }

    override fun onBackPressed() {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when {
            item?.itemId == R.id.okItem -> {
                checkAccount()
                true
            }
            item?.itemId == android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkAccount() {
        val job = Job()
        val scope = CoroutineScope(Dispatchers.Default + job)
        scope.async {
            val account = EmailAccount(
                email = mEmailEdit.text.toString(),
                pwd = mPwdEdit.text.toString(),
                incomingServer = mInServerEdit.text.toString(),
                incomingPort = mInPortEdit.text.toString().toInt(),
                outServer = mOutServerEdit.text.toString(),
                outPort = mOutPortEdit.text.toString().toInt()
            )
            val processError = fun(errStr: String) {
                runOnUiThread {
                    Toast.makeText(this@AccountSettingsActivity, errStr, Toast.LENGTH_LONG).show()
                }
            }

            if (doCheckAccount(account, processError)) {
                runOnUiThread {
                    val intent = Intent()
                    intent.putExtra(C_ACCOUNT, account)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    private suspend fun doCheckAccount(account: EmailAccount, onError: (errStr: String) -> Unit): Boolean {
        if (App.getEmailsRepository()?.accountExists(account) == true){
            onError(getString(R.string.c_err_Account_Exists))
            return false
        }

        val helper = EmailsIOHelper()
        return helper.checkAccount(account, onError)
    }

    class SMTPAuthenticator(private val userName: String?, private val pwd: String?) : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(userName, pwd)
        }
    }

}