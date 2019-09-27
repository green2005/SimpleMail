package by.green.simplemail

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.widget.EditText
import android.widget.Toast
import by.green.simplemail.db.Email
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface


internal class MyJavaScriptInterface {
    @JavascriptInterface
    fun processHTML(html: String) {
        // process the html as needed by the app
    }
}

class ComposeEmailActivity : AppCompatActivity() {

    private lateinit var mPresenter: EmailsPresenter
    private lateinit var mSubjectEdit: EditText
    private lateinit var mDestEdit: EditText
    private lateinit var mContentEdit: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_compose)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mPresenter = ViewModelProviders.of(this).get(EmailsDataPresenter::class.java)
        mSubjectEdit = findViewById(R.id.subjectEdit)
        mDestEdit = findViewById(R.id.destinationEdit)
        mContentEdit = findViewById(R.id.msgEdit)

        val params = intent.extras
        if ((params != null) && (params.containsKey(EmailDetailActivity.EMAIL))) {
            val email = params.get(EmailDetailActivity.EMAIL) as Email
            var emailContent = ""
            if (params.containsKey(EmailDetailActivity.EMAIL_CONTENT)) {
                emailContent = params.get(EmailDetailActivity.EMAIL_CONTENT) as String
            }
            fillResponseForEmail(emailContent, email)
        }
    }



    fun fillResponseForEmail(emailContent: String, email: Email) {
        mDestEdit.setText(email.from_email)
        mContentEdit.setText("\n" + ">" + "\n" + emailContent)
        mSubjectEdit.setText("Re:" + email.subject)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_compose_email, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.item_send) {
            val content = "" //mContentEdit.text.toString()
            val subject = mSubjectEdit.text.toString()
            val dest = mDestEdit.text.toString()
            mPresenter.sendEmail(subject, dest, content, fun(errMsg: String) {
                runOnUiThread {
                    if (errMsg.isNotEmpty()) {
                        Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
                    } else {
                        val msg_sent = getString(R.string.msg_sent)
                        Toast.makeText(this, msg_sent, Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            })
        } else
            if (item?.itemId == android.R.id.home) {
                finish()
                return true
            }
        return super.onOptionsItemSelected(item)
    }


}