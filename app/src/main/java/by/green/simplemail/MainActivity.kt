package by.green.simplemail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import by.green.simplemail.db.Email
import by.green.simplemail.db.EmailAccount
import by.green.simplemail.db.EmailFolder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    EmailsView {


    private lateinit var mPresenter: EmailsPresenter
    private var mAccountsAdapter: AccountsSpinnerAdapter? = null
    private lateinit var mSpinner: Spinner
    private lateinit var mNavView: NavigationView
    private lateinit var mSettingsBtn: ImageButton
    private var mAccounts: List<EmailAccount>? = null
    private var mFolders: List<EmailFolder>? = null
    private var mEmailsObserver: Observer<List<Email>>? = null
    private lateinit var mTvEmailTitle: TextView
    private lateinit var mTvFolderTitle :TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        mTvEmailTitle = toolbar.findViewById(R.id.email_title)
        mTvFolderTitle = toolbar.findViewById(R.id.folder_title)

        mPresenter = ViewModelProviders.of(this).get(EmailsDataPresenter::class.java)
        mPresenter.onViewCreated(this)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            val intent = Intent(this, ComposeEmailActivity::class.java)
            startActivity(intent)
        }

        val drawerLayout: androidx.drawerlayout.widget.DrawerLayout =
            findViewById(R.id.drawer_layout)
        mNavView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        val view = mNavView.getHeaderView(0)
        mSpinner = view.findViewById(R.id.spinner)
        mSettingsBtn = view.findViewById(R.id.btn_settings)
        mSpinner.adapter = mAccountsAdapter

        mSettingsBtn.setOnClickListener {
            editAccountSettings()
        }

        mSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = mAccounts?.get(position)
                mPresenter.onViewActiveAccountChanged(account = item ?: return)
                mTvEmailTitle.text = item.email
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        mNavView.setNavigationItemSelectedListener(this)
        val btnAddAccount = view.findViewById<Button>(R.id.btn_add_account)
        btnAddAccount.setOnClickListener {
            newAccount()
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onViewDestroyed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == AccountSettingsActivity.C_ADD_ACCOUNT) && (resultCode == Activity.RESULT_OK)) {
            val account = data?.getParcelableExtra<EmailAccount>(AccountSettingsActivity.C_ACCOUNT)
            mPresenter.onEmailAccountAdded(account ?: return)
        } else
            if ((requestCode == AccountSettingsActivity.C_EDIT_ACCOUNT) && (resultCode == Activity.RESULT_OK)) {
                val account =
                    data?.getParcelableExtra<EmailAccount>(AccountSettingsActivity.C_ACCOUNT)
                App.getEmailsRepository()?.getCurrentEmailAccount()?.incomingPort =
                    account?.incomingPort ?: 0
                App.getEmailsRepository()?.getCurrentEmailAccount()?.incomingServer =
                    account?.incomingServer ?: ""
                App.getEmailsRepository()?.getCurrentEmailAccount()?.outServer =
                    account?.outServer ?: ""
                App.getEmailsRepository()?.getCurrentEmailAccount()?.outPort = account?.outPort ?: 0
                App.getEmailsRepository()?.getCurrentEmailAccount()?.pwd = account?.pwd ?: ""
                mPresenter.onEmailAccountEdit(account ?: return)
            }
    }

    private fun newAccount() {
        val intent = Intent(this, AccountSettingsActivity::class.java)
        intent.putExtra(
            AccountSettingsActivity.C_REQUEST_CODE,
            AccountSettingsActivity.C_ADD_ACCOUNT
        )
        startActivityForResult(intent, AccountSettingsActivity.C_ADD_ACCOUNT)
    }

    private fun editAccountSettings() {
        val intent = Intent(this, AccountSettingsActivity::class.java)
        intent.putExtra(
            AccountSettingsActivity.C_ACCOUNT,
            App.getEmailsRepository()?.getCurrentEmailAccount()
        )
        intent.putExtra(
            AccountSettingsActivity.C_REQUEST_CODE,
            AccountSettingsActivity.C_EDIT_ACCOUNT
        )
        startActivityForResult(intent, AccountSettingsActivity.C_EDIT_ACCOUNT)
    }

    override fun onBackPressed() {
        val drawerLayout: androidx.drawerlayout.widget.DrawerLayout =
            findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main2, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getLifeCycleOwner(): LifecycleOwner {
        return this
    }

    override fun getAccountsObserver(): Observer<List<EmailAccount>> {
        return Observer<List<EmailAccount>> {
            if (it != null) {
                for (item in it) {
                    if ((item.isActive.toInt() == 1) && (item.id == App.getEmailsRepository()?.getCurrentEmailAccount()?.id)) {
                        return@Observer
                    }
                }
            }
            mAccounts = it
            mNavView.menu.clear()
            mAccountsAdapter = AccountsSpinnerAdapter(this)
            mAccountsAdapter?.setItems(mAccounts ?: return@Observer)
            mSpinner.adapter = mAccountsAdapter
            mAccountsAdapter?.notifyDataSetChanged()
            var activeIndex = -1
            if (it != null) {
                for (item in it) {
                    if (item.isActive.compareTo(1) == 0) {
                        activeIndex = it.indexOf(item)
                        break
                    }
                }
            }
            if (activeIndex > -1) {
                mSpinner.setSelection(activeIndex)
                mTvEmailTitle.text = mAccounts?.get(activeIndex)?.email ?: return@Observer
                mPresenter.onViewActiveAccountChanged(
                    mAccounts?.get(activeIndex) ?: return@Observer
                )
            }
            if (it?.size == 0) {
                val drawerLayout: androidx.drawerlayout.widget.DrawerLayout =
                    findViewById(R.id.drawer_layout)
                drawerLayout.openDrawer(GravityCompat.START)
                mTvEmailTitle.text = ""
            }
        }
    }

    override fun getFoldersObserver(emailAccount: EmailAccount): Observer<List<EmailFolder>> {
        return Observer {
            var incomingFolder: EmailFolder? = null
            var incomingItem: MenuItem? = null
            mFolders = it


            mNavView.menu.clear()
            var i = 0
            if (it != null) {
                for (folder in it) {
                    val id: Int = (folder.id ?: 0).toInt()
                    val menuItem = mNavView.menu.add(0, id, i++, folder.name)
                    if (folder.name.toUpperCase().equals("INBOX")) {
                        incomingFolder = folder
                        incomingItem = menuItem
                    }
                }
            }
            if ((incomingFolder != null) && (incomingItem != null)) {
                incomingItem.setChecked(true)
                // if (!selectedName.contentEquals(incomingItem.title.toString())) {
                showEmails(incomingFolder)
                //  }
                //onNavigationItemSelected(incomingItem)
            }
        }
    }


    override fun getEmailsObserver(folder: EmailFolder): Observer<List<Email>>? {
        return  mEmailsObserver
    }


    private fun showEmails(folder: EmailFolder) {
        mTvFolderTitle.text = folder.name
        val params = Bundle()
        params.putParcelable(EmailsListFragment.FOLDER_PARAM, folder)
        val emailsFragment = EmailsListFragment.getFragment(params)

        val fm = supportFragmentManager
        val tran = fm.beginTransaction()
        tran.replace(R.id.frame, emailsFragment)
        tran.commit()

        val emailsObserver = emailsFragment as EmailsViewObserver
        mEmailsObserver = emailsObserver.getEmailObserver()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.isChecked) return true

        for (i in 0 until mNavView.menu.size()) {
            val menuItem = mNavView.menu.getItem(i)
            menuItem.setChecked(false)
        }
        item.setChecked(true)

        for (folder in mFolders ?: return true) {
            if ((folder.id ?: 0).toInt() == item.itemId) {
                showEmails(folder)
                break
            }
        }
        val drawerLayout: androidx.drawerlayout.widget.DrawerLayout =
            findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
