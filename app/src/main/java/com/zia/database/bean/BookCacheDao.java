package com.zia.database.bean;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Created by zia on 2018/11/30.
 */
@Dao
public interface BookCacheDao {

    @Query("select * from bookCache where bookName = :bookName and siteName = :siteName order by `index` asc")
    List<BookCache> getBookCaches(String bookName, String siteName);

    @Query("select * from bookCache where bookName = :bookName and siteName = :siteName and `index` = :index")
    BookCache getBookCache(String bookName, String siteName, int index);

    @Query("select count(*) from bookCache where bookName = :bookName and siteName = :siteName")
    int getBookCacheSize(String bookName, String siteName);

    @Query("select chapterName from bookCache where bookName = :bookName and siteName = :siteName order by `index` asc")
    List<String> getChapterNames(String bookName, String siteName);

    @Query("select chapterName from bookCache where bookName = :bookName and siteName = :siteName and `index` = :index")
    String getChapterName(int index, String bookName, String siteName);

    @Query("select chapterName from bookCache where bookName = :bookName and siteName = :siteName order by `index` asc")
    DataSource.Factory<Integer, String> getChapterNamesFactory(String bookName, String siteName);

    @Query("select * from bookCache where bookName = :bookName and siteName = :siteName order by `index` asc")
    DataSource.Factory<Integer, BookCache> getCachesFactory(String bookName, String siteName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BookCache bookCache);
}
