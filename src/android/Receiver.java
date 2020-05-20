package org.apache.cordova.sayang;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class Receiver extends BroadcastReceiver {
  Class mainActivity;

  public Receiver() {

  }

  @Override
  public void onReceive(Context context, Intent intent) {
    String  packageName = context.getPackageName();
    Intent  launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
    String  className = launchIntent.getComponent().getClassName();
    try {
      mainActivity = Class.forName(className);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Intent i = new Intent(context, mainActivity);
    i.putExtra("zemmy",1);
    context.startActivity(i);
  }
}
