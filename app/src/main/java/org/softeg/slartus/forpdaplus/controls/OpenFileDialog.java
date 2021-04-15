package org.softeg.slartus.forpdaplus.controls;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.AppTheme;
import org.softeg.slartus.forpdaplus.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
 * Created by slinkin on 16.10.2015.
 */
public class OpenFileDialog extends AlertDialog.Builder {

    private String currentPath = Environment.getExternalStorageDirectory().getPath();
    private final List<File> files = new ArrayList<>();
    private final TextView title;
    private final ListView listView;
    private FilenameFilter filenameFilter;
    private int selectedIndex = -1;
    private OpenDialogListener listener;
    private String accessDeniedMessage;
    private final static int ICON_FOLDER = R.drawable.ic_folder;
    private final static int ICON_FILE = R.drawable.ic_file;


    public interface OpenDialogListener {
        void OnSelectedFile(String fileName);
    }

    private class FileAdapter extends ArrayAdapter<File> {
        private final LayoutInflater mInflater;

        public FileAdapter(Context context, List<File> files) {
            super(context, android.R.layout.simple_list_item_1, files);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            final ViewHolder holder;
            if (view == null) {
                holder = new ViewHolder();

                view = mInflater.inflate(R.layout.file, parent, false);
                holder.file_textview = (TextView) view;
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            File file = getItem(position);

            holder.file_textview.setText(file.getName());
            // If the item is not a directory, use the file icon
            int icon = file.isDirectory() ? ICON_FOLDER : ICON_FILE;
            holder.file_textview.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
            if (!file.isDirectory()) {
                if (selectedIndex == position)
                    holder.file_textview.setBackgroundColor(getContext().getResources().getColor(R.color.holo_blue_dark));
                else
                    holder.file_textview.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
            }

            return holder.file_textview;
        }

        class ViewHolder {
            TextView file_textview;
        }

    }

    private final Context mContext;


    @Override
    public Context getContext() {
        if (Build.VERSION.SDK_INT >= 11)
            return super.getContext();
        return mContext;
    }

    public OpenFileDialog(Context context) {
        super(context);
        mContext = context;
        title = createTitle(context);
        changeTitle();
        LinearLayout linearLayout = createMainLayout(context);
        linearLayout.addView(createBackItem(context));
        listView = createListView(context);
        linearLayout.addView(listView);
        setCustomTitle(title)
                .setView(linearLayout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedIndex > -1 && listener != null) {
                            listener.OnSelectedFile(listView.getItemAtPosition(selectedIndex).toString());
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
    }

    @Override
    public AlertDialog show() {
        try {
            files.addAll(getFiles(currentPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        listView.setAdapter(new FileAdapter(getContext(), files));
        return super.show();
    }

    public OpenFileDialog setFilter(final String filter) {
        filenameFilter = new FilenameFilter() {

            @Override
            public boolean accept(File file, String fileName) {
                File tempFile = new File(String.format("%s/%s", file.getPath(), fileName));
                return !tempFile.isFile() || tempFile.getName().matches(filter);
            }
        };
        return this;
    }

    public OpenFileDialog setOpenDialogListener(OpenDialogListener listener) {
        this.listener = listener;
        return this;
    }

    public OpenFileDialog setAccessDeniedMessage(String message) {
        this.accessDeniedMessage = message;
        return this;
    }

    private static Display getDefaultDisplay(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    private static Point getScreenSize(Context context) {
        return getDisplaySize(getDefaultDisplay(context));
    }

    private static Point getDisplaySize(final Display display) {
        final Point point = new Point();
        if (Build.VERSION.SDK_INT >= 13) {
            try {
                display.getSize(point);
            } catch (NoSuchMethodError ignore) { // Older device
                point.x = display.getWidth();
                point.y = display.getHeight();
            }
        } else {
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        return point;
    }

    private static int getLinearLayoutMinHeight(Context context) {
        return getScreenSize(context).y;
    }

    private LinearLayout createMainLayout(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setBackgroundColor(AppTheme.getThemeBackgroundColorRes());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setMinimumHeight(getLinearLayoutMinHeight(context));
        return linearLayout;
    }

    private int getItemHeight(Context context) {
        TypedValue value = new TypedValue();
        DisplayMetrics metrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= 14)
            context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeightSmall, value, true);
        else
            value.data = 12289;
        getDefaultDisplay(context).getMetrics(metrics);
        return (int) TypedValue.complexToDimension(value.data, metrics);
    }

    private TextView createTextView(Context context, int style) {
        TextView textView = new TextView(context);
        textView.setTextAppearance(context, style);
        textView.setBackgroundColor(AppTheme.getThemeBackgroundColorRes());
        textView.setTextColor(AppTheme.getThemeTextColorRes());
        int itemHeight = getItemHeight(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
        textView.setMinHeight(itemHeight);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(15, 0, 0, 0);
        return textView;
    }

    private TextView createTitle(Context context) {
        return createTextView(context, android.R.style.TextAppearance_Medium);
    }

    private TextView createBackItem(Context context) {
        TextView textView = createTextView(context, android.R.style.TextAppearance_Medium);

        int icon = android.R.drawable.ic_menu_directions;
        textView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        textView.setText("..");
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    File file = new File(currentPath);
                    File parentDirectory = file.getParentFile();
                    if (parentDirectory != null) {
                        currentPath = parentDirectory.getPath();
                        RebuildFiles(((FileAdapter) listView.getAdapter()));
                    }
                } catch (Throwable ex) {
                    Toast.makeText(getContext(), isNullOrEmpty(ex.getLocalizedMessage(), ex.getMessage()), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        return textView;
    }

    private String isNullOrEmpty(String text1, String text2) {
        if (text1 != null && !TextUtils.isEmpty(text1))
            return text1;
        return text2;
    }

    public int getTextWidth(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.left + bounds.width() + 80;
    }

    private void changeTitle() {
        String titleText = currentPath;
        int screenWidth = getScreenSize(getContext()).x;
        int maxWidth = (int) (screenWidth * 0.99);
        if (getTextWidth(titleText, title.getPaint()) > maxWidth) {
            while (getTextWidth("..." + titleText, title.getPaint()) > maxWidth) {
                int start = titleText.indexOf("/", 2);
                if (start > 0)
                    titleText = titleText.substring(start);
                else
                    titleText = titleText.substring(2);
            }
            title.setText("..." + titleText);
        } else {
            title.setText(titleText);
        }
    }

    private List<File> getFiles(String directoryPath) throws Exception {
        File directory = new File(directoryPath);
        List<File> fileList = new ArrayList<>();
        File[] listFiles = directory.listFiles(filenameFilter);
        if (listFiles == null)
            throw new Exception("Пустая директория или доступ запрещен");
        for (File file : directory.listFiles(filenameFilter)) {
            if (file.isDirectory() && file.getName().startsWith("."))
                continue;
            fileList.add(file);
        }


        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File file, File file2) {
                if (file.isDirectory() && file2.isFile())
                    return -1;
                else if (file.isFile() && file2.isDirectory())
                    return 1;
                else
                    return file.getPath().compareToIgnoreCase(file2.getPath());
            }
        });
        return fileList;
    }

    private void RebuildFiles(ArrayAdapter<File> adapter) throws Exception {
        try {
            List<File> fileList = getFiles(currentPath);
            files.clear();
            selectedIndex = -1;
            files.addAll(fileList);
            adapter.notifyDataSetChanged();
            changeTitle();

        } catch (NullPointerException e) {
            String message = getContext().getResources().getString(android.R.string.unknownName);
            if (!accessDeniedMessage.equals(""))
                message = accessDeniedMessage;
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private ListView createListView(Context context) {
        ListView listView = new ListView(context);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                try {
                    final ArrayAdapter<File> adapter = (FileAdapter) adapterView.getAdapter();
                    File file = adapter.getItem(index);
                    if (file.isDirectory()) {
                        currentPath = file.getPath();
                        RebuildFiles(adapter);
                    } else {
                        if (index != selectedIndex)
                            selectedIndex = index;
                        else
                            selectedIndex = -1;
                        adapter.notifyDataSetChanged();
                    }
                } catch (Throwable ex) {
                    Toast.makeText(getContext(), isNullOrEmpty(isNullOrEmpty(ex.getLocalizedMessage(), ex.getMessage()), ex.toString()), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        return listView;
    }
}

