package com.zia.bookdownloader.lib.engine;

import com.zia.bookdownloader.lib.bean.Book;
import com.zia.bookdownloader.lib.listener.EventListener;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created By zia on 2018/11/1.
 */
public interface ISite extends Serializable{

    public List<Book> search(String bookName) throws Exception;

    public File download(Book book, Type type, String savePath, EventListener eventListener);

    public String getSiteName();

    public String getEncodeType();

    public int getThreadCount();

    public void shutDown();
}
