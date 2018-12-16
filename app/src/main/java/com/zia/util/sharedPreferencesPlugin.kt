package com.zia.util

import android.content.Context
import android.content.SharedPreferences
import com.zia.App

val Context.defaultSharedPreferences get() = sharedPreferences("share_data")

fun Context.sharedPreferences(name: String): SharedPreferences = getSharedPreferences(name, Context.MODE_PRIVATE)
fun SharedPreferences.editor(editorBuilder: SharedPreferences.Editor.() -> Unit) = edit().apply(editorBuilder).apply()
fun defaultSharedPreferences(): SharedPreferences = App.getContext().defaultSharedPreferences