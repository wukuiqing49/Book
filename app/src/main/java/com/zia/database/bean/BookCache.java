package com.zia.database.bean;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;

import java.util.List;

/**
 * Created by zia on 2018/11/30.
 */
@Entity(tableName = "bookCache",
        indices = {@Index(value = {"siteName", "bookName", "index"}, unique = true)},
        primaryKeys = {"siteName", "bookName", "index"})
@TypeConverters(ContentsConverter.class)
public class BookCache {
    @ColumnInfo(name = "siteName")
    @NonNull
    private String siteName;

    @ColumnInfo(name = "bookName")
    @NonNull
    private String bookName;

    @ColumnInfo(name = "index")
    private int index;

    @ColumnInfo(name = "chapterName")
    private String chapterName;

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "contents")
    private List<String> contents;


    public BookCache(@NonNull String siteName, @NonNull String bookName, int index, String chapterName, String url, List<String> contents) {
        this.siteName = siteName;
        this.bookName = bookName;
        this.index = index;
        this.chapterName = chapterName;
        this.url = url;
        this.contents = contents;
    }

    @Override
    public String toString() {
        return "BookCache{" +
                "siteName='" + siteName + '\'' +
                ", bookName='" + bookName + '\'' +
                ", index=" + index +
                ", chapterName='" + chapterName + '\'' +
                ", url='" + url + '\'' +
                ", contents=" + contents +
                '}';
    }

    public int getIndex() {
        return index;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public List<String> getContents() {
        return contents;
    }

    public void setContents(List<String> contents) {
        this.contents = contents;
    }
}
