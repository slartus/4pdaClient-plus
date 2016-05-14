package org.softeg.slartus.forpdaplus.video.api.exceptions;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

/**
 * Created by slartus on 07.02.14.
 */
public class ListIdException extends Exception {
    public ListIdException(CharSequence apiId) {
        super(App.getContext().getString(R.string.list_id_exception)+" " + apiId);
    }
}
