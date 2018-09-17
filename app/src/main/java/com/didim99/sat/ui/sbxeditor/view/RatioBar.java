package com.didim99.sat.ui.sbxeditor.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import com.didim99.sat.R;
import java.util.Locale;

/**
 * Created by didim99 on 30.08.18.
 */
public class RatioBar extends ValueBar {

  public RatioBar(Context context) {
    this(context, null, 0);
  }

  public RatioBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RatioBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    updateIconColor(1, 0);
  }

  @Override
  public void setValueInteger(int positive, int negative) {
    if (negative == 0) {
      progressBar.setProgress(positive == 0 ? MAX_VISIBLE_VALUE / 2 : MAX_VISIBLE_VALUE);
      percentage.setText(getResources().getString(R.string.valueBar_percentage, 100));
    } else {
      progressBar.setProgress(positive * MAX_VISIBLE_VALUE / (positive + negative));
      percentage.setText(String.format(Locale.US, getResources().getString(
        R.string.valueBar_ratio), (float) positive / negative ));
    }
    values.setText(String.format(Locale.US, getResources().getString(
      R.string.valueBar_valueInteger), negative, positive));
    updateIconColor(positive, negative);
  }

  private void updateIconColor(double positive, double negative) {
    if (!hasIcon) return;
    iconView.getDrawable().setColorFilter(
      negative > positive ? colorBg : colorFg, PorterDuff.Mode.SRC_IN);
  }
}
