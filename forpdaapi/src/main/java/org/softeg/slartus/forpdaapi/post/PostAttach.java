package org.softeg.slartus.forpdaapi.post;/*
 * Created by slinkin on 05.05.2014.
 */

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.Functions;
import org.softeg.slartus.hosthelper.HostHelper;

import java.util.Date;

public class PostAttach implements IListItem {
    private CharSequence id;

    private CharSequence fileType;

    private CharSequence url;

    private CharSequence name;

    private Date additionDate;

    private float fileSize;

    private CharSequence postId;


    public PostAttach() {
    }

    public CharSequence getId() {
        return id;
    }

    @Override
    public CharSequence getTopLeft() {
        return null;
    }

    @Override
    public CharSequence getTopRight() {
        return FileUtils.getFileSizeString(fileSize);
    }

    @Override
    public CharSequence getMain() {
        return name;
    }

    @Override
    public CharSequence getSubMain() {
        if (additionDate == null)
            return null;
        return "Добавлен: " + Functions.getForumDateTime(additionDate);
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public void setState(int state) {

    }

    @Override
    public CharSequence getSortOrder() {
        return id;
    }

    @Override
    public boolean isInProgress() {
        return false;
    }

    public void setId(CharSequence id) {
        this.id = id;
    }

    public CharSequence getFileType() {
        return fileType;
    }

    public void setFileType(CharSequence fileType) {
        this.fileType = fileType;
    }

    public CharSequence getUrl() {
        return url;
    }

    public void setUrl(CharSequence url) {
        this.url = url;
    }

    public CharSequence getName() {
        return name;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public Date getAdditionDate() {
        return additionDate;
    }

    public void setAdditionDate(Date additionDate) {
        this.additionDate = additionDate;
    }

    public float getFileSize() {
        return fileSize;
    }

    public void setFileSize(float fileSize) {
        this.fileSize = fileSize;
    }

    public CharSequence getPostId() {
        return postId;
    }

    public void setPostId(CharSequence postId) {
        this.postId = postId;
    }


    public String getPostUrl() {
        return "https://"+ HostHelper.getHost() +"/forum/index.php?act=findpost&pid=" + postId;
    }


}
