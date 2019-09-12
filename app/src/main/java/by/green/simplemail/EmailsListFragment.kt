package by.green.simplemail

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.VERTICAL
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import by.green.simplemail.db.Email
import by.green.simplemail.db.EmailFolder

interface EmailsViewObserver {
    fun getEmailObserver(): Observer<List<Email>>
}

class EmailsListFragment : Fragment(), EmailsViewObserver {

    private enum class DataState {
        LOADING,
        BROWSING,
        NO_MORE_DATA
    }

    private var mObserver: Observer<List<Email>>? = null
    private var mAdapter: EmailListAdapter? = null
    private lateinit var mSwipeLayout: SwipeRefreshLayout
    private lateinit var mEmailsPresenter: EmailsPresenter
    private var mState: DataState = DataState.BROWSING
    private lateinit var mProgressPanel: ConstraintLayout
    private lateinit var mFolder: EmailFolder
    private lateinit var mProgressLoading: ProgressBar
    // private var mEmailsStartFrom: Int = 0

    companion object {
        fun getFragment(params: Bundle?): EmailsListFragment {
            val fragment = EmailsListFragment()
            fragment.arguments = params
            return fragment
        }

        const val FOLDER_PARAM = "folder"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_emails_list, container, false)
        mProgressLoading = view.findViewById(R.id.progressBarLoading)
        mSwipeLayout = view.findViewById(R.id.swipe)
        mProgressPanel = view.findViewById(R.id.progressMorePanel)
        val args = arguments
        mFolder = args?.getParcelable(FOLDER_PARAM)
            ?: throw IllegalArgumentException("Folder param is empty")

        mSwipeLayout.setOnRefreshListener {
            if (mState == DataState.NO_MORE_DATA) {
                mState = DataState.BROWSING
            }
            loadEmails()
            // mEmailsPresenter.onRequestEmails(mFolder, mNextPageToken)
        }


        val emailsUIListener: EmailListAdapter.EmailsListListener =
            object : EmailListAdapter.EmailsListListener {
                override fun onItemClick(email: Email) {

                    activity?.let { mEmailsPresenter.showEmailDetails(email, it) }
                }

                override fun onBottomScrolled(msgNum: Int?) {
                    if (msgNum == 0) {
                        mState = DataState.NO_MORE_DATA
                    } else {
                        loadEmails(msgNum ?: 0)
                    }
                }
            }
        mAdapter = EmailListAdapter(activity, emailsUIListener)
        val rvEmails = view.findViewById(R.id.rvEmails) as RecyclerView
        rvEmails.layoutManager = LinearLayoutManager(this.activity)
        val dividerItemDecor = DividerItemDecoration(activity, VERTICAL)
        rvEmails.addItemDecoration(dividerItemDecor)
        rvEmails.adapter = mAdapter
        val onItemSwipeListener: EmailsItemTouchHelper.SwipeListsener =
            object : EmailsItemTouchHelper.SwipeListsener {

                override fun onItemSwiped(adapterIndex: Int) {
                    val email = mAdapter?.getItem(adapterIndex) ?: return
                    val prevHiddenEmail = mAdapter?.getHiddenEmail()
                    if (prevHiddenEmail != null) {
                        mEmailsPresenter.deleteEmail(prevHiddenEmail)
                        mAdapter?.notifyDataSetChanged()
                    }
                    mAdapter?.hideItem(email)
                    showSnackBar(email)
                }
            }

        val emailsItemTouchHelper =
            EmailsItemTouchHelper(
                0, //ItemTouchHelper.LEFT or
                ItemTouchHelper.RIGHT
                , onItemSwipeListener, activity
            )

        val itemTouchHelper = ItemTouchHelper(emailsItemTouchHelper)
        itemTouchHelper.attachToRecyclerView(rvEmails)
        val context = activity
        if (context != null) {
            mEmailsPresenter = ViewModelProviders.of(context).get(EmailsDataPresenter::class.java)
        }
        loadEmails()
        return view
    }


    private class SnackBarCallback(
        val email: Email,
        val adapter: EmailListAdapter,
        val presenter: EmailsPresenter
    ) :
        Snackbar.Callback() {


        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                presenter.deleteEmail(email)
                adapter.notifyDataSetChanged()
            } else {
                adapter.notifyDataSetChanged()
            }
        }
    }


    private fun showSnackBar(email: Email) {
        val view = activity?.findViewById<ConstraintLayout>(R.id.cla) ?: return
        val snackbar = Snackbar.make(view, "Email was deleted", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo deleting?",
            View.OnClickListener {
                mAdapter?.restoreHiddenItem(email)
                snackbar.dismiss()
            })
        snackbar.addCallback(SnackBarCallback(email, mAdapter ?: return, mEmailsPresenter))
        snackbar.show()
    }

    private fun loadEmails(msgNum: Int = 0) {
        if ((mState == DataState.LOADING) || (mState == DataState.NO_MORE_DATA)) return
        if (msgNum != 0) {
            mProgressPanel.visibility = View.VISIBLE
        } else {
            if (mAdapter?.itemCount == 0) {
                mProgressLoading.visibility = View.VISIBLE
            } else {
                mSwipeLayout.isRefreshing = true
            }
        }
        mState = DataState.LOADING
        mEmailsPresenter.onRequestEmails(mFolder, msgNum, fun(error: String) {
            hideLoadingUI()
            if (error.isNotEmpty()) {
                Toast.makeText(activity, error, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun getEmailObserver(): Observer<List<Email>> {
        val observer = mObserver ?: Observer {
            mAdapter?.setEmails(it as MutableList<Email>)
            if (it?.isEmpty() != false) {
                mState = DataState.NO_MORE_DATA
            } else {
                mState = DataState.BROWSING
                //observer is called twice
                //first time we get cached data
                //second time - fresh data from network
                //if cahched data is not empty - we hide progress
                hideLoadingUI()
            }
        }
        mObserver = observer
        return observer
    }

    private fun hideLoadingUI() {
        mSwipeLayout.isRefreshing = false
        mProgressPanel.visibility = View.GONE
        mProgressLoading.visibility = View.GONE
    }
}