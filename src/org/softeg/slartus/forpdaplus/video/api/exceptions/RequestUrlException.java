package org.softeg.slartus.forpdaplus.video.api.exceptions;

/**
 * Created by slartus on 06.02.14.
 */
public class RequestUrlException extends Exception {
    public RequestUrlException(CharSequence apiId) {
        super("Не могу получить ссылку на видео-поток " + apiId);
    }
}
