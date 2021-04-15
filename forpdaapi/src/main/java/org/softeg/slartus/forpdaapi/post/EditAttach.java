package org.softeg.slartus.forpdaapi.post;/*
 * Created by slinkin on 15.07.2014.
 */

import java.io.Serializable;

public class EditAttach implements Serializable {
    private final String mId;
    private final String mName;

    public EditAttach(String id, String name) {
        mId = id;
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }
}
