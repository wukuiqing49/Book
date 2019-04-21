package com.zia.util;

import android.support.annotation.Nullable;
import android.widget.Toast;
import com.zia.App;
import com.zia.toastex.ToastEx;


public class ToastUtil {

    private static Toast toast;

    public static void onSuccess(@Nullable String content) {
        if (content == null) return;
        if (toast != null) toast.cancel();
        toast = ToastEx.success(App.getContext(), content, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void onError(@Nullable String content) {
        if (content == null) return;
        if (toast != null) toast.cancel();
        toast = ToastEx.error(App.getContext(), content, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void onInfo(@Nullable String content) {
        if (content == null) return;
        if (toast != null) toast.cancel();
        toast = ToastEx.info(App.getContext(), content, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void onNormal(@Nullable String content) {
        if (content == null) return;
        if (toast != null) toast.cancel();
        toast = ToastEx.normal(App.getContext(), content, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void onWarning(@Nullable String content) {
        if (content == null) return;
        if (toast != null) toast.cancel();
        toast = ToastEx.warning(App.getContext(), content, Toast.LENGTH_SHORT);
        toast.show();
    }
}