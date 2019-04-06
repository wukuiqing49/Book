package com.zia.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import com.zia.easybookmodule.bean.Book;
import com.zia.page.preview.PreviewActivity;

import java.util.Collections;
import java.util.List;

/**
 * Created by zia on 2018/12/14.
 */
public class ShortcutsUtil {

    public static void addBook(Context context, Book book) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            Intent intent = new Intent(context, PreviewActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra("bookName", book.getBookName());
            intent.putExtra("siteName", book.getSiteName());
            ShortcutInfo info = new ShortcutInfo.Builder(context, BookUtil.buildId(book))
                    .setLongLabel(book.getBookName())
                    .setShortLabel(book.getBookName())
                    .setIntent(intent)
                    .build();
            addShortcut(context, info, 1);
        }
    }

    public static void addBook(Context context, Book book, Icon icon) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            Intent intent = new Intent(context, PreviewActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra("bookName", book.getBookName());
            intent.putExtra("siteName", book.getSiteName());
            ShortcutInfo info = new ShortcutInfo.Builder(context, BookUtil.buildId(book))
                    .setLongLabel(book.getBookName())
                    .setShortLabel(book.getBookName())
                    .setIcon(icon)
                    .setIntent(intent)
                    .build();
            addShortcut(context, info);
        }
    }

    public static void removeBook(Context context, Book book) {
        removeShortcut(context, BookUtil.buildId(book));
    }

    /**
     * 删除shortcut
     *
     * @param context
     * @param id
     */
    public static void removeShortcut(Context context, String id) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            shortcutManager.removeDynamicShortcuts(Collections.singletonList(id));
        }
    }

    public static void setShortcuts(Context context, List<ShortcutInfo> infos) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            shortcutManager.setDynamicShortcuts(infos);
        }
    }

    /**
     * 添加shortcut
     *
     * @param context
     * @param shortcutInfo
     */
    public static void addShortcut(Context context, ShortcutInfo shortcutInfo) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            autoRemoveShortcut(shortcutManager);
            List<ShortcutInfo> shortcutInfos = shortcutManager.getDynamicShortcuts();
            if (shortcutInfos.size() >= 5) {
                shortcutInfos.remove(shortcutInfos.size() - 1);
            }
            shortcutManager.addDynamicShortcuts(Collections.singletonList(shortcutInfo));
        }
    }

    /**
     * 添加shortcut到指定position
     *
     * @param context
     * @param shortcutInfo
     * @param index
     */
    public static void addShortcut(Context context, ShortcutInfo shortcutInfo, int index) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            autoRemoveShortcut(shortcutManager);
            List<ShortcutInfo> dynamicShortcuts = shortcutManager.getDynamicShortcuts();
            for (ShortcutInfo dynamicShortcut : dynamicShortcuts) {
                if (dynamicShortcut.getId().equals(shortcutInfo.getId())) {
                    return;
                }
            }
            dynamicShortcuts.add(index, shortcutInfo);
            setShortcuts(context, dynamicShortcuts);
        }
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private static void autoRemoveShortcut(ShortcutManager shortcutManager) {
        List<ShortcutInfo> shortcutInfos = shortcutManager.getDynamicShortcuts();
        if (shortcutInfos.size() >= 5) {
            shortcutManager.removeDynamicShortcuts(Collections.singletonList(shortcutInfos.get(shortcutInfos.size() - 1).getId()));
        }
    }
}
