package org.softeg.slartus.forpdacommon;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

public class SerializableCookie implements Cookie, Externalizable {

    private static final int NAME = 0x01;
    private static final int VALUE = 0x02;
    private static final int COMMENT = 0x04;
    private static final int COMMENT_URL = 0x08;
    private static final int EXPIRY_DATE = 0x10;
    private static final int DOMAIN = 0x20;
    private static final int PATH = 0x40;
    private static final int PORTS = 0x80;

    private transient int nullMask = 0;
    private transient Cookie cookie;

    public SerializableCookie() {
        super();
    }

    public SerializableCookie(final Cookie cookie) {
        super();

        this.cookie = cookie;
    }

    public String getName() {
        return cookie.getName();
    }

    public String getValue() {
        return cookie.getValue();
    }

    public String getComment() {
        return cookie.getComment();
    }

    public String getCommentURL() {
        return cookie.getCommentURL();
    }

    public Date getExpiryDate() {
        return cookie.getExpiryDate();
    }

    public boolean isPersistent() {
        return cookie.isPersistent();
    }

    public String getDomain() {
        return cookie.getDomain();
    }

    public String getPath() {
        return cookie.getPath();
    }

    public int[] getPorts() {
        return cookie.getPorts();
    }

    public boolean isSecure() {
        return cookie.isSecure();
    }

    public int getVersion() {
        return cookie.getVersion();
    }

    public boolean isExpired(final Date date) {
        return cookie.isExpired(date);
    }

    public void writeExternal(final ObjectOutput out) throws IOException {
        nullMask |= (getName() == null) ? NAME : 0;
        nullMask |= (getValue() == null) ? VALUE : 0;
        nullMask |= (getComment() == null) ? COMMENT : 0;
        nullMask |= (getCommentURL() == null) ? COMMENT_URL : 0;
        nullMask |= (getExpiryDate() == null) ? EXPIRY_DATE : 0;
        nullMask |= (getDomain() == null) ? DOMAIN : 0;
        nullMask |= (getPath() == null) ? PATH : 0;
        nullMask |= (getPorts() == null) ? PORTS : 0;

        out.writeInt(nullMask);

        if ((nullMask & NAME) == 0) {
            out.writeUTF(getName());
        }

        if ((nullMask & VALUE) == 0) {
            out.writeUTF(getValue());
        }

        if ((nullMask & COMMENT) == 0) {
            out.writeUTF(getComment());
        }

        if ((nullMask & COMMENT_URL) == 0) {
            out.writeUTF(getCommentURL());
        }

        if ((nullMask & EXPIRY_DATE) == 0) {
            out.writeLong(getExpiryDate().getTime());
        }

        out.writeBoolean(isPersistent());

        if ((nullMask & DOMAIN) == 0) {
            out.writeUTF(getDomain());
        }

        if ((nullMask & PATH) == 0) {
            out.writeUTF(getPath());
        }

        if ((nullMask & PORTS) == 0) {
            out.writeInt(getPorts().length);

            for (int p : getPorts()) {
                out.writeInt(p);
            }
        }

        out.writeBoolean(isSecure());
        out.writeInt(getVersion());
    }


    public void readExternal(final ObjectInput in) throws IOException,
            ClassNotFoundException {
        nullMask = in.readInt();

        String name = null;
        String value = null;
        String comment = null;
        String commentURL = null;
        Date expiryDate = null;
        boolean isPersistent = false;
        String domain = null;
        String path = null;
        int[] ports = null;
        boolean isSecure = false;
        int version = 0;

        if ((nullMask & NAME) == 0) {
            name = in.readUTF();
        }

        if ((nullMask & VALUE) == 0) {
            value = in.readUTF();
        }

        if ((nullMask & COMMENT) == 0) {
            comment = in.readUTF();
        }

        if ((nullMask & COMMENT_URL) == 0) {
            commentURL = in.readUTF();
        }

        if ((nullMask & EXPIRY_DATE) == 0) {
            expiryDate = new Date(in.readLong());
        }

        isPersistent = in.readBoolean();

        if ((nullMask & DOMAIN) == 0) {
            domain = in.readUTF();
        }

        if ((nullMask & PATH) == 0) {
            path = in.readUTF();
        }

        if ((nullMask & PORTS) == 0) {
            final int len = in.readInt();

            ports = new int[len];

            for (int i = 0; i < len; i++) {
                ports[i] = in.readInt();
            }
        }

        isSecure = in.readBoolean();
        version = in.readInt();

        final BasicClientCookie bc = new BasicClientCookie(name, value);

        bc.setComment(comment);
        bc.setDomain(domain);
        bc.setExpiryDate(expiryDate);
        bc.setPath(path);
        bc.setSecure(isSecure);
        bc.setVersion(version);

        this.cookie = bc;
    }

    @Override
    public String toString() {
        if (cookie == null) {
            return "null";
        } else {
            return cookie.toString();
        }
    }


}
