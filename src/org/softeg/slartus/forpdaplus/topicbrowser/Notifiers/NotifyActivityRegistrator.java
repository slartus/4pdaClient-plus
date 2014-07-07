package org.softeg.slartus.forpdaplus.topicbrowser.Notifiers;

import android.content.Intent;

import java.util.ArrayList;

/**
 * Created by slartus on 05.06.2014.
 */
public class NotifyActivityRegistrator {
    private int mRequestCode = 0;

    public interface NotifyClass {
        public void register(NotifyActivityRegistrator registrator);

        public void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    private ArrayList<NotifyClass> mRegClasses = new ArrayList<>();

    public int registerRequestCode() {
        return registerRequestCode(null);
    }

    public int registerRequestCode(NotifyClass c) {
        int ret = mRequestCode++;
        if ((null != c) && (-1 == mRegClasses.indexOf(c))) {
            mRegClasses.add(c);
        }
        return ret;
    }

    public void sendActivityResult(int requestCode, int resultCode, Intent data) {
        int i = 0, l = mRegClasses.size();
        for (; i < l; i++) {
            try {
                mRegClasses.get(i).onActivityResult(requestCode, resultCode, data);
            } catch (Exception exception) {
            }
        }
    }
}
