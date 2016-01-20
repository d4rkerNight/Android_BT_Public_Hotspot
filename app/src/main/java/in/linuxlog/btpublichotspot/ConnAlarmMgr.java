package in.linuxlog.btpublichotspot;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnAlarmMgr extends BroadcastReceiver{

  @Override
  public void onReceive(Context context, Intent intent){
    //String tag = context.getResources().getString(R.string.logs_tag);
    //String msg = context.getResources().getString(R.string.connAlarmMgr);
    //Log.i(tag, msg);
    //NotificationManager nmgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    ConnService.acquireStaticLock(context);
    context.startService(new Intent(context, ConnTask.class));
  }
}
