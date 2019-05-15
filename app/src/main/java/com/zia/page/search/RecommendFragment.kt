package com.zia.page.search


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.donkingliang.labels.LabelsView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zia.bookdownloader.R
import com.zia.page.base.BaseFragment
import com.zia.util.ColorConstants
import com.zia.util.defaultSharedPreferences
import com.zia.util.editor
import com.zia.util.threadPool.DefaultExecutorSupplier
import kotlinx.android.synthetic.main.fragment_recommend.*
import java.util.*


/**
 * Created by zzzia on 2019/4/19.
 * 搜索推荐fragment，包含推荐和历史搜索
 */
class RecommendFragment : BaseFragment() {

    var labelClickListener: LabelsView.OnLabelClickListener? = null
    var itemClickListener: AdapterView.OnItemClickListener? = null
    private var labelsJson: String? = null
    private var historyList: ArrayList<String>? = ArrayList()
    private val gson = Gson()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        refreshLabels()
        refreshHistory()

        recommend_labelView.setOnLabelClickListener(labelClickListener)
        recommend_historyListView.onItemClickListener = itemClickListener

        recommend_freshLayout.setOnClickListener {
            refreshLabels()
        }

        recommend_deleteLayout.setOnClickListener {
            setAdapter(ArrayList())
            defaultSharedPreferences().editor {
                putString(SearchContants.SP_HISTORY, "")
            }
        }

    }

    private fun refreshHistory() {
        if (context == null) {
            return
        }
        val historyString = defaultSharedPreferences().getString(SearchContants.SP_HISTORY, "")
        if (historyString == null || historyString.isEmpty()) {
            return
        }
        Log.e("RecommendFragment", "history:$historyString")
        try {
            historyList = gson.fromJson<ArrayList<String>>(historyString, object : TypeToken<ArrayList<String>>() {}
                .type)
            if (historyList != null) {
                activity?.runOnUiThread {
                    setAdapter(historyList!!)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addHistory(str: String) {
        if (isLazyLoaded && historyList != null && !historyList!!.contains(str)) {
            recommend_historyListView.post {
                historyList!!.add(str)
                setAdapter(historyList!!)
                defaultSharedPreferences().editor {
                    Log.e("RecommendFragment", "addHistory:$historyList")
                    putString(SearchContants.SP_HISTORY, gson.toJson(historyList))
                }
            }
        }
    }

    private fun setAdapter(list: List<String>) {
        val adapter = ArrayAdapter(context!!, R.layout.item_catalog, list.reversed())
        recommend_historyListView.adapter = adapter
    }

    //加载推荐标签
    private fun refreshLabels() {
        DefaultExecutorSupplier.getInstance().forLightWeightBackgroundTasks().execute {
            if (labelsJson == null) {
                labelsJson = defaultSharedPreferences().getString(SearchContants.SP_LABELS, "")
            }
            if (labelsJson == null || labelsJson!!.isEmpty()) {
                setLabels(getDefaultLabels().shuffled())
            } else {
                try {
                    setLabels(gson.fromJson<ArrayList<LabelBean>>(labelsJson, object : TypeToken<ArrayList<LabelBean>>
                        () {}.type).shuffled())
                } catch (e: Exception) {
                    setLabels(getDefaultLabels().shuffled())
                }
            }
        }
    }

    private fun setLabels(list: List<LabelBean>) {
        activity?.runOnUiThread {
            recommend_labelView.setLabels(list) { label, _, data ->
                when (data.rankNum) {
                    1 -> {
                        label.setBackgroundColor(ColorConstants.CLASSIFY1)
                    }
                    2 -> {
                        label.setBackgroundColor(ColorConstants.CLASSIFY2)
                    }
                    3 -> {
                        label.setBackgroundColor(ColorConstants.CLASSIFY3)
                    }
                    else -> {
                        label.setBackgroundColor(ColorConstants.CLASSIFY4)
                    }
                }
                data.bookName
            }
        }
    }

    private fun getDefaultLabels(): ArrayList<LabelBean> {
        val list = ArrayList<LabelBean>()
        list.add(LabelBean("逆天邪神", 4))
        list.add(LabelBean("亵渎", 2))
        list.add(LabelBean("紫川", 2))
        list.add(LabelBean("诛仙", 1))
        list.add(LabelBean("超魔杀帝国", 1))
        list.add(LabelBean("斗破苍穹", 4))
        list.add(LabelBean("大王饶命", 3))
        list.add(LabelBean("悟空传", 3))
        list.add(LabelBean("我的青春恋爱物语果然有问题", 4))
        return list
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recommend, container, false)
    }
}
