package org.softeg.slartus.forpdaplus.classes;

import android.R;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.AppTheme;
import org.softeg.slartus.forpdaplus.common.AppLog;

import java.io.IOException;
import java.util.ArrayList;

/**
 * User: slinkin
 * Date: 16.03.12
 * Time: 9:46
 */
public class BbCodesPanel extends BbCodesBasePanel {

    public BbCodesPanel(Context context, Gallery gallery, EditText editText) {
        super(context, gallery, editText);


        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String bbCode = m_BbCodes[i];
                    if (bbCode.equals("LIST")) {
                        getListBbCodeOnClickListener("");
                    } else if (bbCode.equals("NUMLIST")) {
                        getListBbCodeOnClickListener("=1");
                    } else if (bbCode.equals("URL")) {
                        getUrlBbCodeOnClickListener();
                    } else if (bbCode.equals("SPOILER")) {
                        getSpoilerBbCodeOnClickListener(i);
                    } else if (bbCode.equals("COLOR") || bbCode.equals("BACKGROUND")) {
                        getColorBbCodeOnClickListener(bbCode, i);
                    } else if (bbCode.equals("SIZE")) {
                        getSizeBbCodeOnClickListener(i);
                    } else {

                        bbCodeClick(i);

                    }
                } catch (Exception ex) {
                    AppLog.e(mContext, ex);
                }
            }
        });


    }

    private void getSizeBbCodeOnClickListener(int tagIndex) {
        int selectionStart = txtPost.getSelectionStart();
        int selectionEnd = txtPost.getSelectionEnd();
        if (selectionEnd < selectionStart && selectionEnd != -1) {
            int c = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = c;
        }

        if (selectionStart == -1)
            selectionStart = 0;
        if (selectionEnd == -1)
            selectionEnd = 0;

        final int finalSelectionStart = selectionStart;
        final int finalSelectionEnd = selectionEnd;
        final CharSequence[] items = new CharSequence[]{"1", "2", "3", "4", "5", "6", "7"};
        new MaterialDialog.Builder(mContext)
                .title(org.softeg.slartus.forpdaplus.R.string.font_size)
                .items(items)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int i, CharSequence items) {
                        String tag = "[SIZE=" + (i + 1) + "]";
                        txtPost.getText().insert(finalSelectionStart, tag);
                        txtPost.getText().insert(finalSelectionEnd + tag.length(), "[/SIZE]");
                        return true; // allow selection
                    }
                })
                .cancelable(true)
                .negativeText(org.softeg.slartus.forpdaplus.R.string.cancel)
                .show();
    }

    private class BBColor {
        public String ColorName;
        public String ColorValue;

        public BBColor(String colorName, String colorValue) {
            ColorName = colorName;
            ColorValue = colorValue;
        }
    }

    private Dialog m_ColorsDialog;


    private void showColorDialog(final int selectionStart, final int selectionEnd, final String bbCode) {
        ArrayList<BBColor> colors = new ArrayList<BBColor>();
        colors.add(new BBColor("black", "#000000"));
        colors.add(new BBColor("white", "#FFFFFF"));
        colors.add(new BBColor("skyblue", "#82CEE8"));
        colors.add(new BBColor("royalblue", "#426AE6"));
        colors.add(new BBColor("blue", "#0000FF"));
        colors.add(new BBColor("darkblue", "#07008C"));
        colors.add(new BBColor("orange", "#FDA500"));
        colors.add(new BBColor("orangered", "#FF4300"));
        colors.add(new BBColor("crimson", "#E1133A"));
        colors.add(new BBColor("red", "#FF0000"));
        colors.add(new BBColor("darkred", "#8C0000"));
        colors.add(new BBColor("green", "#008000"));
        colors.add(new BBColor("limegreen", "#41A317"));
        colors.add(new BBColor("seagreen", "#4E8975"));
        colors.add(new BBColor("deeppink", "#F52887"));
        colors.add(new BBColor("tomato", "#FF6245"));
        colors.add(new BBColor("coral", "#F76541"));
        colors.add(new BBColor("purple", "#800080"));
        colors.add(new BBColor("indigo", "#440087"));
        colors.add(new BBColor("burlywood", "#E3B382"));
        colors.add(new BBColor("sandybrown", "#EE9A4D"));
        colors.add(new BBColor("sienna", "#C35817"));
        colors.add(new BBColor("chocolate", "#C85A17"));
        colors.add(new BBColor("teal", "#037F81"));
        colors.add(new BBColor("silver", "#C0C0C0"));

        ScrollView scrollView = new ScrollView(mContext);

        final LinearLayout tl = new LinearLayout(mContext);
        tl.setOrientation(LinearLayout.VERTICAL);

        int defaultRowCount = mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 5 : 3;
        int rowCount = defaultRowCount;

        LinearLayout.LayoutParams imgLayoutParams
                = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        imgLayoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 45, mContext.getResources().getDisplayMetrics());
        imgLayoutParams.setMargins(2, 2, 2, 2);
        LinearLayout tr = null;

        for (final BBColor bbColor : colors) {
            if (rowCount == defaultRowCount) {
                tr = new LinearLayout(mContext);
                tr.setOrientation(LinearLayout.HORIZONTAL);
                tl.addView(tr);
                rowCount = 0;
            }
            final String key = bbColor.ColorName;
            final String colorValue = bbColor.ColorValue;

            Button imageButton = new Button(mContext);
            imageButton.setBackgroundColor(Color.parseColor(colorValue));
            if (key.equals("white"))
                imageButton.setTextColor(Color.BLACK);
            else
                imageButton.setTextColor(Color.WHITE);
            imageButton.setContentDescription(key);
            imageButton.setText(key);
            imageButton.setTag("colored");
            imageButton.setLayoutParams(imgLayoutParams);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    m_ColorsDialog.dismiss();
                    m_ColorsDialog = null;
                    String tag = "[" + bbCode + "=" + key + "]";
                    txtPost.getText().insert(selectionStart, tag);
                    txtPost.getText().insert(selectionEnd + tag.length(), "[/" + bbCode + "]");
                }
            });

            tr.addView(imageButton, imgLayoutParams);

            rowCount++;
        }
        scrollView.addView(tl, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        m_ColorsDialog = new MaterialDialog.Builder(mContext)
                .cancelable(true)
                .customView(scrollView,true)
                .show();
    }

    private void getColorBbCodeOnClickListener(final String bbCode, int tagIndex) {
        int selectionStart = txtPost.getSelectionStart();
        int selectionEnd = txtPost.getSelectionEnd();
        if (selectionEnd < selectionStart && selectionEnd != -1) {
            int c = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = c;
        }

        if (selectionStart == -1)
            selectionStart = 0;
        if (selectionEnd == -1)
            selectionEnd = 0;

        int mColor = 0xFFFF0000;
        final int finalSelectionStart = selectionStart;
        final int finalSelectionEnd = selectionEnd;
        showColorDialog(selectionStart, selectionEnd, bbCode);
//        new UberColorPickerDialog(mContext, new UberColorPickerDialog.OnColorChangedListener() {
//            @Override
//            public void colorChanged(int color) {
//
//                String tag = "[" + bbCode + "=" + ExtColor.encodeRGB(color) + "]";
//                txtPost.getText().insert(finalSelectionStart, tag);
//                txtPost.getText().insert(finalSelectionEnd + tag.length(), "[/" + bbCode + "]");
//            }
//
//            @Override
//            public void canceled() {
//
//            }
//        }, mColor, false).show();
    }


    private void getSpoilerBbCodeOnClickListener(int tagIndex) {
        int selectionStart = txtPost.getSelectionStart();
        int selectionEnd = txtPost.getSelectionEnd();
        if (selectionEnd < selectionStart && selectionEnd != -1) {
            int c = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = c;
        }
        String spoilerText = null;
        if (selectionStart != -1 && selectionStart != selectionEnd) {
            spoilerText = txtPost.getText().toString()
                    .substring(selectionStart, selectionEnd);
        } else {
            if (mNotClosedCodes[tagIndex] > 0) {
                txtPost.getText().insert(selectionStart, "[/SPOILER]");
                mNotClosedCodes[tagIndex]--;
                return;
            }
        }
        createSpoilerDialog(spoilerText, tagIndex);
    }

    private void createSpoilerDialog(final String spoilerText, final int tagIndex) {


        LinearLayout layout = new LinearLayout(mContext);

        layout.setPadding(5, 5, 5, 5);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView tx = new TextView(mContext);
        tx.setText(org.softeg.slartus.forpdaplus.R.string.spoiler_title);
        layout.addView(tx);

        // Set an EditText view to get user input
        final EditText input = new EditText(mContext);

        input.requestFocus();
        layout.addView(input);
        final int[] selectionStart = {txtPost.getSelectionStart()};
        final int[] selectionEnd = {txtPost.getSelectionEnd()};

        new MaterialDialog.Builder(mContext)
                .customView(layout,true)
                .cancelable(false)
                .positiveText(org.softeg.slartus.forpdaplus.R.string.ok)
                .negativeText(org.softeg.slartus.forpdaplus.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (selectionEnd[0] < selectionStart[0] && selectionEnd[0] != -1) {
                            int c = selectionStart[0];
                            selectionStart[0] = selectionEnd[0];
                            selectionEnd[0] = c;
                        }
                        String spoilerName = input.getText().toString();
                        if (!TextUtils.isEmpty(spoilerName))
                            spoilerName = "=" + spoilerName;
                        String bbcode = "SPOILER";//TextUtils.isEmpty(spoilerName) ? "SPOILER" : "SPOIL";
                        String startSpoiler = "[" + bbcode + spoilerName + "]";

                        if (selectionStart[0] != -1 && selectionStart[0] != selectionEnd[0])
                            txtPost.getText().replace(selectionStart[0], selectionEnd[0], startSpoiler + spoilerText + "[/" + bbcode + "]");
                        else {
                            txtPost.getText().insert(selectionStart[0], startSpoiler);
                            mNotClosedCodes[tagIndex]++;
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        return;
                    }
                })
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        input.requestFocus();
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Service.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(input, 0);
                    }
                }).show();
    }

    @Override
    protected void initVars() {
        m_BbCodes = new String[]{"B", "I", "U", "S", "SUB", "SUP", "LEFT", "CENTER",
                "RIGHT", "URL", "QUOTE", "OFFTOP", "CODE", "SPOILER", "HIDE", "LIST", "NUMLIST", "COLOR", "BACKGROUND",
                "SIZE", "CUR"};
        mNotClosedCodes = new int[m_BbCodes.length];  //+1 - для спойлера с отрицательным индексом
    }

    @Override
    protected BbImage[] getImages() {
        String style = AppTheme.getCurrentThemeName();
        if(style.equals("dark")) style = "black";

        BbImage[] res = new BbImage[m_BbCodes.length];
        String path = "forum/style_images/1/folder_editor_buttons_" + style + "/";
        for (int i = 0; i < res.length; i++) {

            res[i] = new BbImage(path, m_BbCodes[i].toLowerCase() + ".png", m_BbCodes[i]);
        }
        return res;
    }

    private String[] m_BbCodes;
    private int[] mNotClosedCodes;


    private void getUrlBbCodeOnClickListener() {

        int selectionStart = txtPost.getSelectionStart();
        int selectionEnd = txtPost.getSelectionEnd();
        if (selectionEnd < selectionStart && selectionEnd != -1) {
            int c = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = c;
        }
        String urlText = null;
        if (selectionStart != -1 && selectionStart != selectionEnd) {
            urlText = txtPost.getText().toString()
                    .substring(selectionStart, selectionEnd);
        }

        createUrlDialog(null, urlText, mContext.getString(org.softeg.slartus.forpdaplus.R.string.enter_full_address), "https://");

    }

    private void createUrlDialog(final String url, final String urlText, String captionText, String editText) {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setPadding(5, 5, 5, 5);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView tx = new TextView(mContext);
        tx.setText(captionText);
        layout.addView(tx);

        // Set an EditText view to get user input
        final EditText input = new EditText(mContext);
        input.setText(editText);
        input.requestFocus();
        layout.addView(input);
        final int[] selectionStart = {txtPost.getSelectionStart()};
        final int[] selectionEnd = {txtPost.getSelectionEnd()};
        new MaterialDialog.Builder(mContext)
                .cancelable(false)
                .customView(layout,true)
                .positiveText(org.softeg.slartus.forpdaplus.R.string.ok)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String tempUrlText = urlText;
                        String tempUrl = url;
                        if (!TextUtils.isEmpty(url)) {
                            tempUrlText = input.getText().toString();
                        } else {
                            tempUrl = input.getText().toString();
                        }

                        if (TextUtils.isEmpty(tempUrlText)) {
                            createUrlDialog(input.getText().toString(), null, mContext.getString(org.softeg.slartus.forpdaplus.R.string.enter_title), mContext.getString(org.softeg.slartus.forpdaplus.R.string.visit_my_homepage));
                            return;
                        }

                        if (selectionEnd[0] < selectionStart[0] && selectionEnd[0] != -1) {
                            int c = selectionStart[0];
                            selectionStart[0] = selectionEnd[0];
                            selectionEnd[0] = c;
                        }
                        txtPost.getText().replace(selectionStart[0], selectionEnd[0], "[URL=" + (tempUrl == null ? "" : tempUrl) + "]" + tempUrlText + "[/URL]");
                    }
                })
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        input.requestFocus();
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Service.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(input, 0);
                    }
                })
                .show();
    }

    private void getListBbCodeOnClickListener(final String listTagPostFix) throws IOException {
        int selectionStart = txtPost.getSelectionStart();
        int selectionEnd = txtPost.getSelectionEnd();
        if (selectionEnd < selectionStart && selectionEnd != -1) {
            int c = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = c;
        }
        if (selectionStart != -1 && selectionStart != selectionEnd) {
            String selectedText = txtPost.getText().toString()
                    .substring(selectionStart, selectionEnd);
            while (selectedText.indexOf("\n\n") != -1) {
                selectedText = selectedText.replace("\n\n", "\n");
            }
            String modifiedText = "[LIST" + listTagPostFix + "]"
                    + selectedText
                    .replaceAll("^", "[*]")
                    .replace("\n", "\n[*]")
                    + "[/LIST]";
            txtPost.getText().replace(selectionStart, selectionEnd, modifiedText);
            return;
        }
        StringBuilder sb = new StringBuilder();
        createListDialog(1, sb, listTagPostFix);

    }

    private void createListDialog(final int ind, final StringBuilder sb, final String listTagPostFix) {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setPadding(5, 5, 5, 5);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView tx = new TextView(mContext);
        tx.setText(String.format(mContext.getString(org.softeg.slartus.forpdaplus.R.string.enter_content_n_item), ind));
        layout.addView(tx);

        // Set an EditText view to get user input
        final EditText input = new EditText(mContext);
        input.requestFocus();
        layout.addView(input);

        new MaterialDialog.Builder(mContext)
                .cancelable(false)
                .customView(layout,true)
                .positiveText(org.softeg.slartus.forpdaplus.R.string.ok)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (input.getText().toString().isEmpty()) {
                            tryInsertListText(sb, listTagPostFix);
                            return;
                        }
                        sb.append("[*]" + input.getText().toString() + "\n");
                        createListDialog(ind + 1, sb, listTagPostFix);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        tryInsertListText(sb, listTagPostFix);
                    }
                })
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        input.requestFocus();
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Service.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(input, 0);
                    }
                }).show();
    }

    private void tryInsertListText(StringBuilder sb, final String listTagPostFix) {
        String text = sb.toString().trim();
        if (TextUtils.isEmpty(text)) return;

        int selectionStart = txtPost.getSelectionStart();
        txtPost.getText().insert(selectionStart, "[LIST" + listTagPostFix + "]" + text + "[/LIST]");
    }

    private void bbCodeClick(int tagIndex) {
        String tag = m_BbCodes[tagIndex];

        int selectionStart = txtPost.getSelectionStart();
        int selectionEnd = txtPost.getSelectionEnd();
        if (selectionEnd < selectionStart && selectionEnd != -1) {
            int c = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = c;
        }
        if (selectionStart != -1 && selectionStart != selectionEnd) {
            txtPost.getText().insert(selectionStart, "[" + tag + "]");
            txtPost.getText().insert(selectionEnd + tag.length() + 2, "[/" + tag + "]");
            return;
        }

        if (mNotClosedCodes[tagIndex] > 0) {
            txtPost.getText().insert(selectionStart, "[/" + tag + "]");
            mNotClosedCodes[tagIndex]--;
        } else {
            txtPost.getText().insert(selectionStart, "[" + tag + "]");
            mNotClosedCodes[tagIndex]++;
        }
    }


}
