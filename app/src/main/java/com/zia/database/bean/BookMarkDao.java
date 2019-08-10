package com.zia.database.bean;

import androidx.room.*;

/**
 * Created by zia on 2018/11/4.
 */
@Dao
public interface BookMarkDao {

    @Query("select * from bookMark where bookName = :bkname and siteName = :stname")
    BookMark getBookMark(String bkname, String stname);

    @Query("select position from bookMark where bookName = :bkName and siteName = :siteName")
    int getPosition(String bkName, String siteName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BookMark bookMark);

    @Delete
    void delete(BookMark bookMark);

    @Query("delete from bookMark where bookName = :bkname and siteName = :stname")
    void delete(String bkname, String stname);

    @Update
    void update(BookMark bookMark);
}
