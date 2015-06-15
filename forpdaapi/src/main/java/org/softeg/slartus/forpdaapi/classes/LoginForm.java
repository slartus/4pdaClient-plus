package org.softeg.slartus.forpdaapi.classes;

/*
 * Created by slinkin on 15.06.2015.
 */
public class LoginForm {
    private Throwable error;
    private String capPath;
    private String capD;
    private String capS;
    private String session;

    public String getCapPath() {
        return capPath;
    }

    public void setCapPath(String capPath) {
        this.capPath = capPath;
    }

    public String getCapD() {
        return capD;
    }

    public void setCapD(String capD) {
        this.capD = capD;
    }

    public String getCapS() {
        return capS;
    }

    public void setCapS(String capS) {
        this.capS = capS;
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
