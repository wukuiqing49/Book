package com.zia.page.search;

/**
 * Created by zia on 2019/4/19.
 */
public class LabelBean {
    private String bookName;
    private int rankNum = 4;

    public LabelBean(String bookName, int rankNum) {
        this.bookName = bookName;
        this.rankNum = rankNum;
    }

    public LabelBean(String bookName) {
        this.bookName = bookName;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public int getRankNum() {
        return rankNum;
    }

    public void setRankNum(int rankNum) {
        this.rankNum = rankNum;
    }
}
