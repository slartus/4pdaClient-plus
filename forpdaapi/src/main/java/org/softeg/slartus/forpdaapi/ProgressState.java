package org.softeg.slartus.forpdaapi;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 28.02.13
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
public abstract class ProgressState {
    public abstract void update(String message, int percents);

    public boolean isCanceled() {
        return m_Canceled;
    }

    private Boolean m_Canceled = false;

    public void cancel() {
        m_Canceled = true;
    }
}
