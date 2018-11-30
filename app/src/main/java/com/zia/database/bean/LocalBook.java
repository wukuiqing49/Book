package com.zia.database.bean;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import com.zia.easybookmodule.bean.Book;

import java.io.Serializable;

/**
 * Created by zia on 2018/11/2.
 */
@Entity(tableName = "localBook")
public class LocalBook implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int bkId;

    @ColumnInfo(name = "bookName")
    private String bookName;

    @ColumnInfo(name = "filePath")
    private String filePath;

    @ColumnInfo(name = "author")
    private String author = "未知";

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "chapterSize")
    private String imageUrl = "";

    @ColumnInfo(name = "lastUpdateTime")
    private String lastUpdateTime = "未知";

    @ColumnInfo(name = "lastChapterName")
    private String lastChapterName = "未知";

    @ColumnInfo(name = "siteName")
    private String siteName = "未知";

    public LocalBook() {
    }

    public LocalBook(String filePath, Book book) {
        this.filePath = filePath;
        bookName = book.getBookName();
        author = book.getAuthor();
        url = book.getUrl();
        imageUrl = book.getImageUrl();
        lastUpdateTime = book.getLastUpdateTime();
        lastChapterName = book.getLastChapterName();
        siteName = book.getSiteName();
    }

    @Override
    public String toString() {
        return "LocalBook{" +
                "filePath='" + filePath + '\'' +
                ", bookName='" + bookName + '\'' +
                ", author='" + author + '\'' +
                ", url='" + url + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                ", lastChapterName='" + lastChapterName + '\'' +
                ", site='" + siteName + '\'' +
                '}';
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public String getSite() {
        return siteName;
    }

    public void setSite(String site) {
        this.siteName = site;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public int getBkId() {
        return bkId;
    }

    public void setBkId(int bkId) {
        this.bkId = bkId;
    }
}
