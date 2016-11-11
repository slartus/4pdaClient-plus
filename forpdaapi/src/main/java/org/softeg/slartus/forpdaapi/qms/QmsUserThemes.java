package org.softeg.slartus.forpdaapi.qms;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 06.02.13
 * Time: 9:59
 * To change this template use File | Settings | File Templates.
 */
public class QmsUserThemes extends ArrayList<QmsUserTheme> {
    private Throwable error;
    public String Nick;

    public int getSelectedCount() {
        int count = 0;
        for (QmsUserTheme theme : this) {
            if (theme.isSelected())
                count++;
        }
        return count;
    }

    public int getHasNewCount() {
        int count = 0;
        for (QmsUserTheme theme : this) {
            if (!TextUtils.isEmpty(theme.NewCount))
                count++;
        }
        return count;
    }

    public QmsUserTheme getFirstHasNew() {
        int count = 0;
        for (QmsUserTheme theme : this) {
            if (!TextUtils.isEmpty(theme.NewCount))
                return theme;
        }
        return null;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
