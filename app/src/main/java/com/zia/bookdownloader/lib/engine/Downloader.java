package com.zia.bookdownloader.lib.engine;

import com.zia.bookdownloader.lib.bean.Book;
import com.zia.bookdownloader.lib.listener.EventListener;
import com.zia.bookdownloader.lib.util.SiteUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created By zia on 2018/10/30.
 */
public class Downloader {


    private Type type = Type.EPUB;
    private EventListener eventListener;
    private String path = ".";

    public Downloader(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void search(String bookName) {
        search(bookName, SiteUtil.getAllSites());
    }

    public void search(String bookName, List<ISite> sites) {
        if (path == null) {
            eventListener.onError("请配置文件路径", new FileNotFoundException());
            return;
        }
        eventListener.pushMessage("开始搜索书籍");

        ConcurrentLinkedQueue<List<Book>> bookListList = new ConcurrentLinkedQueue<>();
        CountDownLatch countDownLatch = new CountDownLatch(sites.size());
        ExecutorService service = Executors.newFixedThreadPool(20);

        for (ISite site : sites) {
            service.execute(() -> {
                List<Book> results = null;
                try {
                    results = site.search(bookName);
                    for (Book book : results) {
                        book.setBookName(book.getBookName().replaceAll("[《》]",""));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    eventListener.pushMessage(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
                if (results == null) {
                    eventListener.pushMessage(site.getSiteName() + "搜索结果错误，正在尝试其它网站");
                    return;
                }
                bookListList.add(results);
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            eventListener.onError("搜索时发生并发错误", e);
        } finally {
            service.shutdown();
        }


        int resultSize = 0;
        for (List<Book> bookList : bookListList) {
            resultSize += bookList.size();
        }

        if (resultSize == 0) {
            eventListener.onError("没有搜索到书籍", new IOException());
        }

        //混合插入，每一个站点的
        List<Book> bookList = new ArrayList<>();
        int index = 0;
        while (bookList.size() < resultSize) {
            for (List<Book> bl : bookListList) {
                if (index < bl.size()) {
                    bookList.add(bl.get(index));
                }
            }
            index++;
        }
        //微调排序
        Collections.sort(bookList, (o1, o2) -> {
            if (o1.getBookName().equals(bookName) && !o2.getBookName().equals(bookName)) {
                return -1;
            } else if (!o1.getBookName().equals(bookName) && o2.getBookName().equals(bookName)) {
                return 1;
            } else if (o1.getBookName().contains(bookName) && !o2.getBookName().contains(bookName)) {
                return -1;
            } else if (!o1.getBookName().contains(bookName) && o2.getBookName().contains(bookName)) {
                return 1;
            } else if (o1.getBookName().length() == bookName.length()
                    && o2.getBookName().length() != bookName.length()) {
                return -1;
            } else if (o1.getBookName().length() != bookName.length()
                    && o2.getBookName().length() == bookName.length()) {
                return 1;
            }
            return 0;
        });

        eventListener.pushMessage("搜索到" + bookList.size() + "本相关书籍");

        //选择要下载的书籍
        eventListener.onChooseBook(bookList);

    }

    public void download(Book book) {
        book.getSite().download(book, type, path, eventListener);
    }

    public void setSavePath(String path) {
        this.path = path;
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                eventListener.onError("文件路径创建失败", new Exception("文件路径创建失败"));
            }
        }
    }

    public void setSaveResult(Type type) {
        this.type = type;
    }
}
