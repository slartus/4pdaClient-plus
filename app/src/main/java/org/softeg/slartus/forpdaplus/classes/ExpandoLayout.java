package org.softeg.slartus.forpdaplus.classes;

/**
 * User: slinkin
 * Date: 05.03.12
 * Time: 8:20
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.softeg.slartus.forpdaplus.R;


public class ExpandoLayout extends ViewGroup implements AnimationListener {

    public static int DEFAULT_TEXT_SIZE = 17;
    public static int DEFAULT_SUBTITLE_TEXT_SIZE = 10;
    public static int DEFAULT_TITLE_ROW_PADDING = 0;
    public static int DEFAULT_MORE_BAR_HEIGHT = 50;
    public static int DEFAULT_MORE_BAR_FADE_COLOR = 0x00000;
    public static String DEFAULT_COMPACT_TEXT = "More";
    public static String DEFAULT_EXPANDED_TEXT = "Less";
    public static int DEFAULT_FADE_HEIGHT = 40;

    private boolean expanded = false;
    private String text = null;
    private String m_SubTitleText = null;
    private int textSize = DEFAULT_TEXT_SIZE;
    private int m_SubTitleTextSize = DEFAULT_SUBTITLE_TEXT_SIZE;
    private int titleRowPadding = DEFAULT_TITLE_ROW_PADDING;
    private boolean moreBar = false;
    private boolean moreBarOnRight = false;
    private int moreBarDefaultHeight = DEFAULT_MORE_BAR_HEIGHT;
    private int moreBarFadeColor = DEFAULT_MORE_BAR_FADE_COLOR;
    private String compactText = DEFAULT_COMPACT_TEXT;
    private String expandedText = DEFAULT_EXPANDED_TEXT;
    private boolean showAndHideChildren = false;
    private boolean useAnimation = true;

    private Context context;
    private LinearLayout titleRow;
    private ImageView icon;
    private TextView more;
    private Bitmap fade;
    boolean toggleAtAnimationEnd;
    boolean showHideAtAnimationEnd;
    boolean firstShowHideCall = true;

    public ExpandoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
/*
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandoLayout);
        try {
            text = a.getString(R.styleable.ExpandoLayout_text);

            titleRowPadding = a.getInt(R.styleable.ExpandoLayout_titleRowPadding, DEFAULT_TITLE_ROW_PADDING);
            expanded = a.getBoolean(R.styleable.ExpandoLayout_expanded, false);
            moreBar = a.getBoolean(R.styleable.ExpandoLayout_moreBar, false);
            moreBarOnRight = a.getBoolean(R.styleable.ExpandoLayout_moreBarOnRight, false);
            moreBarDefaultHeight = a.getInt(R.styleable.ExpandoLayout_moreBarDefaultViewHeight, DEFAULT_MORE_BAR_HEIGHT);
            moreBarFadeColor = a.getInt(R.styleable.ExpandoLayout_moreBarFadeColor, DEFAULT_MORE_BAR_FADE_COLOR);
            if (a.hasValue(R.styleable.ExpandoLayout_compactText))
                compactText = a.getString(R.styleable.ExpandoLayout_compactText);
            if (a.hasValue(R.styleable.ExpandoLayout_expandedText))
                expandedText = a.getString(R.styleable.ExpandoLayout_expandedText);
            showAndHideChildren = a.getBoolean(R.styleable.ExpandoLayout_showAndHideChildren, false);
        } finally {
            a.recycle();
        }
        */
    }

    public ExpandoLayout(Context context) {
        this(context, null);
    }

    public void setText(String text) {
        this.text = text;
        m_TitleView.setText(text);
    }

    public void setSubTitleText(String text) {
        this.m_SubTitleText = text;
        m_SubTitleView.setText(text);
    }

    public void setSubTitleTextColor(int color) {

        m_SubTitleView.setTextColor(color);
    }


    private TextView m_TitleView;
    private TextView m_SubTitleView;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        titleRow = new LinearLayout(context);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        titleRow.setPadding(titleRowPadding, titleRowPadding, titleRowPadding, titleRowPadding);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        // TODO: setting this causes the icon drawable to not draw/size properly if it is the
        //       first numbered drawable (hence the need for the 'aaa' drawable placeholder)
        //titleRow.setBackgroundResource(R.drawable.title_row_bkgnd);
        if (!moreBar) {
            icon = new ImageView(context);
            setIconImage();
            icon.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            icon.setPadding(0, 0, 5, 0);
            titleRow.addView(icon);

            LinearLayout titleLayout = new LinearLayout(context);
            titleLayout.setOrientation(LinearLayout.VERTICAL);
            titleLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            titleLayout.setPadding(titleRowPadding, titleRowPadding, titleRowPadding, titleRowPadding);
            titleLayout.setGravity(Gravity.CENTER_VERTICAL);

            m_TitleView = new TextView(context);
            m_TitleView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            m_TitleView.setText(text);
            m_TitleView.setTextSize(textSize);
            //  m_TitleView.setGravity(Gravity.CENTER_VERTICAL);
            titleLayout.addView(m_TitleView);

            m_SubTitleView = new TextView(context);
            m_SubTitleView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            m_SubTitleView.setText(m_SubTitleText);
            m_SubTitleView.setTextSize(m_SubTitleTextSize);
            //  m_SubTitleView.setGravity(Gravity.BOTTOM);
            titleLayout.addView(m_SubTitleView);

            titleRow.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    toggleExpand();
                }
            });
            titleRow.addView(titleLayout);
            addView(titleRow, 0);
        } else {
            more = new TextView(context);
            more.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            setMoreText();
            titleRow.addView(more);
            View v = new View(context);
            v.setBackgroundColor(Color.LTGRAY);
            v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 2));
            ((LinearLayout.LayoutParams) v.getLayoutParams()).weight = 1;
            if (moreBarOnRight) {
                titleRow.addView(v, 0);
                more.setPadding(5, 0, 0, 0);
            } else {
                titleRow.addView(v);
                more.setPadding(0, 0, 5, 0);
            }
            titleRow.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    toggleExpand();
                }
            });
            addView(titleRow);
        }
    }

    protected void toggleExpand() {
        if (useAnimation) {
            // get start and end dimensions
            int width = getWidth();
            int startHeight = getHeight();
            expanded = !expanded;
            measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(1000000, MeasureSpec.AT_MOST));
            int endHeight = getMeasuredHeight();
            // set measure dimensions back to original to stop flicker
            //   we will change this again in onAnimationStart
            expanded = !expanded;
            measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(1000000, MeasureSpec.AT_MOST));
            // create animation
            Animation animation = new ExpandAnimation(this, width, startHeight, endHeight);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setDuration(100L);
            animation.setAnimationListener(this);
            startAnimation(animation);
        } else {
            setToggleFlagAndGui();
            requestLayout();
        }

        // play clicky sound
        playSoundEffect(SoundEffectConstants.CLICK);

    }


    public void onAnimationStart(Animation animation) {
        super.onAnimationStart();
        if (!moreBar || !expanded) {
            setToggleFlagAndGui();
            toggleAtAnimationEnd = false;
        } else
            toggleAtAnimationEnd = true;
        if (expanded) {
            showHideChildren();
            showHideAtAnimationEnd = false;
        } else
            showHideAtAnimationEnd = true;
    }


    public void onAnimationRepeat(Animation animation) {
    }


    public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd();
        if (showHideAtAnimationEnd)
            showHideChildren();
        if (toggleAtAnimationEnd)
            setToggleFlagAndGui();
        ((ExpandAnimation) animation).ResetViewLayout();
    }

    private void setToggleFlagAndGui() {
        expanded = !expanded;
        // toggle icon/text
        if (!moreBar)
            setIconImage();
        else
            setMoreText();
    }

    private void setIconImage() {
        if (expanded)
            icon.setImageResource(R.drawable.expander_ic_maximized);
        else
            icon.setImageResource(R.drawable.expander_ic_minimized);
    }

    private void setMoreText() {
        if (expanded)
            more.setText(expandedText);
        else
            more.setText(compactText);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getPaddingLeft();
        int height = getPaddingTop();

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.x = getPaddingLeft();
            lp.y = height;
            // move the moreBar down enough to partially see the content
            if (moreBar && !expanded && child == titleRow)
                lp.y += moreBarDefaultHeight;

            width = Math.max(width, getPaddingLeft() + child.getMeasuredWidth());
            if (expanded || child == titleRow)
                height += child.getMeasuredHeight();
        }

        width += getPaddingLeft() + getPaddingRight();
        height += getPaddingBottom();

        // enlarge the view enough to see the moreBar
        if (moreBar && !expanded)
            height += moreBarDefaultHeight;

        setMeasuredDimension(resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            child.layout(lp.x, lp.y,
                    lp.x + child.getMeasuredWidth(),
                    lp.y + child.getMeasuredHeight());
        }
        if (firstShowHideCall || !useAnimation) {
            firstShowHideCall = false;
            showHideChildren();
        }
    }

    private void showHideChildren() {
        if (showAndHideChildren) {
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                // hide or show child views if the 'showAndHideChildren' prop set
                if (expanded && child != titleRow)
                    child.setVisibility(View.VISIBLE);
                else if (!expanded && child != titleRow)
                    child.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        // draw the fade-in when the moreBar is compacted
        if (moreBar && moreBarDefaultHeight > 0 && !expanded) {
            final int height = getHeight() - titleRow.getHeight() - DEFAULT_FADE_HEIGHT;
            canvas.drawBitmap(fade, 0, height, null);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0)
            createFadeBitmap();
    }

    /**
     * Here we create the bitmap that will be painted for the fade effect
     * with a compacted moreBar
     */
    private void createFadeBitmap() {
        int width = getWidth();
        int height = DEFAULT_FADE_HEIGHT;
        if (fade == null || fade.getWidth() != width) {
            fade = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(fade);
            // draw fade bitmap
            for (int y = 0; y < height; y++) {
                Paint paint = new Paint();
                paint.setColor(moreBarFadeColor);
                int alpha = (int) ((float) y / height * 255);
                paint.setAlpha(alpha);
                canvas.drawLine(0, y, width, y + 1, paint);
            }
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        // Stop child views from drawing underneath the moreBar area
        if (moreBar && moreBarDefaultHeight > 0 && !expanded
                && child != titleRow) {
            final int restoreCount = canvas.save();
            final int width = getWidth();
            final int height = getHeight();
            final int titleRowHeight = titleRow.getHeight();
            canvas.clipRect(0, 0, width, height - titleRowHeight);
            boolean res = super.drawChild(canvas, child, drawingTime);
            canvas.restoreToCount(restoreCount);
            return res;
        } else
            return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p.width, p.height);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public int x;
        public int y;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }
    }

    public static class ExpandAnimation extends Animation {
        private View view;
        private ViewGroup.LayoutParams lp;
        private int width, startHeight, endHeight;
        boolean reset = false;

        public ExpandAnimation(View view, int width, int startHeight, int endHeight) {
            this.view = view;
            this.lp = view.getLayoutParams();
            this.width = width;
            this.startHeight = startHeight;
            this.endHeight = endHeight;
        }

        public void ResetViewLayout() {
            reset = true;
            view.setLayoutParams(lp);
            view.requestLayout();
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (!reset) {
                float dhTotal = endHeight - startHeight;
                view.setLayoutParams(new LinearLayout.LayoutParams(
                        width, startHeight + Math.round(dhTotal * interpolatedTime)));
            }
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }

        @Override
        public boolean willChangeTransformationMatrix() {
            return false;
        }
    }
}

