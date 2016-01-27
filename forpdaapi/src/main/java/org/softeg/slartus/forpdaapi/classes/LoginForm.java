package org.softeg.slartus.forpdaapi.classes;

/*
 * Created by slinkin on 15.06.2015.
 */
public class LoginForm {
    private Throwable error;
    private String capPath;
    private String capTime;
    private String capSigig;
    private String session;

    public String getCapPath() {
        return capPath;
    }

    public void setCapPath(String capPath) {
        this.capPath = capPath;
    }

    public String getCapTime() {
        return capTime;
    }

    public void setCapTime(String capTime) {
        this.capTime = capTime;
    }

    public String getCapSig() {
        return capSigig;
    }

    public void setCapSig(String capSigig) {
        this.capSigig = capSigig;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getSession() {
        return session;
    }
}
