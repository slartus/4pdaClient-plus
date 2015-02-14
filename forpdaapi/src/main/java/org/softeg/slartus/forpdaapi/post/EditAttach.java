package org.softeg.slartus.forpdaapi.post;/*
 * Created by slinkin on 15.07.2014.
 */

import java.io.Serializable;

public class EditAttach implements Serializable{
    private String mId;
    private String mName;
    private String mSize;
    private String mType;

    public EditAttach(String id, String name, String size, String type) {
        mId = id;
        mName = name;
        mSize = size;
        mType = type;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }
}
