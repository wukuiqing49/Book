package com.zia.bookdownloader.lib.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created By zia on 2018/10/30.
 */
public class Catalog implements Serializable, Parcelable {
    private String chapterName;
    private String url;
    private int index;

    public Catalog(String chapterName, String url) {
        this.chapterName = chapterName;
        this.url = url;
    }

    public Catalog(){ }

    @Override
    public String toString() {
        return "Catalog{" +
                "chapterName='" + chapterName + '\'' +
                ", url='" + url + '\'' +
                ", index=" + index +
                '}';
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.chapterName);
        dest.writeString(this.url);
        dest.writeInt(this.index);
    }

    protected Catalog(Parcel in) {
        this.chapterName = in.readString();
        this.url = in.readString();
        this.index = in.readInt();
    }

    public static final Parcelable.Creator<Catalog> CREATOR = new Parcelable.Creator<Catalog>() {
        @Override
        public Catalog createFromParcel(Parcel source) {
            return new Catalog(source);
        }

        @Override
        public Catalog[] newArray(int size) {
            return new Catalog[size];
        }
    };
}
