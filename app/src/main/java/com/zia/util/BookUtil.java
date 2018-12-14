package com.zia.util;

import com.zia.easybookmodule.bean.Book;
import com.zia.easybookmodule.engine.Site;
import com.zia.easybookmodule.engine.SiteCollection;

import java.util.Iterator;

/**
 * Created by zia on 2018/12/14.
 */
public class BookUtil {
    public static String buildId(Book book) {
        return book.getBookName() + "_" + book.getSiteName();
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
