package com.didim99.sat.sbxeditor.model;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;

/**
 * Input values validator and converter
 * Created by didim99 on 16.08.18.
 */
public class InputValidator {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_InputValidator";
  private static InputValidator instance = new InputValidator();
  public static InputValidator getInstance() { return instance; }
  private InputValidator() {}

  private Toast toastMsg;

  public void init(Context appContext) {
    toastMsg = Toast.makeText(appContext, "", Toast.LENGTH_LONG);
  }

  public boolean checkSbxName(String name, boolean allowEmpty) {
    if (name.isEmpty()) {
      if (!allowEmpty)
        showToast(R.string.sandboxNameIsEmpty);
      return allowEmpty;
    } else {
      boolean valid = !name.matches(SBML.INVALID_SBX_NAME);
      if (!valid)
        showToast(R.string.sandboxNameIncorrect);
      return valid;
    }
  }

  public Integer checkInteger(EditText src, Integer minValue,
                              int msgId_empty, int msgId_incorrect, String logMsg)
    throws ValidationException {
    return checkInteger(src, minValue, null,
      msgId_empty, msgId_incorrect, logMsg);
  }

  public Integer checkInteger(EditText src, Integer minValue, Integer maxValue,
                              int msgId_empty, int msgId_incorrect, String logMsg)
    throws ValidationException {
    if (minValue == null) minValue = Integer.MIN_VALUE;
    if (maxValue == null) maxValue = Integer.MAX_VALUE;

    String strValue = checkEmptyStr(src, msgId_empty, logMsg);
    if (strValue == null) return null;

    try {
      Integer result = Integer.parseInt(strValue);
      if (result < minValue || result > maxValue)
        throw new IllegalArgumentException("Incorrect value "
          + "(min: " + minValue + ", max: " + maxValue + ", value: " + result + ")");
      return result;
    } catch (IllegalArgumentException e) {
      MyLog.w(LOG_TAG, "Incorrect value: " + logMsg);
      showToast(msgId_incorrect);
      throw new ValidationException();
    }
  }

  public Float checkFloat(EditText src, Number multiplier,
                          int msgId_empty, int msgId_incorrect, String logMsg)
    throws ValidationException {
    return checkFloat(src, multiplier, null, null,
      msgId_empty, msgId_incorrect, logMsg);
  }

  public Float checkFloat(EditText src, Number multiplier,
                          int msgId_incorrect, String logMsg)
    throws ValidationException {
    return checkFloat(src, multiplier, null, null,
      0, msgId_incorrect, logMsg);
  }

  public Float checkFloat(EditText src, Number multiplier, Number minValue, Number maxValue,
                          int msgId_empty, int msgId_incorrect, String logMsg)
    throws ValidationException {
    if (multiplier == null) multiplier = 1;
    if (minValue == null) minValue = Double.MIN_VALUE;
    if (maxValue == null) maxValue = Double.MAX_VALUE;

    String strValue = checkEmptyStr(src, msgId_empty, logMsg);
    if (strValue == null) return null;

    try {
      Double result = Double.parseDouble(strValue) * multiplier.doubleValue();
      if (result < minValue.doubleValue() || result > maxValue.doubleValue())
        throw new IllegalArgumentException("Incorrect value "
          + "(min: " + minValue + ", max: " + maxValue + ", value: " + result + ")");
      return result.floatValue();
    } catch (IllegalArgumentException e) {
      MyLog.w(LOG_TAG, "Incorrect value: " + logMsg);
      showToast(msgId_incorrect);
      throw new ValidationException();
    }
  }

  public String checkEmptyStr(EditText src, int msgId_empty, String logMsg)
    throws ValidationException {
    String strValue = src.getText().toString();
    if (strValue.isEmpty()) {
      if (msgId_empty != 0) {
        MyLog.w(LOG_TAG, "Empty value: " + logMsg);
        showToast(msgId_empty);
        throw new ValidationException();
      } else
        return null;
    } else return strValue;
  }

  private void showToast(int msgId) {
    toastMsg.setText(msgId);
    toastMsg.show();
  }

  public class ValidationException extends IllegalArgumentException {}
}
