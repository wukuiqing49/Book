package com.zia.bookdownloader.lib.listener;

import com.zia.bookdownloader.lib.bean.Book;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public interface EventListener extends Serializable {
    void onChooseBook(List<Book> books);

    void pushMessage(String msg);

    void onDownload(int progress, String msg);

    void onEnd(String msg, File file);

    void onError(String msg, Exception e);
}
