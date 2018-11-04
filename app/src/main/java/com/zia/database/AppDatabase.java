package com.zia.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;
import com.zia.App;
import com.zia.database.bean.*;

/**
 * Created by zia on 2018/5/6.
 */
@Database(entities = {LocalBook.class, NetBook.class, BookMark.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "book_db";

    public abstract LocalBookDao localBookDao();

    public abstract NetBookDao netBookDao();

    public abstract BookMarkDao bookMarkDao();

    private static AppDatabase INSTANCE;

//    private static final Migration m_1_2 = new Migration(1, 2) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("create table IF NOT EXISTS bookMark (markId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, siteName TEXT, bookName TEXT,position INTEGER)");
//        }
//    };

    public static AppDatabase getAppDatabase() {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(App.getContext(), AppDatabase.class, DATABASE_NAME)
                    // allow queries on the main thread.
                    // Don't do this on a real app! See PersistenceBasicSample for an example.
                    .allowMainThreadQueries()
//                    .addMigrations(m_1_2)
                    .fallbackToDestructiveMigration()//版本不一致直接重建数据库=_=
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
