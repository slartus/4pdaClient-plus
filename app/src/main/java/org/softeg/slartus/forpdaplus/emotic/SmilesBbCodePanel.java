package org.softeg.slartus.forpdaplus.emotic;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Gallery;

import org.softeg.slartus.forpdaplus.classes.BbCodesBasePanel;
import org.softeg.slartus.forpdaplus.classes.BbImage;
import org.softeg.slartus.forpdaplus.classes.common.ExtBitmap;
import org.softeg.slartus.forpdaplus.common.AppLog;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 17.10.12
 * Time: 9:46
 * To change this template use File | Settings | File Templates.
 */
public class SmilesBbCodePanel extends BbCodesBasePanel {

    public SmilesBbCodePanel(final Context context, Gallery gallery, EditText editText) {
        super(context, gallery, editText, new IGetBitmap() {
            @Override
            public Bitmap getBitmap(Context context, String filePath) throws IOException {
                return ExtBitmap.getBitmapFromAsset(context, filePath);
            }
        });
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                try {
                    final BbImage bbImage = (BbImage) view.getTag();
                    tryInsertText(bbImage.Code);


                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Smiles smiles = Smiles.createSmiles();
                            smiles.setWeights();

                            Smile smile = smiles.findByFileName(bbImage.FileName);
                            smiles.addWeight(smile);
                        }
                    });
                    thread.run();
                } catch (Exception ex) {
                    AppLog.e(mContext, ex);
                }
            }
        });

    }

    @Override
    protected BbImage[] getImages() {
        return Smiles.createSmiles().getFilesList();
    }


    private void tryInsertText(String text) {

        if (TextUtils.isEmpty(text)) return;

        int selectionStart = txtPost.getSelectionStart();
        txtPost.getText().insert(selectionStart, " " + text + " ");
    }
}
