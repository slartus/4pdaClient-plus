package org.softeg.slartus.forpdaplus.video.api.exceptions;

/**
 * Created by slinkin on 07.02.14.
 */
public class ApiException extends Exception {
    public ApiException(CharSequence apiId, CharSequence message) {
        super(apiId + ": " + message);
    }
}
