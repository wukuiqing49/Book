package com.zia.database.bean;

import android.arch.persistence.room.*;

import java.util.List;

/**
 * Created by zia on 2018/11/2.
 */
@Dao
public interface NetBookDao {
    @Query("select * from netBook")
    List<NetBook> getNetBooks();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NetBook netBook);

    @Delete
    void delete(NetBook netBook);

    @Query("delete from netBook where bookName = :bkname and siteName = :stName")
    void delete(String bkname, String stName);

    @Update
    void update(NetBook netBook);
}
