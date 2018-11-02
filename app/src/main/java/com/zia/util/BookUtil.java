package com.zia.util;

import com.zia.bookdownloader.lib.engine.ChapterSite;
import com.zia.bookdownloader.lib.engine.ISite;
import com.zia.bookdownloader.lib.util.NetUtil;
import com.zia.bookdownloader.lib.util.SiteUtil;
import com.zia.database.AppDatabase;
import com.zia.database.bean.NetBook;
import com.zia.database.bean.NetBookDao;

import java.io.IOException;
import java.util.List;

/**
 * Created by zia on 2018/11/2.
 */
public class BookUtil {
    public static int updateNetBook() {
        NetBookDao netBookDao = AppDatabase.getAppDatabase().netBookDao();
        List<NetBook> netBooks = netBookDao.getNetBooks();
        int c = 0;
        for (NetBook netBook : netBooks) {
            ChapterSite site = getSiteByName(netBook.getSiteName());
            try {
                String html = NetUtil.getHtml(netBook.getUrl(), site.getEncodeType());
                int size = site.parseCatalog(html, netBook.getUrl()).size();
                if (netBook.getLastCheckCount() < size) {
                    netBook.setCurrentCheckCount(size);
                    netBookDao.update(netBook);
                    c++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return c;
    }

    public static ChapterSite getSiteByName(String siteName) {
        for (ISite iSite : SiteUtil.getAllSites()) {
            if (iSite.getSiteName().equals(siteName)) {
                return (ChapterSite) iSite;
            }
        }
        return null;
    }
}
