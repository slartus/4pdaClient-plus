package org.softeg.slartus.forpdaplus.video.api.exceptions;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

/**
 * Created by slartus on 06.02.14.
 */
public class IdException extends Exception {
    public IdException(CharSequence apiId) {
        super(App.getContext().getString(R.string.id_exeption)+" " + apiId);
    }
}
