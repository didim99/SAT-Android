package com.didim99.sat.sbxeditor.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.didim99.sat.R;
import java.util.Locale;

/**
 * Custom ProgressBar class with percentage and value displaying.
 * Created by didim99 on 29.08.18.
 */
public class ValueBar extends LinearLayout {
  protected static final int MAX_VISIBLE_VALUE = 1000;

  protected final ProgressBar progressBar;
  protected final TextView percentage;
  protected final TextView values;
  protected final ImageView iconView;
  protected final int colorBg, colorFg;
  protected final boolean hasIcon;

  public ValueBar(Context context) {
    this(context, null, 0);
  }

  public ValueBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ValueBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray array = context.obtainStyledAttributes(
      attrs, R.styleable.ValueBar, defStyleAttr, 0);
    colorBg = array.getColor(R.styleable.ValueBar_colorBackground,
      getResources().getColor(R.color.valueBarBg));
    colorFg = array.getColor(R.styleable.ValueBar_colorForeground,
      getResources().getColor(R.color.valueBarFg));
    int iconID = array.getResourceId(R.styleable.ValueBar_iconID, 0);
    array.recycle();

    LayerDrawable drawable = (LayerDrawable) getResources().getDrawable(R.drawable.bar_circle);
    drawable.findDrawableByLayerId(android.R.id.background)
      .setColorFilter(colorBg, PorterDuff.Mode.SRC_IN);
    drawable.findDrawableByLayerId(R.id.bar_circle_fg)
      .setColorFilter(colorFg, PorterDuff.Mode.SRC_IN);

    View itemView = LayoutInflater.from(context).inflate(
      R.layout.view_bar_circle_single, this);
    iconView = itemView.findViewById(R.id.icon);
    progressBar = itemView.findViewById(R.id.progressBar);
    percentage = itemView.findViewById(R.id.percentage);
    values = itemView.findViewById(R.id.value);
    progressBar.setProgressDrawable(drawable);
    progressBar.setMax(MAX_VISIBLE_VALUE);
    setValueIfMaxZero(0);

    hasIcon = iconID != 0;
    if (hasIcon) {
      iconView.setImageResource(iconID);
      updateIconColor(0);
    } else {
      iconView.setVisibility(GONE);
    }
  }

  public void setValueDouble(double max, double current) {
    if (!(max > 0))
      setValueIfMaxZero((int) Math.ceil(current));
    else {
      progressBar.setProgress((int) (current * MAX_VISIBLE_VALUE / max));
      percentage.setText(getResources().getString(R.string.valueBar_percentage,
        (int) (current * 100 / max)));
    }
    values.setText(String.format(Locale.US, getResources().getString(
      R.string.valueBar_valueFloat), current, max));
    updateIconColor(max);
  }

  public void setValueInteger(int max, int current) {
    if (max == 0)
      setValueIfMaxZero(current);
    else {
      progressBar.setProgress(current * MAX_VISIBLE_VALUE / max);
      percentage.setText(getResources().getString(R.string.valueBar_percentage,
        current * 100 / max));
    }
    values.setText(getResources().getString(R.string.valueBar_valueInteger, current, max));
    updateIconColor(max);
  }

  private void setValueIfMaxZero(int current) {
    progressBar.setProgress(MAX_VISIBLE_VALUE);
    if (current == 0)
      progressBar.setProgress(0);
    percentage.setText(getResources().getString(R.string.valueBar_percentage,
      current > 0 ? 100 : 0));
  }

  private void updateIconColor(double maxValue) {
    if (!hasIcon) return;
    iconView.getDrawable().setColorFilter(
      maxValue == 0.0 ? colorBg : colorFg, PorterDuff.Mode.SRC_IN);
  }
}
