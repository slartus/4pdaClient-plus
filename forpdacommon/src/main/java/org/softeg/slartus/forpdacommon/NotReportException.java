package org.softeg.slartus.forpdacommon;

import java.io.IOException;

/**
 * User: slinkin
 * Date: 21.10.11
 * Time: 8:03
 */
public class NotReportException extends IOException {
    public NotReportException(String message) {
        super(message);
    }

    public NotReportException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }
}
