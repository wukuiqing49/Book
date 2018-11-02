package com.zia.util;

import android.util.Pair;
import android.view.View;

import java.util.List;

/**
 * Created by zia on 2018/6/13.
 */
public class Java2Kotlin {
    public static Pair<View, String>[] getPairs(List<Pair<View, String>> list) {
        Pair<View, String> pair[] = new Pair[list.size()];
        for (int i = 0; i < list.size(); i++) {
            pair[i] = list.get(i);
        }
        return pair;
    }
}
