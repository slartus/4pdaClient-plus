package org.softeg.slartus.forpdacommon

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.DrawableRes

fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.resources.getDrawable(id, theme);
    } else {
        this.resources.getDrawable(id);
    }