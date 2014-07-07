package org.softeg.slartus.forpdaplus.video.api.exceptions;

/**
 * Created by slartus on 07.02.14.
 */
public class ListIdException extends Exception {
    public ListIdException(CharSequence apiId) {
        super("Не могу получить идентификатор списка " + apiId);
    }
}
