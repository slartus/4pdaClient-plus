package org.softeg.slartus.forpdaapi.post;/*
 * Created by slinkin on 15.07.2014.
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditPost {
    private String error;
    private String body;
    private Map<String, String> params=new HashMap<>();
    private String attachPostKey;
    private String editPostReason;
    private Interview interview;
    private List<EditAttach> attaches;

    public String getAttachPostKey() {
        return attachPostKey;
    }

    public void setAttachPostKey(String attachPostKey) {
        this.attachPostKey = attachPostKey;
    }

    public String getEditPostReason() {
        return editPostReason;
    }

    public void setEditPostReason(String editPostReason) {
        this.editPostReason = editPostReason;
    }

    public Interview getInterview() {
        return interview;
    }

    public void setInterview(Interview interview) {
        this.interview = interview;
    }

    public List<EditAttach> getAttaches() {
        return attaches;
    }

    public void setAttaches(List<EditAttach> attaches) {
        this.attaches = attaches;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
