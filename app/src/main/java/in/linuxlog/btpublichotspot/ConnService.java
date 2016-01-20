package in.linuxlog.btpublichotspot;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class ConnService extends IntentService{

  public static final String LOCK_NAME_STATIC = "";
  public static final String LOCK_NAME_LOCAL = "";
  private static PowerManager.WakeLock lockStatic = null;
  private PowerManager.WakeLock lockLocal = null;

  public ConnService(String name){
    super(name);
  }

  public  static void acquireStaticLock(Context context){
    getLock(context).acquire();
  }

  synchronized private static PowerManager.WakeLock getLock(Context context){
    if(lockStatic == null){
      PowerManager pmgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
      lockStatic = pmgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
      lockStatic.setReferenceCounted(true);
    }
    return(lockStatic);
  }

  @Override
  public void onCreate(){
    super.onCreate();
    PowerManager pmgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
    lockLocal = pmgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_LOCAL);
    lockLocal.setReferenceCounted(true);
  }

  @Override
  public void onStart(Intent intent, final int startId){
    lockLocal.acquire();
    super.onStart(intent, startId);
    getLock(this).release();
  }

  @Override
  protected void onHandleIntent(Intent intent){
    lockLocal.release();
  }
}
