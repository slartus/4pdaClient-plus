package org.softeg.slartus.forpdaplus.controls.quickpost.items;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostItem;

/**
 * Created by slartus on 23.02.14.
 */
public class SettingsItem extends QuickPostItem {
    @Override
    public String getTitle() {
        return "Настройки";
    }

    @Override
    public String getName() {
        return "settings";
    }



    @Override
    public BaseQuickView createView(Context context) {
        return new SettingsQuickView(context);
    }
}
