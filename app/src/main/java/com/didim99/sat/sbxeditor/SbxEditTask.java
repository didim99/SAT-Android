package com.didim99.sat.sbxeditor;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.Utils;
import com.didim99.sat.sbxeditor.TextGenerator.IllegalCharException;
import java.io.IOException;
import java.lang.ref.SoftReference;

/*
 * Created by didim99 on 21.02.18.
 */

public class SbxEditTask extends AsyncTask<SbxEditConfig, Void, String> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_sbxEditTask";

  public static final class Event {
    public static final int START = 1;
    public static final int FINISH = 2;
  }

  private static final class Error {
    private static final int OPEN_IO = 1;
    private static final int PARSER_FAILURE = 2;
    private static final int FONT_LOAD = 3;
    private static final int ALL_LINES_EMPTY = 4;
    private static final int ILLEGAL_CHAR = 5;
  }

  //Application level
  private SoftReference<Context> appContext;
  private EventListener listener;
  //local
  private int mode, objType, modCount;
  private boolean newFileCreated;
  //Errors
  private String errMsg, sysMsg;
  private int errCode;
  private int lineNum;
  private char illegalChar;

  public SbxEditTask(Context appContext) {
    this.appContext = new SoftReference<>(appContext);
  }

  public void registerEventListener(EventListener listener) {
    this.listener = listener;
  }

  public void unregisterEventListener() {
    this.listener = null;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    if (listener != null)
      listener.onTaskEvent(Event.START, true);
  }

  @Override
  protected String doInBackground(SbxEditConfig... configs) {
    MyLog.d(LOG_TAG, "Executing...");
    SbxEditConfig config = configs[0];
    objType = Station.getObjType(config.getStations());
    mode = config.getMode();

    switch (mode) {
      case Sandbox.Mode.CREATE:
        Storage.setSandbox(new Sandbox(appContext.get(), config));
        break;
      case Sandbox.Mode.OPEN:
        try {
          Storage.setSandbox(new Sandbox(appContext.get(), config.getFileName()));
        } catch (IOException e) {
          this.errMsg = e.getMessage();
          this.errCode = Error.OPEN_IO;
        } catch (Sandbox.SBMLParserException e) {
          this.errMsg = e.getMessage();
          this.errCode = Error.PARSER_FAILURE;
          this.lineNum = e.getLineNum();
        }
        break;
      case Sandbox.Mode.ADD_MODULE:
        modCount = config.getCount();
        Storage.getSandbox().addModule(config);
        break;
      case Sandbox.Mode.ADD_COLONY:
        Storage.getSandbox().createColony(config);
        objType = Station.Type.COLONY;
        break;
      case Sandbox.Mode.ADD_ALL:
        Storage.getSandbox().addAllModules(config);
        break;
      case Sandbox.Mode.ADD_TEXT:
        try {
          Storage.getSandbox().addText(config);
        } catch (IOException e) {
          this.errMsg = e.getMessage();
          this.errCode = Error.FONT_LOAD;
        } catch (IllegalCharException e) {
          this.errMsg = e.getMessage();
          this.errCode = Error.ILLEGAL_CHAR;
          this.illegalChar = e.getIllegalChar();
        } catch (IllegalArgumentException e) {
          this.errMsg = e.getMessage();
          this.errCode = Error.ALL_LINES_EMPTY;
        }
        break;
      case Sandbox.Mode.ADD_ALL_FONT:
        try {
          Storage.getSandbox().addAllFont(config);
        } catch (IOException e) {
          this.errMsg = e.getMessage();
          this.errCode = Error.FONT_LOAD;
        }
        break;
      case Sandbox.Mode.COPY:
        try {
          Storage.getSandbox().stationCopy(config);
        } catch (CloneNotSupportedException e) {
          this.errMsg = e.getMessage();
        }
        break;
      case Sandbox.Mode.EDIT:
        try {
          Storage.getSandbox().stationEdit(config);
        } catch (CloneNotSupportedException e) {
          this.errMsg = e.getMessage();
        }
        break;
      case Sandbox.Mode.DELETE:
        Storage.getSandbox().stationDelete(config.getStations());
        break;
      case Sandbox.Mode.OPTIMIZE:
        Storage.getSandbox().optimize(
          config.getOptSaveId(), config.isRefreshCargo(), config.isRefreshFuel());
        break;
      case Sandbox.Mode.UPDATE_NAV:
        Storage.getSandbox().updateNaviComp();
        break;
      case Sandbox.Mode.SAVE:
        try {
          Storage.getSandbox().save(appContext.get(), config.getOverwrite(),
            config.getCompress(), config.getVerCode());
          newFileCreated = !config.getOverwrite();
        } catch (IOException e) {
          this.errMsg = e.getMessage();
        }
        break;
      case Sandbox.Mode.SEND:
        try {
          Utils.copyFile(Storage.getSandbox().getFileName(), config.getFileName());
        } catch (IOException e) {
          this.errMsg = e.getMessage();
        }
        break;
      case Sandbox.Mode.FUEL_INFO:
        try {
          sysMsg = Storage.getSandbox().getFuelInfo();
        } catch (IOException e) {
          this.errMsg = e.getMessage();
        }
        break;
    }

    if (errMsg != null)
      MyLog.e(LOG_TAG, "Error when processing sandbox:\n  " + errMsg);
    return errMsg;
  }

  @Override
  protected void onPostExecute(String result) {
    super.onPostExecute(result);
    MyLog.d(LOG_TAG, "Executing completed");
    publishStatus();
    appContext.clear();
    listener = null;
  }

  private void publishStatus() {
    boolean success = errMsg == null;
    boolean hasMessage = true;

    if (listener != null)
      listener.onTaskEvent(Event.FINISH, success);
    Toast toastMsg = Toast.makeText(appContext.get(), "", Toast.LENGTH_LONG);

    if (success) {
      String action = null;
      switch (mode) {
        case Sandbox.Mode.ADD_MODULE:
          toastMsg.setText(appContext.get().getResources()
            .getQuantityString(R.plurals.sbxProcessing_moduleAdd_success, modCount));
          toastMsg.show();
          return;
        case Sandbox.Mode.ADD_ALL:
          toastMsg.setText(appContext.get().getResources()
            .getQuantityString(R.plurals.sbxProcessing_moduleAdd_success, 10));
          toastMsg.show();
          return;
        case Sandbox.Mode.ADD_TEXT:
        case Sandbox.Mode.ADD_ALL_FONT:
          toastMsg.setText(R.string.sbxProcessing_textAdd_success);
          toastMsg.show();
          return;
        case Sandbox.Mode.OPTIMIZE:
          toastMsg.setText(R.string.sbxProcessing_optimize_success);
          toastMsg.show();
          return;
        case Sandbox.Mode.ADD_COLONY:
          action = appContext.get().getString(R.string.sbxProcessing_add_success);
          break;
        case Sandbox.Mode.COPY:
          action = appContext.get().getString(R.string.sbxProcessing_copy_success);
          break;
        case Sandbox.Mode.EDIT:
          action = appContext.get().getString(R.string.sbxProcessing_edit_success);
          break;
        case Sandbox.Mode.DELETE:
          action = appContext.get().getString(R.string.sbxProcessing_delete_success);
          break;
        case Sandbox.Mode.SAVE:
          toastMsg.setText(R.string.sbxProcessing_save_success);
          toastMsg.show();
          return;
        case Sandbox.Mode.FUEL_INFO:
          toastMsg.setText(appContext.get().getString(
            R.string.sbxProcessing_dataSave_success, sysMsg));
          toastMsg.show();
          return;
        default:
          hasMessage = false;
          break;
      }

      if (hasMessage) {
        String msg = null;
        switch (objType) {
          case Station.Type.STATION:
            msg = appContext.get().getString(R.string.sbxProcessing_station, action);
            break;
          case Station.Type.GROUP:
            msg = appContext.get().getString(R.string.sbxProcessing_group, action);
            break;
          case Station.Type.COLONY:
            msg = appContext.get().getString(R.string.sbxProcessing_colony, action);
            break;
          case Station.Type.TEXT:
            switch (mode) {
              case Sandbox.Mode.COPY:
                msg = appContext.get().getString(R.string.sbxProcessing_textCopy_success);
                break;
              case Sandbox.Mode.EDIT:
                msg = appContext.get().getString(R.string.sbxProcessing_textEdit_success);
                break;
              case Sandbox.Mode.DELETE:
                msg = appContext.get().getString(R.string.sbxProcessing_textDelete_success);
                break;
              default:
                return;
            }
            break;
          case Station.Type.MULTIPLE_OBJECTS:
            switch (mode) {
              case Sandbox.Mode.COPY:
                msg = appContext.get().getString(R.string.sbxProcessing_multipleCopy_success);
                break;
              case Sandbox.Mode.EDIT:
                msg = appContext.get().getString(R.string.sbxProcessing_multipleEdit_success);
                break;
              case Sandbox.Mode.DELETE:
                msg = appContext.get().getString(R.string.sbxProcessing_multipleDelete_success);
                break;
              default:
                return;
            }
            break;
        }
        toastMsg.setText(msg);
        toastMsg.show();
      }
    } else {
      switch (errCode) {
        case Error.OPEN_IO:
          toastMsg.setText(appContext.get().getString(R.string.editErr_sbxLoadIO, errMsg));
          break;
        case Error.PARSER_FAILURE:
          toastMsg.setText(appContext.get().getString(R.string.editErr_sbxParserFailure, lineNum));
          break;
        case Error.FONT_LOAD:
          toastMsg.setText(R.string.editErr_fontLoad);
          break;
        case Error.ILLEGAL_CHAR:
          toastMsg.setText(appContext.get().getString(R.string.editErr_invalidChar, illegalChar));
          break;
        case Error.ALL_LINES_EMPTY:
          toastMsg.setText(R.string.editErr_emptyText);
          break;
        default:
          toastMsg.setText(appContext.get().getString(R.string.sbxProcessing_failed, errMsg));
          break;
      }
      toastMsg.show();
    }
  }

  public boolean isSuccess() {
    return errMsg == null;
  }

  public boolean isNewFileCreated() {
    boolean res = newFileCreated;
    newFileCreated = false;
    return res;
  }

  public interface EventListener {
    void onTaskEvent(int event, boolean success);
  }
}
