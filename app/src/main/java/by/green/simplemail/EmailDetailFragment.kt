package by.green.simplemail

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import by.green.simplemail.db.Email
import by.green.simplemail.db.EmailContentPart
import by.green.simplemail.db.EmailContentType


interface EmailContentProvider {
    fun getEmailContent(): String
}

class EmailDetailFragment : Fragment(), EmailDetailsView, EmailContentProvider {

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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_email_detail, container, false)
        val email = arguments?.getParcelable(EmailDetailActivity.EMAIL) as Email
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        mProgress = view.findViewById(R.id.progress)
        mProgress.visibility = View.VISIBLE

        mWebView = view.findViewById(R.id.wvContent)
        mWebView?.setInitialScale(1)
        mWebView?.getSettings()?.setUseWideViewPort(true)
        mWebView?.getSettings()?.setLoadWithOverviewMode(true)
        mWebView?.getSettings()?.setBuiltInZoomControls(true)
        mWebView?.getSettings()?.setSupportZoom(true)

        tvTitle.text = email.subject
        val tvSender = view.findViewById<TextView>(R.id.tvAuthor)
        tvSender.text = email.from_title
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        tvDate.text = email.date

        mPresenter =
            activity?.let { ViewModelProviders.of(it).get(EmailsDataPresenter::class.java) }
                ?: return view
        mPresenter.onEmailDetailsViewCreated(this, email)


        return view
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
        if (content.isNotEmpty()) {
            //mWebView?.getSettings()?.setLoadWithOverviewMode(true)
            // mWebView?.getSettings()?.setUseWideViewPort(true)

            val onPageLoadListener = fun() {
                //   mWebView?.getSettings()?.setLoadWithOverviewMode(true)
                //   mWebView?.getSettings()?.setUseWideViewPort(true)
            }
            //val webClient = CWebClient(onPageLoadListener)
            //mWebView?.webViewClient = webClient
            for (item in content) {

                if (item.contentType == EmailContentType.HTML) {
                    mEmailContent = item
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
                    };
            }

            //mEmailContent = content[content.size - 1]


        }
        mProgress.visibility = View.GONE


        //   mWebView?.settings?.loadWithOverviewMode = true
        //   mWebView?.settings?.useWideViewPort = true
    }

    override fun getEmailContent(): String {
        return mEmailContent?.content ?: ""
    }


}