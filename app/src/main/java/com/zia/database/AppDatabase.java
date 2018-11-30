package com.zia.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import com.zia.App;
import com.zia.database.bean.*;

/**
 * Created by zia on 2018/5/6.
 */
@Database(entities = {LocalBook.class, NetBook.class, BookMark.class, BookCache.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "book_db";

    public abstract LocalBookDao localBookDao();

    public abstract NetBookDao netBookDao();

    public abstract BookMarkDao bookMarkDao();

    public abstract BookCacheDao bookCacheDao();

    private static AppDatabase INSTANCE;

//    private static final Migration m_1_2 = new Migration(1, 2) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("create table IF NOT EXISTS bookMark (markId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, siteName TEXT, bookName TEXT,position INTEGER)");
//        }
//    };

//    private static final Migration m_2_3 = new Migration(2,3) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("alter table netBook add column time timestamp not null default CURRENT_TIMESTAMP");
//        }
//    };

    public static AppDatabase getAppDatabase() {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(App.getContext(), AppDatabase.class, DATABASE_NAME)
                    // allow queries on the main thread.
                    // Don't do this on a real app! See PersistenceBasicSample for an example.
                    .allowMainThreadQueries()
//                    .addMigrations(m_2_3)
                    .fallbackToDestructiveMigration()//版本不一致直接重建数据库=_=
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
