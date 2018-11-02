package com.zia.bookdownloader.lib.site.h;

import com.zia.bookdownloader.lib.bean.Book;
import com.zia.bookdownloader.lib.bean.Catalog;
import com.zia.bookdownloader.lib.engine.ChapterSite;
import com.zia.bookdownloader.lib.util.BookGriper;
import com.zia.bookdownloader.lib.util.NetUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created By zia on 2018/10/31.
 * 手机看书 http://www.shoujikanshu.cc/
 */
public class Shouji extends ChapterSite {

    private static final String root = "http://www.shoujikanshu.cc";

    @Override
    public String getSiteName() {
        return "手机看书";
    }

    @Override
    public List<Book> search(String bookName) throws Exception {
        int count = 20;
        String url = "http://www.shoujikanshu.cc/book/keyname.php?q=" + URLEncoder.encode(bookName, getEncodeType());
        String html = "";
        while (count-- > 0) {
            String tempHtml = NetUtil.getHtml(url, getEncodeType());
            if (!tempHtml.contains("提示信息")) {
                html = tempHtml;
                break;
            }
        }
        if (html.equals("")) {
            throw new IOException();
        }
        Elements lis = Jsoup.parse(html).getElementsByClass("listbox").first().getElementsByTag("li");
        List<Book> bookList = new ArrayList<>();
        for (Element li : lis) {
            String href = root + li.getElementsByTag("a").first().attr("href");
            StringBuilder stringBuilder = new StringBuilder(href);
            stringBuilder.insert(href.lastIndexOf('/') + 1, "index_");
            String bkName = li.getElementsByTag("a").first().text();
            List<TextNode> textNodes = li.getElementsByClass("info").first().textNodes();
            String lastUpdateTime = textNodes.get(textNodes.size() - 1).text();
            bookList.add(new Book(bkName, "未知", stringBuilder.toString(), "未知", lastUpdateTime, "未知", this));
        }
        return bookList;
    }

    @Override
    public List<Catalog> parseCatalog(String catalogHtml, String url) {
        Elements as = Jsoup.parse(catalogHtml).getElementsByClass("list").first().getElementsByTag("a");
        String u = url.substring(0, url.lastIndexOf('/') + 1);
        List<Catalog> catalogs = new ArrayList<>();
        for (Element a : as) {
            String href = u + a.attr("href");
            String name = a.text();
            catalogs.add(new Catalog(name, href));
        }
        System.out.println(catalogs);
        return catalogs;
    }

    @Override
    public List<String> parseContent(String chapterHtml) {
        List<TextNode> textNodes = Jsoup.parse(chapterHtml).getElementsByClass("content").first().textNodes();
        return BookGriper.getContentsByTextNodes(textNodes);
    }
}
