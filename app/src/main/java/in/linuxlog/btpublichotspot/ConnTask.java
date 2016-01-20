package in.linuxlog.btpublichotspot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import in.linuxlog.btpublichotspot.BTVariables.Vars;


public class ConnTask extends ConnService{

  public ConnTask(){
    super("ConnTask");
  }

  @Override
  protected void onHandleIntent(Intent intent){

    // Some vars
    String iv;
    Integer result;
    String[] vars = null;
    String[] params = new String[3];

    SharedPreferences pref = getSharedPreferences(Vars.PREF_DATA, Context.MODE_PRIVATE);
    params[0] = pref.getString(Vars.PREF_USERNAME, null);
    params[1] = pref.getString(Vars.PREF_PASSWD, null);
    params[2] = "chk";
    iv = pref.getString(Vars.PREF_IV, null);

    in.linuxlog.btpublichotspot.BTSession conn = new in.linuxlog.btpublichotspot.BTSession();
    if(Vars.var != null){
      vars = BTFunctions.dec(Vars.context, params[0], params[1], Vars.var, iv);
    }
    if(vars != null){
      // Set unencrypted data
      params[0] = vars[0];
      params[1] = vars[1];
      result = conn.checkConn(params);
      Log.i("BT Public Hotspot:: ", "Checking Session...");
    }else{
      Vars.chk = "enc";
      result = 0; // TODO error
    }

    switch(result){
      case 0:
        Log.w("BT Public Hotspot:: ", "...");
        break;
      case 1:
        Log.e("BT Public Hotspot:: ", "Session Error!");
        break;
      case 2:
      case 3:
      case 4:
        Log.i("BT Public Hotspot:: ", "Session Successful!");
        break;
    }

    super.onHandleIntent(intent);
  }
}
