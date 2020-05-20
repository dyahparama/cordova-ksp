package org.apache.cordova.sayang;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static org.apache.cordova.engine.SystemWebViewEngine.TAG;

/**
 * This class echoes a string called from JavaScript.
 */
public class MeidaSayang extends CordovaPlugin {
  private CallbackContext callbackContext;
  DatabaseHelper myDb;
  DatabasePusher pusherDb;

  @Override
  public Bundle onSaveInstanceState() {
    return super.onSaveInstanceState();
  }

  @Override
  public void onResume(boolean multitasking) {
    super.onResume(multitasking);
    int data = cordova.getActivity().getIntent().getIntExtra("zemmy", 100);
    callbackContext.success("lope u" + data + "berhasil");
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    myDb = new DatabaseHelper(cordova.getContext());
    pusherDb = new DatabasePusher(cordova.getContext());
    this.callbackContext = callbackContext;

    if (action.equals("coolMethod")) {
      String param = args.getString(0);
      coolMethod(param);
      return true;
    }
    if (action.equals("insertUser")) {
      String param = args.getString(0);
      insertUser(param);
      return true;
    }
    if (action.equals("deleteUser")) {
      deleteUser();
      return true;
    }
    if (action.equals("getUser")) {
      getUser();
      return true;
    }

    return false;
  }

  private void coolMethod(String message) throws JSONException {
    Intent intentServive = new Intent(cordova.getActivity(), service.class);
    intentServive.putExtra("message",message);
    cordova.getContext().startService(intentServive);
    Intent intent = new Intent(cordova.getActivity(), Receiver.class);
    intent.setAction("org.apache.cordova.sayang.BroadcastReceiver");
    PendingIntent pendingIntent = PendingIntent.getBroadcast(cordova.getActivity(), 1, intent, 0);
    pusherDb.truncateData();
    pusherDb.insertData(message);
    cordova.getActivity().sendBroadcast(intent);
  }

  private void insertUser(String message) {
    boolean isInserted = myDb.insertData(message);
    if (isInserted == true)
      callbackContext.success("success");
    else
      callbackContext.error("failed");
  }
  private void deleteUser() {
    myDb.truncateData();
    callbackContext.success("success");
  }
  private void getUser() {

    JSONArray arr = cur2Json(myDb.getAllData());
    callbackContext.success(arr.toString());
  }
  public JSONArray cur2Json(Cursor cursor) {

    JSONArray resultSet = new JSONArray();
    cursor.moveToFirst();
    while (cursor.isAfterLast() == false) {
      int totalColumn = cursor.getColumnCount();
      JSONObject rowObject = new JSONObject();
      for (int i = 0; i < totalColumn; i++) {
        if (cursor.getColumnName(i) != null) {
          try {
            rowObject.put(cursor.getColumnName(i),
              cursor.getString(i));
          } catch (Exception e) {
            Log.d(TAG, e.getMessage());
          }
        }
      }
      resultSet.put(rowObject);
      cursor.moveToNext();
    }
    cursor.close();
    return resultSet;
  }
}
