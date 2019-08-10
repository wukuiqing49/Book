package com.zia.page.usersite

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zia.easybookmodule.bean.rule.XpathSiteRule
import com.zia.easybookmodule.engine.Site
import com.zia.easybookmodule.engine.SiteCollection
import com.zia.easybookmodule.net.NetUtil
import com.zia.easybookmodule.site.CustomXpathSite
import com.zia.page.base.BaseViewModel
import com.zia.util.FileUtil
import com.zia.util.MergeUtil
import com.zia.util.threadPool.DefaultExecutorSupplier

/**
 * Created by zia on 2019-06-02.
 */
class CustomViewModel : BaseViewModel() {

    val openDialog = MutableLiveData<Boolean>()
    val localRules = MutableLiveData<List<XpathSiteRule>>()
    val netRules = MutableLiveData<List<XpathSiteRule>>()
    val saveFileStatus = MutableLiveData<Boolean>()

    private val gson = Gson()

    fun getRulesFromNet(url: String) {
        openDialog.postValue(true)
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute {
            try {
                val json = NetUtil.getHtml(url, "utf-8")
                val rulesTemp = getRulesFromJson(json)
                toast.postValue("获取成功")
                netRules.postValue(rulesTemp)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                toast.postValue(e.message)
            } finally {
                openDialog.postValue(false)
            }
        }
    }

    fun readRulesFromFile() {
        openDialog.postValue(true)
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute {
            try {
                val json = FileUtil.getString(FileUtil.rulePath)
                if (json == null || json.isBlank()) {
                    toast.postValue("还没有添加过书源..")
                } else {
                    val rulesTemp = getRulesFromJson(json)
                    netRules.postValue(rulesTemp)
                }
                openDialog.postValue(false)
            } catch (e: Exception) {
                e.printStackTrace()
                toast.postValue(e.message)
            }
        }
    }

    fun saveRulesToFile(rules: List<XpathSiteRule>) {
        openDialog.postValue(true)
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute {
            val sites = ArrayList<Site>()
            rules.forEach {
                sites.add(CustomXpathSite(it))
            }
            val mergeList = MergeUtil.mergeListNoRepeat(SiteCollection.getInstance().allSites, sites) { site ->
                site.siteName
            }
            SiteCollection.getInstance().allSites.clear()
            SiteCollection.getInstance().addSites(mergeList)
            try {
                val json = gson.toJson(rules)
                FileUtil.writeFile(FileUtil.rulePath, json)
                saveFileStatus.postValue(true)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                saveFileStatus.postValue(false)
            } finally {
                openDialog.postValue(false)
            }
        }
    }

    private fun getRulesFromJson(json: String): List<XpathSiteRule> {
        return gson.fromJson<List<XpathSiteRule>>(
            json, TypeToken.getParameterized(List::class.java, XpathSiteRule::class.java).type
        )
    }
}