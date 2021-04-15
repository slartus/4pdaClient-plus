package org.softeg.slartus.forpdaapi.post;/*
 * Created by slinkin on 15.07.2014.
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EditPost implements Serializable {
    private String error;
    private String body;
    private final EditPostParams params = new EditPostParams();
    private final List<EditAttach> attaches = new ArrayList<>();
    private String postEditReason;
    private boolean enableEmo;
    private boolean enableSign;

    public List<EditAttach> getAttaches() {
        return attaches;
    }

    public EditPostParams getParams() {
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

    public String getForumId() {
        if (params.containsKey("f"))
            return params.get("f");
        return null;
    }

    public String getTopicId() {
        if (params.containsKey("t"))
            return params.get("t");
        return null;
    }

    public String getId() {
        if (params.containsKey("p"))
            return params.get("p");
        return null;
    }

    public String getAuthKey() {
        if (params.containsKey("auth_key"))
            return params.get("auth_key");
        return null;
    }

    public String getPostEditReason() {
        return postEditReason;
    }

    public void setPostEditReason(String postEditReason) {
        this.postEditReason = postEditReason;
    }

    public void setEnableEmo(boolean enableEmo) {
        this.enableEmo = enableEmo;
    }

    public boolean isEnableEmo() {
        return enableEmo;
    }

    public void setEnableSign(boolean enableSign) {
        this.enableSign = enableSign;
    }

    public boolean isEnableSign() {
        return enableSign;
    }

    public void setId(String id) {
        params.put("p", id);
    }

    public void setForumId(String forumId) {
        params.put("f", forumId);
    }

    public void setTopicId(String topicId) {
        params.put("t", topicId);
    }

    public void setAuthKey(String authKey) {
        params.put("auth_key", authKey);
    }

    private String getFileList() {
        if (params.containsKey("file-list"))
            return params.get("file-list");
        return "";
    }

    public void addAttach(EditAttach editAttach) {
        if (editAttach == null) return;
        attaches.add(editAttach);
        String fileList = getFileList();
        if (!"".equals(fileList))
            fileList += ",";
        fileList += editAttach.getId();
        params.put("file-list", fileList);
    }

    public void deleteAttach(String attachId) {
        for (EditAttach editAttach : attaches) {
            if (attachId.equals(editAttach.getId())) {
                attaches.remove(editAttach);
                break;
            }
        }
        StringBuilder fileList = new StringBuilder();
        for (EditAttach editAttach : attaches) {
            if (fileList.length() > 0)
                fileList.append(",");
            fileList.append(editAttach.getId());
        }
        params.put("file-list", fileList.toString());
    }
}
