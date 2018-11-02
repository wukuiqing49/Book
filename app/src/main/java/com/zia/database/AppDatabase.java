package com.zia.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import com.zia.App;
import com.zia.database.bean.LocalBook;
import com.zia.database.bean.LocalBookDao;
import com.zia.database.bean.NetBook;
import com.zia.database.bean.NetBookDao;

/**
 * Created by zia on 2018/5/6.
 */
@Database(entities = {LocalBook.class, NetBook.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "book_db";

    public abstract LocalBookDao localBookDao();

    public abstract NetBookDao netBookDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getAppDatabase() {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(App.getContext(), AppDatabase.class, DATABASE_NAME)
                    // allow queries on the main thread.
                    // Don't do this on a real app! See PersistenceBasicSample for an example.
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
