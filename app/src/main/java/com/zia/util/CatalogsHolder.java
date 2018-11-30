//package com.zia.util;
//
//import com.zia.easybookmodule.bean.Book;
//import com.zia.easybookmodule.bean.Catalog;
//
//import javax.annotation.Nullable;
//import java.util.List;
//
///**
// * Created by zia on 2018/11/7.
// */
//public class CatalogsHolder {
//
//    private List<Catalog> catalogs = null;
//    private Book book = null;
//    private int position = 0;
//
//    public void setCatalogs(List<Catalog> catalogs, Book book, int position) {
//        this.catalogs = catalogs;
//        this.book = book;
//        this.position = position;
//    }
//
//    public @Nullable
//    List<Catalog> getCatalogs() {
//        return catalogs;
//    }
//
//    public @Nullable
//    Book getNetBook() {
//        return book;
//    }
//
//    public @Nullable
//    int getPosition() {
//        return position;
//    }
//
//    public void setPosition(int position) {
//        this.position = position;
//    }
//
//    public void clean() {
//        catalogs = null;
//        book = null;
//        position = 0;
//    }
//
//    private CatalogsHolder() {
//    }
//
//    private static class SingletonHolder {
//        private final static CatalogsHolder instance = new CatalogsHolder();
//    }
//
//    public static CatalogsHolder getInstance() {
//        return SingletonHolder.instance;
//    }
//}
