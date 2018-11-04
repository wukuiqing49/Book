package com.zia.database.bean;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by zia on 2018/11/4.
 * 书签 阅读记录
 */
@Entity(tableName = "bookMark")
public class BookMark {

    @PrimaryKey(autoGenerate = true)
    private int markId;

    @ColumnInfo(name = "siteName")
    private String siteName;

    @ColumnInfo(name = "bookName")
    private String bookName;

    @ColumnInfo(name = "position")
    private int position;

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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getMarkId() {
        return markId;
    }

    public void setMarkId(int markId) {
        this.markId = markId;
    }
}
