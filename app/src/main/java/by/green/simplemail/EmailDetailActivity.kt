package by.green.simplemail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import by.green.simplemail.db.Email


class EmailDetailActivity : AppCompatActivity() {

    companion object {
        val EMAIL = "email"
        val EMAIL_CONTENT = "email_details"
    }

    private lateinit var mEmail: Email
    private lateinit var mFm: EmailDetailFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_detail)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mEmail = intent.extras?.getParcelable(EmailDetailActivity.EMAIL) ?: return as Email
        mFm = EmailDetailFragment.getFragment(intent.extras)
        val tran = supportFragmentManager.beginTransaction()
        tran.replace(R.id.frame, mFm)
        tran.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val presenter = ViewModelProviders.of(this).get(EmailsDataPresenter::class.java)

        return when {
            item?.itemId == android.R.id.home -> {
                finish()
                true
            }
            item?.itemId == R.id.setUnread -> {
                presenter.setEmailRead(mEmail, false)
                finish()
                true
            }
            item?.itemId == R.id.deleteMail -> {
                presenter.deleteEmail(mEmail)
                finish()
                true
            }
            item?.itemId == R.id.reply -> {
                val intent = Intent(this, ComposeEmailActivity::class.java)
                intent.putExtra(EMAIL, mEmail)
                val emailContent = mFm.getEmailContent()
                intent.putExtra(EMAIL_CONTENT, emailContent)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail_activity, menu)
        return true
    }
}