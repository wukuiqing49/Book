package com.zia.page.preview

import com.zia.easybookmodule.bean.Chapter
import com.zia.easybookmodule.engine.strategy.ContentStrategy

/**
 * Created by zia on 2018/11/21.
 */
class MyContentStrategy : ContentStrategy(){
    /**
     * 取消掉最后的空行
     */
    override fun parseTxtContent(chapter: Chapter): String {
        val sb = StringBuilder()
        sb.append(chapter.chapterName)
        sb.append("\n")
        for (line in chapter.contents) {
            //2个缩进+正文+换行
            sb.append("\u3000")
            sb.append(line)
            sb.append("\n")
        }
        return sb.toString()
    }
}