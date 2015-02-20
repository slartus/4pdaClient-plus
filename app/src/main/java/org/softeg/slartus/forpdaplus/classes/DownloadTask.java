package org.softeg.slartus.forpdaplus.classes;

import android.content.Context;
import android.text.TextUtils;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.db.DownloadsTable;
import org.softeg.slartus.forpdacommon.FileUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

/**
 * User: slinkin
 * Date: 12.10.11
 * Time: 11:12
 */
public class DownloadTask {

    public static final int STATE_PENDING = 5;
    public static final int STATE_CONNECTING = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_SUCCESSFULL = 2;
    public static final int STATE_ERROR = 3;
    public static final int STATE_CANCELED = 4;

    private ArrayList<Client.OnProgressPositionChangedListener> m_OnStateListeners = new ArrayList<Client.OnProgressPositionChangedListener>();
    private String m_Url;
    private String outputFile;
    private int m_State = STATE_PENDING;
    private Exception ex;
    private int m_Percents;
    private long downloadedSize;
    private long m_ContentLength;
    private Date m_CreateDate;
    private Date m_StateChangedDate;
    private int m_NotificationId;
    private long m_Range;
    private String m_DownloadingFilePath;

    public DownloadTask(String url, int notificationId) {

        m_Url = url;
        m_NotificationId = notificationId;
        m_CreateDate = new Date();
        m_StateChangedDate = new Date();

        DownloadsTable.insertRow(m_NotificationId, m_Url, m_CreateDate);
    }

    public DownloadTask(String url, int notificationId, Date createDate, long contentLength) {

        m_Url = url;
        m_NotificationId = notificationId;
        m_CreateDate = createDate;
        m_ContentLength = contentLength;

    }

    public int getId() {
        return m_NotificationId;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public int getState() {
        return m_State;
    }

    public String getStateMessage() {
        return getStateMessage(m_State, ex);
    }

    public boolean isActive() {
        return m_State == STATE_PENDING || m_State == STATE_CONNECTING || m_State == STATE_DOWNLOADING;
    }

    public static String getStateMessage(int state, Exception downloadTaskException) {
        switch (state) {
            case STATE_PENDING:
            case STATE_CONNECTING:
                return App.getContext().getString(R.string.Connecting);
            case STATE_DOWNLOADING:
                return App.getContext().getString(R.string.Downloading);
            case STATE_SUCCESSFULL:
                return App.getContext().getString(R.string.DownloadingComplete);
            case STATE_CANCELED:
                return App.getContext().getString(R.string.DownloadingCanceled);
            case STATE_ERROR:
                return App.getContext().getString(R.string.DownloadError) + ": " + (downloadTaskException == null ?
                        App.getContext().getString(R.string.UnknownError) : downloadTaskException.getMessage());
        }
        return App.getContext().getString(R.string.Unknown);
    }

    public void setJustState(int state) {
        this.m_State = state;
    }

    public void setState(int state) {
        this.m_State = state;
        m_StateChangedDate = new Date();


        doStateChanged();
    }

    public void setEx(Exception ex) {
        m_State = STATE_ERROR;
        this.ex = ex;
    }

    public Exception getEx() {
        return ex;
    }

    public void addStateListener(Client.OnProgressPositionChangedListener stateListener) {
        if (stateListener != null) {
            m_OnStateListeners.add(stateListener);
        }
    }

    private Context m_Context;

    public Context getContext() {
        return m_Context;
    }

    public void setContext(Context context) {
        this.m_Context = context;

    }

    private void doStateChanged() {
        for (Client.OnProgressPositionChangedListener stateListener : m_OnStateListeners) {
            stateListener.onProgressChanged(m_Context, this, ex);
        }

    }

    public void setProgressState(long downloadedSize, long contentLength) {
        m_Percents = (int) ((float) downloadedSize / contentLength * 100);
        this.downloadedSize = downloadedSize;
        this.m_ContentLength = contentLength;
        setState(STATE_DOWNLOADING);
    }

    public String getUrl() {
        return m_Url;
    }

    public String getFileName() throws UnsupportedEncodingException {
        try{
            if (TextUtils.isEmpty(outputFile))
                return FileUtils.getFileNameFromUrl(m_Url);

            int index = outputFile.lastIndexOf("/");

            return outputFile.substring(index + 1, outputFile.length());
        }catch (Throwable ignored){
            return m_Url;
        }

    }

    public int getPercents() {
        return m_Percents;
    }

    public void calcPercents() {
        m_Percents = (int) ((float) downloadedSize / m_ContentLength * 100);
    }

    public long getM_ContentLength() {
        return m_ContentLength;
    }

    public void setM_ContentLength(long value) {
        m_ContentLength = value;
    }


    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long value) {
        downloadedSize = value;
    }

    public void cancel() {
        if (m_State == STATE_CONNECTING || m_State == STATE_DOWNLOADING || m_State == STATE_PENDING) {
            m_State = STATE_CANCELED;
            doStateChanged();
        }
    }

    public void setCreateDate(Date value) {
        m_CreateDate = value;
    }

    public Date getCreateDate() {
        return m_CreateDate;
    }

    public Date getStateChangedDate() {
        return m_StateChangedDate;
    }

    public void setStateChangedDate(Date m_StateChangedDate) {
        this.m_StateChangedDate = m_StateChangedDate;
    }

    public static long getRange(String filePath) {
        File file = new File(filePath);
        if (!file.exists())
            return 0;
        return file.length();
    }

    public void setDownloadingFilePath(String downloadingFilePath) {
        m_DownloadingFilePath = downloadingFilePath;
    }

    public String getDownloadingFilePath() {
        return m_DownloadingFilePath;
    }

    public void updateInfo(long fileLength) {
        DownloadsTable.updateRow(m_NotificationId, m_DownloadingFilePath, outputFile, fileLength);
    }
}
