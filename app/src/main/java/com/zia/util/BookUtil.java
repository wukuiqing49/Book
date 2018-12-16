package com.zia.util;

import com.zia.easybookmodule.bean.Book;
import com.zia.easybookmodule.engine.Site;
import com.zia.easybookmodule.engine.SiteCollection;

import java.util.Iterator;

/**
 * Created by zia on 2018/12/14.
 */
public class BookUtil {
    /**
     * 通过小说名字和站点名字能够构成唯一的id
     * @param book
     * @return
     */
    public static String buildId(Book book) {
        return book.getBookName() + "_" + book.getSiteName();
    }

    public static String buildId(String bookName, String siteName) {
        return bookName + "_" + siteName;
    }

    public static Site getSite(String siteName) {
        Iterator var1 = SiteCollection.getInstance().getAllSites().iterator();

        Site site;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            site = (Site) var1.next();
        } while (!site.getSiteName().equals(siteName));

        return site;
    }
}
