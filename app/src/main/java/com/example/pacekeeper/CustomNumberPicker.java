package com.example.pacekeeper;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import androidx.core.content.res.ResourcesCompat;

public class CustomNumberPicker extends android.widget.NumberPicker {
    private Typeface type;
    private final Context context;

    public CustomNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        type = ResourcesCompat.getFont(context, R.font.fugaz_one);
        updateView(child);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);

        type = ResourcesCompat.getFont(context, R.font.fugaz_one);
        updateView(child);
    }

    private void updateView(View view) {

        if (view instanceof EditText) {
            ((EditText) view).setTypeface(type);
            ((EditText) view).setTextSize(35);
            ((EditText) view).setTextColor(getResources().getColor(
                    R.color.text_color));
        }

    }
}
