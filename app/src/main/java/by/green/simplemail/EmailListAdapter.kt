package by.green.simplemail

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import by.green.simplemail.db.Email


interface HiddenEmailProvider {
    fun getHiddenEmail():Email?
}

class EmailListAdapter(val context: Context?, val listener: EmailsListListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),HiddenEmailProvider {

    interface EmailsListListener {
        fun onBottomScrolled(msgNum: Int?)
        fun onItemClick(email: Email)
    }

    private var mEmails: MutableList<Email>? = null
    private val mInflater = LayoutInflater.from(context)
    private var mHiddenEmail: Email? = null
    private var mHiddenIndex = -1

    override fun getHiddenEmail(): Email? {
        return mHiddenEmail
    }

    fun setEmails(emails: MutableList<Email>?) {
        mHiddenEmail = null
        mHiddenIndex = -1
        mEmails = emails
        notifyDataSetChanged()
    }

    fun hideItem(email: Email) {
        mHiddenIndex = mEmails?.indexOf(email) ?: -1
        mHiddenEmail = email
        notifyDataSetChanged()
    }

    fun restoreHiddenItem(email: Email) {
        mHiddenEmail = null
        mHiddenIndex = -1
    }

    private class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvDate: TextView? = itemView.findViewById(R.id.tvDate)
        var tvTitle: TextView? = itemView.findViewById(R.id.tvTitle)
        var tvSender: TextView? = itemView.findViewById(R.id.tvAuthor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = mInflater.inflate(R.layout.item_email_list, parent, false)
        val vh = VH(view)
        view.setOnClickListener {
            val vh = it.tag as VH
            val pos = vh.adapterPosition
            val email = getItem(pos) ?: return@setOnClickListener
            listener.onItemClick(email)
        }
        view.tag = vh
        return vh
    }

    fun getItem(position: Int): Email? {
        if (mHiddenIndex == -1) {
            return mEmails?.get(position)
        } else {
            if (position >= mHiddenIndex) {
                return mEmails?.get(position + 1)
            } else
                mEmails?.get(position)
        }

        return mEmails?.get(position)
    }


    override fun getItemCount(): Int {
        return if (mHiddenEmail == null)
            mEmails?.size ?: 0 else
            (mEmails?.size?.minus(1) ?: 0)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = (holder as VH)
        val email = getItem(position) //mEmails?.get(position)
        vh.tvDate?.text = email?.date
        vh.tvSender?.text = email?.from_title
        vh.tvTitle?.text = email?.subject

        if (email?.unread == true) {
            vh.tvTitle?.setTypeface(null, Typeface.BOLD)
            vh.tvSender?.setTypeface(null, Typeface.BOLD)
            vh.tvDate?.setTypeface(null, Typeface.BOLD)
        } else {
            vh.tvTitle?.setTypeface(null, Typeface.NORMAL)
            vh.tvSender?.setTypeface(null, Typeface.NORMAL)
            vh.tvDate?.setTypeface(null, Typeface.NORMAL)
        }

        val emails = mEmails
        if ((position == itemCount - 1) && (emails != null) && (itemCount > 0)) {
            val msgNum = (emails[position].msgNum ?: 0) - 1
            if (msgNum >= 0) {
                listener.onBottomScrolled(msgNum)
            }
        }

    }
}