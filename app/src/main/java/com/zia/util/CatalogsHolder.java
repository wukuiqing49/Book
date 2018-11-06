package com.zia.util;

import com.zia.bookdownloader.lib.bean.Book;
import com.zia.bookdownloader.lib.bean.Catalog;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Created by zia on 2018/11/7.
 */
public class CatalogsHolder {

    private ArrayList<Catalog> catalogs = null;
    private Book book = null;

    public void setCatalogs(ArrayList<Catalog> catalogs, Book book) {
        this.catalogs = catalogs;
        this.book = book;
    }

    public @Nullable
    ArrayList<Catalog> getCatalogs() {
        return catalogs;
    }

    public @Nullable
    Book getNetBook() {
        return book;
    }

    public void clean() {
        catalogs = null;
        book = null;
    }

    private CatalogsHolder() {
    }

    private static class SingletonHolder {
        private final static CatalogsHolder instance = new CatalogsHolder();
    }

    public static CatalogsHolder getInstance() {
        return SingletonHolder.instance;
    }
}
