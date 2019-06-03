package com.zia.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zia on 2019-06-03.
 */
public class MergeUtil {
    /**
     * 合并两个集合，去掉重复的
     */
    public static <T> List<T> mergeListNoRepeat(List<T> list1, List<T> list2, WrapId<T> wrapId) {
        List<T> result = new ArrayList<>(list1.size() + list2.size());

        //保持list1数量最小，尽量减小set的数量
        List<T> lessList = list1;
        List<T> moreList = list2;
        if (list1.size() > list2.size()) {
            lessList = list2;
            moreList = list1;
        }

        Set<String> set = new HashSet<>(lessList.size());

        for (T t : lessList) {
            set.add(wrapId.getId(t));
            result.add(t);
        }

        for (T t : moreList) {
            if (!set.contains(wrapId.getId(t))){
                result.add(t);
            }
        }

        return result;
    }

    /**
     * 合并两个集合，如果有重复的，更新新值
     */
    public static <T> List<T> mergeListByReplace(List<T> oldList, List<T> newList, WrapId<T> wrapId) {
        List<T> result = new ArrayList<>(oldList.size() + newList.size());

        Set<String> set = new HashSet<>(newList.size());

        for (T t : newList) {
            set.add(wrapId.getId(t));
            result.add(t);
        }

        for (T t : oldList) {
            if (!set.contains(wrapId.getId(t))){
                result.add(t);
            }
        }

        return result;
    }

    public interface WrapId<T> {
        String getId(T t);
    }
}
