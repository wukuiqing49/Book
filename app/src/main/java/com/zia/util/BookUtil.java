package com.zia.util;

import com.zia.bookdownloader.lib.bean.Catalog;
import com.zia.bookdownloader.lib.engine.ChapterSite;
import com.zia.bookdownloader.lib.engine.ISite;
import com.zia.bookdownloader.lib.util.NetUtil;
import com.zia.bookdownloader.lib.util.SiteUtil;
import com.zia.database.AppDatabase;
import com.zia.database.bean.NetBook;
import com.zia.database.bean.NetBookDao;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zia on 2018/11/2.
 */
public class BookUtil {
    public static int updateNetBook() {
        ExecutorService service = Executors.newFixedThreadPool(5);
        NetBookDao netBookDao = AppDatabase.getAppDatabase().netBookDao();
        List<NetBook> netBooks = netBookDao.getNetBooks();
        CountDownLatch bookCountDown = new CountDownLatch(netBooks.size());
        CountDownLatch sizeCountDown = new CountDownLatch(netBooks.size());
        for (NetBook netBook : netBooks) {
            service.execute(() -> {
                ChapterSite site = getSiteByName(netBook.getSiteName());
                try {
                    String html = NetUtil.getHtml(netBook.getUrl(), site.getEncodeType());
                    List<Catalog> catalogs = site.parseCatalog(html, netBook.getUrl());
                    if (netBook.getLastCheckCount() < catalogs.size()) {
                        netBook.setCurrentCheckCount(catalogs.size());
                        netBook.setLastChapterName(catalogs.get(catalogs.size() - 1).getChapterName());
                        netBookDao.update(netBook);
                        sizeCountDown.countDown();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    bookCountDown.countDown();
                }
            });
        }
        int size = netBooks.size();
        try {
            bookCountDown.await();
            service.shutdown();
            return (int) (size - sizeCountDown.getCount());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            service.shutdown();
        }
        return 0;
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
