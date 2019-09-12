package by.green.simplemail

import android.content.Context
import android.view.LayoutInflater
import android.widget.CheckedTextView
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import by.green.simplemail.db.EmailAccount

class AccountsSpinnerAdapter(context: Context) : BaseAdapter() {
    private var mAccounts: List<EmailAccount>? = null
    private lateinit var mInflater: LayoutInflater

    init {
        mInflater = LayoutInflater.from(context)
    }

    public fun setItems(list: List<EmailAccount>) {
        mAccounts = list
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: mInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        val item = view.findViewById<CheckedTextView>(android.R.id.text1)
        item.text = mAccounts?.get(position)?.email
        return view
    }

    override fun getItem(position: Int): Any {
        return mAccounts?.get(position) ?: Any()
    }

    override fun getItemId(position: Int): Long {
        return mAccounts?.get(position)?.id ?: 0
    }

    override fun getCount(): Int {
        return mAccounts?.size ?: 0
    }

}