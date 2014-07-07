package org.softeg.slartus.forpdaplus;/*
 * Created by slinkin on 29.04.2014.
 */


import android.os.Bundle;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {

    public Bundle getArgs(){
        return args;
    }

    protected Bundle args = new Bundle();
    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            args = getArguments();
        }
        if (savedInstanceState != null) {
            args = savedInstanceState;
        }
    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        if (args != null)
            outState.putAll(args);
    }

}
