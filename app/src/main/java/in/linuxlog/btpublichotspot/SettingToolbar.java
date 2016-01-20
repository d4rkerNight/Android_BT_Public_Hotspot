package in.linuxlog.btpublichotspot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import in.linuxlog.btpublichotspot.BTVariables.Vars;

public class SettingToolbar extends MainActivity{

  public static void resetShared(Context context){

    Vars.ok = context.getResources().getString(R.string.ok);
    Vars.title = context.getResources().getString(R.string.title_info);

    SharedPreferences remPref = context.getSharedPreferences(Vars.PREF_DATA, 0);
    if(remPref.edit().clear().commit()){
      Vars.msg = context.getResources().getString(R.string.msg_res_data);
    }else{
      Vars.msg = context.getResources().getString(R.string.msg_res_data_err);
    }
    new AlertDialog.Builder(context)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setTitle(Vars.title)
      .setMessage(Vars.msg)
      .setPositiveButton(Vars.ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        }
      })
      .show();
  }
}
