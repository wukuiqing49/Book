package com.zia.util;

import android.support.annotation.Nullable;
import android.widget.Toast;
import com.zia.App;
import com.zia.toastex.ToastEx;

import java.lang.ref.WeakReference;


public class ToastUtil {

    private static WeakReference<Toast> toast;

    public static void onSuccess(@Nullable String content) {
        if (content == null) return;
        if (toast != null && toast.get() != null) toast.get().cancel();
        toast = new WeakReference<>(ToastEx.success(App.getContext(), content, Toast.LENGTH_SHORT));
        toast.get().show();
    }

    public static void onError(@Nullable String content) {
        if (content == null) return;
        if (toast != null && toast.get() != null) toast.get().cancel();
        toast = new WeakReference<>(ToastEx.error(App.getContext(), content, Toast.LENGTH_SHORT));
        toast.get().show();
    }

    public static void onInfo(@Nullable String content) {
        if (content == null) return;
        if (toast != null && toast.get() != null) toast.get().cancel();
        toast = new WeakReference<>(ToastEx.info(App.getContext(), content, Toast.LENGTH_SHORT));
        toast.get().show();
    }

    public static void onNormal(@Nullable String content) {
        if (content == null) return;
        if (toast != null && toast.get() != null) toast.get().cancel();
        toast = new WeakReference<>(ToastEx.normal(App.getContext(), content, Toast.LENGTH_SHORT));
        toast.get().show();
    }

    public static void onWarning(@Nullable String content) {
        if (content == null) return;
        if (toast != null && toast.get() != null) toast.get().cancel();
        toast = new WeakReference<>(ToastEx.warning(App.getContext(), content, Toast.LENGTH_SHORT));
        toast.get().show();
    }
}