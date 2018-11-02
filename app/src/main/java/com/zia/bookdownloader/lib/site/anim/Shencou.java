package com.zia.bookdownloader.lib.site.anim;

import com.zia.bookdownloader.lib.bean.Book;
import com.zia.bookdownloader.lib.bean.Catalog;
import com.zia.bookdownloader.lib.engine.ChapterSite;
import com.zia.bookdownloader.lib.util.NetUtil;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.net.URLEncoder;
import java.util.List;

/**
 * Created By zia on 2018/10/31.
 * 神凑轻小说 http://www.shencou.com/
 */
public class Shencou extends ChapterSite {
    @Override
    public String getSiteName() {
        return "神凑轻小说";
    }

    @Override
    public List<Book> search(String bookName) throws Exception {
        String url = "http://www.shencou.com/modules/article/search.php";
        RequestBody requestBody = new FormBody.Builder()
                .addEncoded("searchkey", URLEncoder.encode(bookName, getEncodeType()))
                .build();
        String html = NetUtil.getHtml(url, requestBody, getEncodeType());
        return null;
    }

    @Override
    public List<Catalog> parseCatalog(String catalogHtml, String url) {
        return null;
    }

    @Override
    public List<String> parseContent(String chapterHtml) {
        return null;
    }
}
