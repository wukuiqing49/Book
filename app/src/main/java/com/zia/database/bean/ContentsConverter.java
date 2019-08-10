package com.zia.database.bean;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class ContentsConverter {
    @TypeConverter
    public static List<String> revert(String contents) {
        return new Gson().fromJson(contents, new TypeToken<List<String>>() {
        }.getType());
    }

    @TypeConverter
    public static String converter(List<String> contents) {
        return new Gson().toJson(contents);
    }
}