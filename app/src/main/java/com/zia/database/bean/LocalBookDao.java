package com.zia.database.bean;

import android.arch.persistence.room.*;

import java.util.List;

/**
 * Created by zia on 2018/11/2.
 */
@Dao
public interface LocalBookDao {

    @Query("select * from localBook")
    List<LocalBook> getLocalBooks();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LocalBook localBook);

    @Delete
    void delete(LocalBook localBook);

    @Query("delete from localBook where bookName = :bkname and siteName = :stname")
    void delete(String bkname, String stname);

    @Update
    void update(LocalBook localBook);
}
