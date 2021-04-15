package org.softeg.slartus.forpdaplus;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public final class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private final HashMap<String, Activity> m_Activities = new HashMap<>();

    public void onActivityCreated(Activity activity, Bundle bundle) {
        m_Activities.put(activity.getLocalClassName(), activity);
    }

    public void onActivityDestroyed(Activity activity) {
        m_Activities.remove(activity.getLocalClassName());
    }

    public void onActivityPaused(Activity activity) {

    }

    public void onActivityResumed(Activity activity) {

    }

    public void onActivitySaveInstanceState(Activity activity,
                                            Bundle outState) {

    }

    public void onActivityStarted(Activity activity) {

    }

    public void onActivityStopped(Activity activity) {

    }

    void finishActivities() {
        for (Map.Entry<String, Activity> entry : m_Activities.entrySet()) {
            try {
                Activity activity = entry.getValue();

                if (activity == null)
                    continue;

                if (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed())
                    continue;

                if (activity.isFinishing())
                    continue;

                entry.getValue().finish();
            } catch (Throwable ex) {
                Log.e("", "finishActivities:" + ex.toString());
            }
        }
    }
}
