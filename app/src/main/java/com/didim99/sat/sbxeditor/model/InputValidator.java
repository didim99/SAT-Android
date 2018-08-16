package com.didim99.sat.sbxeditor.model;

import android.content.Context;
import android.widget.Toast;
import com.didim99.sat.R;

/**
 * Created by didim99 on 16.08.18.
 */
public class InputValidator {

  public enum Response {
    EMPTY, INCORRECT, OK
  }

  private static Toast toastMsg;

  public static void init(Context appContext) {
    toastMsg = Toast.makeText(appContext, "", Toast.LENGTH_LONG);
  }

  public static boolean checkSbxName(String name, boolean allowEmpty) {
    if (name.isEmpty()) {
      if (!allowEmpty) {
        toastMsg.setText(R.string.sandboxNameIsEmpty);
        toastMsg.show();
      }
      return allowEmpty;
    } else {
      boolean valid = !name.matches(SBML.INVALID_SBX_NAME);
      if (!valid) {
        toastMsg.setText(R.string.sandboxNameIncorrect);
        toastMsg.show();
      }
      return valid;
    }
  }
}
