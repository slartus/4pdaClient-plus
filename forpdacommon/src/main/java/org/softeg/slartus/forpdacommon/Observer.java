package org.softeg.slartus.forpdacommon;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by slinkin on 19.11.2014.
 */
public class Observer<I> {
    private final ArrayList<I> strongListeners = new ArrayList<I>();
    private final ArrayList<WeakReference<I>> weakListeners = new ArrayList<WeakReference<I>>();

    public void addStrongListener(I listener) {
        strongListeners.add(listener);
    }

    public void addWeakListener(I listener) {
        weakListeners.add(new WeakReference<I>(listener));
    }

    public void removeListener(I listener) {
        strongListeners.remove(listener);
        for (int i = 0; i < weakListeners.size(); ++i) {
            WeakReference<I> ref = weakListeners.get(i);
            if (ref.get() == null || ref.get() == listener) {
                weakListeners.remove(i--);
            }
        }
    }

    public List<I> getListeners() {
        ArrayList<I> activeListeners = new ArrayList<I>();
        activeListeners.addAll(strongListeners);
        for (int i = 0; i < weakListeners.size(); ++i) {
            WeakReference<I> ref = weakListeners.get(i);
            I listener = ref.get();
            if (listener == null) {
                weakListeners.remove(i--);
                continue;
            }

            activeListeners.add(listener);
        }
        return activeListeners;
    }
}
