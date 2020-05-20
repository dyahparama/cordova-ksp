package org.apache.cordova.sayang;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.json.JSONException;
import org.mge.fusion.app.R;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;

public class service extends Service {
  private NotificationManager notifManager;
  Context context = this;
  PusherOptions options;
  Pusher pusher;
  Channel channel;
  Class mainActivity;
  Resources resources;
  String packageName;

  @Override
  public IBinder onBind(Intent intent) {

    return null;
  }

  @Override
  public void onCreate() {
    packageName = context.getPackageName();
    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
    String className = launchIntent.getComponent().getClassName();
    resources = context.getResources();
    try {
      mainActivity = Class.forName(className);
    } catch (Exception e) {
      e.printStackTrace();
    }
    DatabasePusher dbPusher = new DatabasePusher(context);
    Cursor p = dbPusher.getAllData();
    Helper h = new Helper();
    if (p.getCount()>0){
      p.moveToFirst();
      try {
        String cluster = h.strToJSON(p.getString(1),"cluster");
        String apikey = h.strToJSON(p.getString(1),"apikey");
        String event = h.strToJSON(p.getString(1),"event");
        String channelNm = h.strToJSON(p.getString(1),"channelNm");
        //pusher("ap1","e327dc39f0ae164632ea","my-channel","test-event");
        pusher(cluster,apikey,event,channelNm);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }


  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }
  private void pusher(String cluster,String apikey, String channelNm, String event)  {
    options = new PusherOptions();
    options.setCluster(cluster);
    pusher = new Pusher(apikey, options);
    pusher.connect();
    channel = pusher.subscribe(channelNm);
    channel.bind(event, new SubscriptionEventListener() {
      @Override
      public void onEvent(PusherEvent event) {

        DatabaseHelper myDb;
        Helper h = new Helper();
        myDb = new DatabaseHelper(context);
        Cursor c = myDb.getAllData();
        if (c.getCount()>0){
          c.moveToFirst();
          try {
            String dataLocal = h.strToJSON(c.getString(1),"uuid");
            String dataNotif = h.strToJSON(event.getData(),"uuid");
            String userLocal = h.strToJSON(c.getString(1),"id");
            String userNotif = h.strToJSON(event.getData(),"id");
            if (!dataLocal.equals(dataNotif) && userLocal.equals(userNotif)){
              myDb.truncateData();
              createNotification("Akun yg saat ini aktif telah digunakan di device lain", context);
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
          //createNotification(event.getData(), context);
        }

      }
    });


  }

  public void createNotification(String aMessage, Context context) {
    final int NOTIFY_ID = 0; // ID of notification
    String id = "zemmuwa"; // default_channel_id
    String title = "test"; // Default Channel
    Intent intent;
    PendingIntent pendingIntent;
    NotificationCompat.Builder builder;
    if (notifManager == null) {
      notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      int importance = NotificationManager.IMPORTANCE_HIGH;
      NotificationChannel mChannel = notifManager.getNotificationChannel(id);
      if (mChannel == null) {
        mChannel = new NotificationChannel(id, title, importance);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notifManager.createNotificationChannel(mChannel);
      }
      builder = new NotificationCompat.Builder(context, id);
      //intent = new Intent(context, MainActivity.class);
      intent = new Intent(context, Receiver.class);
      intent.setAction("org.apache.cordova.sayang.BroadcastReceiver");
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtra("pushnotification", "yes");
      Log.d("isiIntent", intent.hasExtra("pushnotification") + "");
      //pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      builder.setContentTitle(aMessage)                            // required
        .setSmallIcon(resources.getIdentifier("ic_launcher", "mipmap", packageName))   // required
        //.setSmallIcon(R.mipmap.ic_launcher)
        .setContentText(context.getString(resources.getIdentifier("app_name", "string", packageName))) // required
        //.setContentText(context.getString(R.string.app_name))

        .setDefaults(Notification.DEFAULT_ALL)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setTicker(aMessage)
        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
    } else {
      builder = new NotificationCompat.Builder(context, id);
      intent = new Intent(context, mainActivity);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
      builder.setContentTitle(aMessage)                            // required
        //.setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
        .setContentText(context.getString(resources.getIdentifier("app_name", "string", packageName))) // required
        .setContentText(context.getString(R.string.app_name))
        .setDefaults(Notification.DEFAULT_ALL)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setTicker(aMessage)
        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
        .setPriority(Notification.PRIORITY_HIGH);
    }
    Notification notification = builder.build();
    notifManager.notify(NOTIFY_ID, notification);
  }
}
