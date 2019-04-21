package com.zia.page.search

import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import com.donkingliang.labels.LabelsView
import com.zia.bookdownloader.R
import com.zia.easybookmodule.bean.Book
import com.zia.page.base.BaseActivity
import com.zia.util.KeyboardktUtils
import com.zia.util.ToastUtil
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.item_catalog.view.*

class SearchActivity : BaseActivity() {

    private lateinit var viewModel: SearchViewModel

    private val searchFragment = SearchResultFragment()
    private val recommendFragment = RecommendFragment()

    private val dialog by lazy {
        val dialog = ProgressDialog(this)
        dialog.setCancelable(true)
        dialog.progress = 0
        dialog.setTitle("正在搜索")
        dialog.setMessage("")
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog.show()
        dialog.setOnCancelListener {
            viewModel.shutDown()
        }
        dialog
    }

    private var searchKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchKey = intent.getStringExtra("searchKey")

        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)

        addFragment(searchFragment)
        addFragment(recommendFragment)

        recommendFragment.labelClickListener = LabelsView.OnLabelClickListener { label, data, position ->
            searchEt.setText(label.text)
        }

        recommendFragment.itemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            searchEt.setText(view.item_catalog_name.text)
        }

        initObservers()

        initSearchLayout()

        if (searchKey != null && searchKey!!.isNotEmpty()) {
            searchEt.setText(searchKey)
            viewModel.search(searchKey!!)
        }
    }

    private fun initObservers() {
        viewModel.loadBooks.observe(this, Observer<List<Book>> {
            if (it != null) {
                hideFragment(recommendFragment)
                ToastUtil.onSuccess("搜索到${it.size}本书籍")
                searchFragment.bookAdapter?.freshBooks(ArrayList(it))
                searchFragment.searchRv?.scrollToPosition(0)
                hideDialog()
            }
        })

        viewModel.error.observe(this, Observer {
            it?.printStackTrace()
            ToastUtil.onError(it?.message)
            hideDialog()
        })

        viewModel.toast.observe(this, Observer {
            ToastUtil.onInfo(it)
        })

        viewModel.dialogMessage.observe(this, Observer {
            updateDialog(it)
        })

        viewModel.dialogProgress.observe(this, Observer {
            updateDialog(it)
        })
    }

    private fun search() {
        viewModel.shutDown()
        val bookName = searchEt.text?.toString()
        if (bookName != null && bookName.isNotEmpty()) {
            recommendFragment.addHistory(bookName)
            initDialog()
            viewModel.search(bookName)
        }
    }

    private fun initSearchLayout() {
        search_edit_cancel.setOnClickListener {
            searchEt.setText("")
        }

        searchEt.setOnEditorActionListener { _, actionId, event ->
            if ((actionId == KeyEvent.KEYCODE_UNKNOWN || actionId == KeyEvent.KEYCODE_SEARCH || actionId == KeyEvent.KEYCODE_HOME)
                && event != null && event.action == KeyEvent.ACTION_DOWN
            ) {
                search()
                KeyboardktUtils.hideKeyboard(searchBt)
                search_edit_cancel.visibility = View.INVISIBLE
                return@setOnEditorActionListener true
            }
            false
        }

        searchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!searchEt.text?.toString().isNullOrEmpty()) {
                    search_edit_cancel.visibility = View.VISIBLE
                } else {
                    search_edit_cancel.visibility = View.INVISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        searchBt.setOnClickListener {
            search()
        }
    }

    private fun initDialog() {
        updateDialog(0)
        updateDialog("")
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.search_fragmentLayout, fragment)
        transaction.commit()
    }

    private fun addFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.search_fragmentLayout, fragment)
        transaction.commit()
    }

    private fun hideFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.hide(fragment)
        transaction.commit()
    }

    private fun updateDialog(progress: Int?) {
        if (progress != null) {
            dialog.progress = progress
        }
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun updateDialog(msg: String?) {
        if (msg != null) {
            dialog.setMessage(msg)
        }
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun hideDialog() {
        dialog.dismiss()
    }
}
