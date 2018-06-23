package com.android.Aage;

import android.content.Context;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;

/**
 * Created by Administrator on 2017/8/15.
 */

public class AutoControlTextSizeEditText extends EditText {

    //unit px
    private static float DEFAULT_MIN_TEXT_SIZE = 10;
    private static float DEFAULT_MAX_TEXT_SIZE = 30;
    // Attributes
    private TextPaint testPaint;
    private float minTextSize;
    private float maxTextSize;

    public AutoControlTextSizeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    public void initialise() {
        testPaint = new TextPaint();
        testPaint.set(this.getPaint());
        // max size defaults to the intially specified text size unless it is too small
        maxTextSize = this.getTextSize();
        if (maxTextSize <= DEFAULT_MIN_TEXT_SIZE) {
            maxTextSize = DEFAULT_MAX_TEXT_SIZE;
        }
        minTextSize = DEFAULT_MIN_TEXT_SIZE;
    }

    /**
     * Re size the font so the specified text fits in the text box * assuming the text box is the specified width.
     */
    private void refitText(String text, int textWidth, int textHeight) {
        if (textWidth > 0&&textHeight>0) {
            //allow diplay rect
            int availableWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
            int availableHeight = textHeight - this.getPaddingBottom() - this.getPaddingTop();
            //by the line calculate allow displayWidth
            int autoWidth = availableWidth;
            float mult=1f;
            float add=0;
            if (Build.VERSION.SDK_INT>16)
            {
                mult=getLineSpacingMultiplier();
                add=getLineSpacingExtra();
            }else{
                //the mult default is 1.0f,if you need change ,you can reflect invoke this field;
            }
            float trySize = maxTextSize;
            testPaint.setTextSize(trySize);
            int oldline=1,newline=1;
            while ((trySize > minTextSize)) {
                //calculate text singleline width。
                int displayW = (int) testPaint.measureText(text);
                //calculate text singleline height。
                int displaH=round(testPaint.getFontMetricsInt(null)*mult+add);
                if (displayW < autoWidth) {
                    break;
                }
                //calculate maxLines
                newline = availableHeight / displaH;
                //if line change ,calculate new autoWidth
                if (newline > oldline) {
                    oldline=newline;
                    autoWidth = availableWidth * newline;
                    continue;
                }
                //try more small TextSize
                trySize -= 1;
                if (trySize <= minTextSize) {
                    trySize = minTextSize;
                    break;
                }

                testPaint.setTextSize(trySize);
            }
            //setMultiLine
            if (newline>=2)
            {
                this.setSingleLine(false);
                this.setMaxLines(newline);
            }
            this.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        refitText(text.toString(), this.getWidth(), this.getHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.e("TagSizeChange","new("+w+","+h+") old("+oldw+""+oldh+")");
        if (w != oldw || h != oldh) {
            refitText(this.getText().toString(), w, h);
        }
    }
    //FastMath.round()
    public static int round(float value) {
        long lx = (long) (value * (65536 * 256f));
        return (int) ((lx + 0x800000) >> 24);
    }

}
