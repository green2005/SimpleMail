package by.green.simplemail

import android.Manifest
import androidx.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import by.green.simplemail.db.Email
import by.green.simplemail.db.EmailContentPart
import by.green.simplemail.db.EmailContentType


interface EmailContentProvider {
    fun getEmailContent(): String
}

class EmailDetailFragment : androidx.fragment.app.Fragment(), EmailDetailsView, EmailContentProvider {

    companion object {
        const val PIC_WIDTH = 0.2

        fun getFragment(params: Bundle?): EmailDetailFragment {
            val fragment = EmailDetailFragment()
            fragment.arguments = params
            return fragment
        }
    }


    private lateinit var mPresenter: EmailsPresenter
    private var mWebView: WebView? = null
    private lateinit var mProgress: ProgressBar
    private var mEmailContent: EmailContentPart? = null
    private lateinit var mView: View
    private lateinit var mScrollView: View
    private lateinit var mTvDate: TextView
    private val mAttachments = ArrayList<EmailContentPart>()
    private lateinit var mAttachmentsLV: LinearLayout
    private lateinit var mEmail: Email
    private var mAttachmentId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mView = inflater.inflate(R.layout.fragment_email_detail, container, false)
        mAttachmentsLV = mView.findViewById(R.id.lvAttachments)
        mScrollView = mView.findViewById(R.id.scrollView)
        if (arguments?.containsKey(EmailDetailActivity.EMAIL) == true) {
            mEmail = arguments?.getParcelable(EmailDetailActivity.EMAIL)?:return mView
        }
        val tvTitle = mView.findViewById<TextView>(R.id.tvTitle)
        mProgress = mView.findViewById(R.id.progress) ?: return mView
        mProgress.visibility = View.VISIBLE

        mWebView = mView.findViewById(R.id.wvContent)
        mWebView?.setInitialScale(1)
        mWebView?.getSettings()?.setUseWideViewPort(true)
        mWebView?.getSettings()?.setLoadWithOverviewMode(true)
        mWebView?.getSettings()?.setBuiltInZoomControls(true)
        mWebView?.getSettings()?.setSupportZoom(true)

        tvTitle.text = mEmail.subject
        val tvSender = mView.findViewById<TextView>(R.id.tvAuthor)
        tvSender.text = mEmail.from_title
        mTvDate = mView.findViewById<TextView>(R.id.tvDate)
        mTvDate.text = mEmail.date

        mPresenter =
            activity?.let { ViewModelProviders.of(it).get(EmailsDataPresenter::class.java) }
                ?: return mView
        mPresenter.onEmailDetailsViewCreated(this, mEmail)
        return mView
    }

    override fun onDestroy() {
        mPresenter.onEmailDetailsViewDestroyed()
        super.onDestroy()
    }

    private class CWebClient(val onLoadListener: () -> Unit) : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onLoadListener()
        }
    }

    override fun showEmailDetails(content: List<EmailContentPart>) {
        mWebView?.settings?.javaScriptEnabled = true
        var attachmentNo = 0
        if (content.isNotEmpty()) {
            for (item in content) {
                if (item.contentType == EmailContentType.HTML) {
                    mEmailContent = item

                    mWebView?.getSettings()?.setJavaScriptEnabled(true);
                    mWebView?.getSettings()?.setLoadWithOverviewMode(true);
                    mWebView?.getSettings()?.setUseWideViewPort(true);
                    mWebView?.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
                    mWebView?.setScrollbarFadingEnabled(false);

                    mWebView?.loadDataWithBaseURL(
                        "", item.content,
                        //"text/plain",
                        "text/html",
                        "UTF-8", ""
                    )


                } else
                    if (item.contentType == EmailContentType.TXT) {
                        mEmailContent = item
                        mWebView?.loadDataWithBaseURL(
                            "", item.content,
                            //"text/plain",
                            "text/plain",
                            "UTF-8", ""
                        )
                    } else
                        if (item.contentType == EmailContentType.FILE) {
                            mAttachments.add(item)
                        }
            }

        }
        if (mAttachments.size > 0) {
            initAttachments()
        }
        mProgress.visibility = View.GONE
    }

    private fun initAttachments() {
        for (item in mAttachments) {
            val vAttachment = layoutInflater.inflate(R.layout.attachment_item, null)
            val tv = vAttachment.findViewById<TextView>(R.id.tvItem)
            tv.text = item.content
            tv.tag = item.sectionId
            tv.setOnClickListener {
                openAttachment((it as TextView).tag as Int)
            }
            mAttachmentsLV.addView(vAttachment)
        }
    }

    private fun openAttachment(attachmentId: Int) {
        val onResult: (String) -> Unit = {

        }
        if (ContextCompat.checkSelfPermission(
                this.activity?.baseContext ?: return,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            mAttachmentId = attachmentId
            val a = Array<String>(1) { Manifest.permission.WRITE_EXTERNAL_STORAGE }
            ActivityCompat.requestPermissions(
                activity ?: return,
                a, 0
            )
            //return null
            // Permission is not granted
        } else
            mPresenter.showEmailAttachment(
                activity?.baseContext ?: return,
                mEmail,
                attachmentId,
                onResult
            )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openAttachment(mAttachmentId)
        }
    }

    override fun getEmailContent(): String {
        return mEmailContent?.content ?: ""
    }


}