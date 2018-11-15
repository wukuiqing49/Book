package com.zia.database.bean;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;
import com.zia.easybookmodule.bean.Book;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zia on 2018/11/2.
 */

@Entity(tableName = "netBook")
public class NetBook implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int bkId;

    @ColumnInfo(name = "bookName")
    private String bookName;

    @ColumnInfo(name = "author")
    private String author = "未知";

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "chapterSize")
    private String chapterSize = "未知";

    @ColumnInfo(name = "lastUpdateTime")
    private String lastUpdateTime = "未知";

    @ColumnInfo(name = "lastChapterName")
    private String lastChapterName = "未知";

    @ColumnInfo(name = "siteName")
    private String siteName = "未知";

    @ColumnInfo(name = "lastCheckCount")
    private int lastCheckCount;//记录的阅读数量

    @ColumnInfo(name = "currentCheckCount")
    private int currentCheckCount;//最新章节数量

    @ColumnInfo(name = "time")
    private long time = new Date().getTime();

    public NetBook() {
    }

    public NetBook(Book book, int lastCheckCount) {
        bookName = book.getBookName();
        author = book.getAuthor();
        url = book.getUrl();
        chapterSize = book.getChapterSize();
        lastUpdateTime = book.getLastUpdateTime();
        lastChapterName = book.getLastChapterName();
        siteName = book.getSiteName();
        this.lastCheckCount = lastCheckCount;
        currentCheckCount = lastCheckCount;
    }

    public Book getRawBook() {
        Log.e("NetBook", toString());
        return new Book(bookName, author, url, chapterSize, lastUpdateTime, lastChapterName, siteName);
    }

    @Override
    public String toString() {
        return "NetBook{" +
                "bkId=" + bkId +
                ", bookName='" + bookName + '\'' +
                ", author='" + author + '\'' +
                ", url='" + url + '\'' +
                ", chapterSize='" + chapterSize + '\'' +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                ", lastChapterName='" + lastChapterName + '\'' +
                ", siteName='" + siteName + '\'' +
                ", lastCheckCount=" + lastCheckCount +
                ", currentCheckCount=" + currentCheckCount +
                ", time=" + time +
                '}';
    }

    public int getLastCheckCount() {
        return lastCheckCount;
    }

    public void setLastCheckCount(int lastCheckCount) {
        this.lastCheckCount = lastCheckCount;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChapterSize() {
        return chapterSize;
    }

    public void setChapterSize(String chapterSize) {
        this.chapterSize = chapterSize;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getLastChapterName() {
        return lastChapterName;
    }

    public void setLastChapterName(String lastChapterName) {
        this.lastChapterName = lastChapterName;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public int getCurrentCheckCount() {
        return currentCheckCount;
    }

    public void setCurrentCheckCount(int currentCheckCount) {
        this.currentCheckCount = currentCheckCount;
    }

    public int getBkId() {
        return bkId;
    }

    public void setBkId(int bkId) {
        this.bkId = bkId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
