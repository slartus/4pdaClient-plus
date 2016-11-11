package org.softeg.slartus.forpdaplus.classes;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by slinkin on 20.06.13.
 */
public class CountingMultipartEntity extends MultipartEntity {

    private final ProgressState listener;

    public CountingMultipartEntity(final ProgressState listener) {
        super();
        this.listener = listener;

    }

    public CountingMultipartEntity(final HttpMultipartMode mode, final ProgressState listener) {
        super(mode);
        this.listener = listener;
    }

    public CountingMultipartEntity(HttpMultipartMode mode, final String boundary,
                                   final Charset charset, final ProgressState listener) {
        super(mode, boundary, charset);
        this.listener = listener;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        super.writeTo(new CountingOutputStream(outstream, this.listener, getContentLength()));
    }

    private int m_RepeatsCount = 2;

    public boolean isRepeatable() {

        if (m_RepeatsCount-- > 0)
            return true;
        return false;
    }


    public static class CountingOutputStream extends FilterOutputStream {

        private final ProgressState listener;
        private long transferred;
        private long m_Length;

        public CountingOutputStream(final OutputStream out,
                                    final ProgressState listener, long length) {
            super(out);
            this.listener = listener;
            this.transferred = 0;
            m_Length = length;
        }


        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            if (listener.isCanceled())
                throw new NotReportException(App.getContext().getString(R.string.loading_canceled_by_user));
            this.transferred += len;

            listener.update(App.getContext().getString(R.string.loading), (int) (transferred * 100.0 / m_Length));
        }

        public void write(int b) throws IOException {
            out.write(b);
            if (listener.isCanceled())
                throw new NotReportException(App.getContext().getString(R.string.loading_canceled_by_user));
            this.transferred++;
            listener.update(App.getContext().getString(R.string.loading), (int) (transferred / m_Length * 100));
        }

        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
            // System.out.println("Written " + b.length + " bytes");
        }


    }
}
