package org.softeg.slartus.forpdaplus.video.api.exceptions;

/**
 * Created by slartus on 06.02.14.
 */
public class IdException extends Exception {
    public IdException(CharSequence apiId) {
        super("Не могу получить идентификатор " + apiId);
    }
}
