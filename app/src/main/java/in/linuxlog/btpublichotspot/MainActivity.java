package in.linuxlog.btpublichotspot;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static in.linuxlog.btpublichotspot.BTVariables.Vars;

// TODO
// * add setting activity
// * add to menu exit
// * after logged in ask to open app
// * test TextView instead of processDialog
// * remember password phrase in memory for the service to run or
//    use the service to decrypt the data, but still
//    you need to remember the phrase entered and later the salt when included
// * add info text: which SSID is connected
// * add custom icons
// * add option to buy credits "open browser to url"
// * add button to ask again to decrypt pass if any
// * move any global vars or Var* classes to BTVariables Vars class
// * add session status
/** **/
public class MainActivity extends AppCompatActivity{

  @Override
  protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    // Check language
    SharedPreferences prefLang = getSharedPreferences(Vars.PREF_LANG, MODE_PRIVATE);
    Vars.setLang = prefLang.getString(Vars.PREF_LOCALE, null);
    Configuration config = new Configuration();
    if(Vars.setLang == null || Vars.setLang.equals("en")){
      Locale locale = new Locale("en");
      Locale.setDefault(locale);
      config.locale = Locale.ENGLISH;
    }else{
      Locale locale = new Locale("it");
      Locale.setDefault(locale);
      config.locale = Locale.ITALIAN;
    }
    getBaseContext().getResources().updateConfiguration(config,
      getBaseContext().getResources().getDisplayMetrics());
    setContentView(R.layout.activity_main);
//    Vars.mActivityContext = this;
    // Get text from resource string
    Vars.no = this.getResources().getString(R.string.no);
    Vars.ok = this.getResources().getString(R.string.ok);
    Vars.yes = this.getResources().getString(R.string.yes);
    Vars.tag = this.getResources().getString(R.string.logs_tag);
    // Check Wifi/SSID state with BroadcastReceiver
    this.registerReceiver(this.wifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
    this.registerReceiver(this.ssidStateReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
    // fill up login field if SharedPreferences
    // variables are set & ask to decrypt the data
    SharedPreferences pref = getSharedPreferences(Vars.PREF_DATA, MODE_PRIVATE);
    Vars.iv = pref.getString(Vars.PREF_IV, null);
    Vars.username = pref.getString(Vars.PREF_USERNAME, null);
    Vars.password = pref.getString(Vars.PREF_PASSWD, null);
    // Get editText field
    Vars.emailText = (EditText) findViewById(R.id.emailText);
    Vars.passwdText = (EditText) findViewById(R.id.passwdText);
    Vars.passwdText.setTypeface(Typeface.DEFAULT);
    if(Vars.username != null && Vars.password != null && Vars.iv != null){
      Vars.dec = true;
      BTFunctions.decryptData(this, Vars.username, Vars.password, Vars.iv);
    }
    btnSignIn();
    Vars.handler = new Handler();
  }

  // SSID state & set the text accordingly
  public BroadcastReceiver ssidStateReceiver = new BroadcastReceiver(){
    @Override
    public void onReceive(Context context, Intent intent){
      Vars.ssidText = new TextView(MainActivity.this);
      Vars.ssidText = (TextView) findViewById(R.id.ssidText);
      WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
      WifiInfo wifiInfo = wifiManager.getConnectionInfo();
      Vars.ssid = wifiInfo.getSSID();
      Vars.ssidText.setText(Vars.ssid);
      if(Vars.ssid.equals("\"BTWiFi-with-FON\"") || Vars.ssid.equals("\"BTWiFi\"") ||
          Vars.ssid.equals("\"BTWifi-with-FON\"") && Vars.initCheck == 0){
        new NetworkAsyncTask().execute(Vars.achk);
      }
    }
  };

  // Wifi state
  public BroadcastReceiver wifiStateReceiver = new BroadcastReceiver(){
    @Override
    public void onReceive(Context context, Intent intent){
      int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
        WifiManager.WIFI_STATE_UNKNOWN);
      switch(extraWifiState){
        case WifiManager.WIFI_STATE_DISABLED:
          disabledWifi();
          break;
        case WifiManager.WIFI_STATE_DISABLING: // TODO
          break;
        case WifiManager.WIFI_STATE_ENABLED:
          if(checkSSID()){
            // Check connection status
            new NetworkAsyncTask().execute(Vars.achk);
          }
          //Toast.makeText(context, "TESTING...", Toast.LENGTH_SHORT).show();
          BTFunctions.checkEmail(context);
          break;
        case WifiManager.WIFI_STATE_ENABLING: // TODO
          BTFunctions.enablingWifi(context);
          break;
        case WifiManager.WIFI_STATE_UNKNOWN: // TODO
          break;
      }
    }
  };

  private void checkSuccess(boolean success){
    if(success){
      Runnable runnable = new Runnable(){
        String uname = Vars.emailText.getText().toString();
        String passwd = Vars.passwdText.getText().toString();

        @Override
        public void run(){
          Vars.handler.post(new Runnable(){
            @Override
            public void run(){
              if(Vars.enc){
                BTFunctions.encryptData(MainActivity.this, uname, passwd);
              }
            }
          });
        }
      };
      new Thread(runnable).start();
      Log.d(Vars.tag, "checkSuccess..."); // testing
    }
  }

  // Button behaviour
  private void btnSignIn(){
    // Check SSID, emailText & passwdText fields
    Button btn_signin = (Button) findViewById(R.id.btn_signin);
    btn_signin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (checkSSID()) {
          if (BTFunctions.isValidEmail(MainActivity.this, Vars.emailText) == 0) {
            // Alert if email is invalid
            BTFunctions.emptyFields(MainActivity.this, 0);
          } else if (BTFunctions.isValidEmail(MainActivity.this, Vars.emailText) == 1) {
            // Alert if email is empty
            BTFunctions.emptyFields(MainActivity.this, 1);
          } else {
            if (!Vars.passwdText.getText().toString().isEmpty()) {
              // Check if session is established
              checkSession();
            } else {
              // Alert if password is empty
              BTFunctions.emptyFields(MainActivity.this, 2);
            }
          }
        }
      }
    });
  }

  // Create Session
  private void checkSession(){
    Vars.initCheck = 1;
    String sess = "sess";
    String empty = "empty";
    Intent alarmIntent;
    SharedPreferences pref = getSharedPreferences(Vars.PREF_DATA, MODE_PRIVATE);
    Vars.username = pref.getString(Vars.PREF_USERNAME, null);
    Vars.password = pref.getString(Vars.PREF_PASSWD, null);
    // If null get User values
    // else use SharedPreferences values
    if(Vars.res == 0 || Vars.res == 1){
      if(Vars.username == null && Vars.password == null){
        new NetworkAsyncTask().execute(Vars.emailText.getText().toString(),
          Vars.passwdText.getText().toString(), empty);
      }else{
        if(Vars.dec){
          BTFunctions.decryptData(this, Vars.username, Vars.password, Vars.iv);
        }else{
          new NetworkAsyncTask().execute(Vars.emailText.getText().toString(), Vars.passwdText.getText().toString(), sess);
        }
      }
    }else{
      new NetworkAsyncTask().execute(Vars.achk);
    }
    // PendingIntent to perform a broadcast
    alarmIntent = new Intent(MainActivity.this, ConnAlarmMgr.class);
    PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
  }

  // TODO add SSID whitelist
  // Check hard coded SSID or open wifi option
  private boolean checkSSID(){
    Vars.ssidText = new TextView(this);
    Vars.ssidText = (TextView) findViewById(R.id.ssidText);
    String title = this.getResources().getString(R.string.title_sel_SSID);
    String msg = this.getResources().getString(R.string.msg_sel_SSID);
    WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    Vars.ssid = wifiInfo.getSSID();
    if(Vars.ssid.equals("\"BTWiFi-with-FON\"") || Vars.ssid.equals("\"BTWiFi\"") ||
      Vars.ssid.equals("\"BTWifi-with-FON\"")){
      return true;
    }else{
      new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(title)
        .setMessage(msg)
        .setPositiveButton(Vars.ok, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialog, int which){
            // Open wifi option to select SSID
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
          }
        })
        .show();
    }
    return false;
  }

  // Exit app or enable wifi
  private void disabledWifi(){
    String title = this.getResources().getString(R.string.title_dis_wifi);
    String msg = this.getResources().getString(R.string.msg_dis_wifi);
    final String toast = this.getResources().getString(R.string.toast_dis_wifi);
    final WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
    new AlertDialog.Builder(this)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setTitle(title)
      .setMessage(msg)
      .setPositiveButton(Vars.yes, new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which){
          wifiManager.setWifiEnabled(true);
        }
      })
      .setNegativeButton(Vars.no, new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which){
          BTFunctions.stopAlarmMgr(MainActivity.this);
          // Unregister Receiver
          MainActivity.this.unregisterReceiver(wifiStateReceiver);
          MainActivity.this.unregisterReceiver(ssidStateReceiver);
          finish();
          Toast.makeText(MainActivity.this, toast, Toast.LENGTH_LONG).show();
        }
      })
      .show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu){
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item){
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    switch(item.getItemId()){
      case R.id.ask_dec:
        // Ask again to decrypt data if any
        item.setEnabled(false);
        if(Vars.username != null && Vars.password != null && Vars.iv != null){
          Vars.dec = true;
          item.setEnabled(true);
          BTFunctions.decryptData(this, Vars.username, Vars.password, Vars.iv);
        }
        return true;
      case R.id.action_settings:
        // Setting Activity
        Intent intentSetting = new Intent(this, SettingActivity.class);
        startActivity(intentSetting);
        return true;
      case R.id.resetShared:
        // Stop service & reset data
        BTFunctions.confirmReset(this);
        return true;
      case R.id.set_exit:
        // Exit app
        onBackPressed();
        return true;
      case R.id.buy_credits:
        // Open browser to buy credits
        Intent intentBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(Vars.link));
        startActivity(intentBrowser);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed(){
    Vars.title = this.getResources().getString(R.string.title_cls_app);
    Vars.msg = this.getResources().getString(R.string.msg_cls_app);
    Vars.yes = this.getResources().getString(R.string.yes);
    final String toast = this.getResources().getString(R.string.toast_dis_wifi);
    // Exit app alertDialog & stop service
    new AlertDialog.Builder(this)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setTitle(Vars.title)
      .setMessage(Vars.msg)
      .setPositiveButton(Vars.yes, new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which){
          BTFunctions.stopAlarmMgr(MainActivity.this);
          // Unregister Receiver
          MainActivity.this.unregisterReceiver(wifiStateReceiver);
          MainActivity.this.unregisterReceiver(ssidStateReceiver);
          finish();
          Toast.makeText(MainActivity.this, toast, Toast.LENGTH_LONG).show();
        }})
      .setNegativeButton(Vars.no, null)
      .show();
  }

  //
  // Async task: establishing connection
  //
  class NetworkAsyncTask extends AsyncTask<String, Void, Integer>{

    private ProgressDialog progressDialog;

//    private ProgressBar progressBar;
//    private Context mContext;
//    public NetworkAsyncTask(Context context){
//      mContext = context;
//    }

    @Override
    protected Integer doInBackground(String... params){
      try{
        Looper.prepare();
        return new BTSession().checkConn(params);
      }catch(Exception e){
        Vars.stackTrace = Log.getStackTraceString(e);
        return 5;
      }
    }
    @Override
    protected void onPreExecute(){
      super.onPreExecute();
      Vars.msg = MainActivity.this.getResources().getString(R.string.wait);
      progressDialog = new ProgressDialog(MainActivity.this);
      progressDialog.setMessage(Vars.msg);
      progressDialog.setCancelable(false);
      progressDialog.show();
    }
    @Override
    protected void onPostExecute(Integer result){
      super.onPostExecute(result);
      String incorrect = MainActivity.this.getResources().getString(R.string.incorrect);
      String logged = MainActivity.this.getResources().getString(R.string.logged_in);
      String already = MainActivity.this.getResources().getString(R.string.already_log);
      // Dismiss onPreExecute progress dialog
      if(progressDialog.isShowing()){
        progressDialog.dismiss();
      }
      Vars.res = result;
      progressDialog = new ProgressDialog(MainActivity.this);
      switch(result) {
        case 0:
          // TODO not logged in
          //BTFunctions.startAlarmMgr(MainActivity.this);
          Vars.enc = false;
          break;
        case 1:
          progressDialog.setMessage(incorrect);
          progressDialog.show();
          dismissTimed(result);
          break;
        case 2:
          Vars.enc = false;
          progressDialog.setMessage(logged);
          progressDialog.show();
          BTFunctions.startAlarmMgr(MainActivity.this);
          dismissTimed(result);
          break;
        case 3:
          // TODO ask for decrypt if the data is saved
          // so if true start service
          //BTFunctions.startAlarmMgr(MainActivity.this); // testing
          Vars.enc = !Vars.emailText.getText().toString().isEmpty() && !Vars.passwdText.getText().toString().isEmpty() && Vars.username == null && Vars.password == null && Vars.iv == null;
          progressDialog.setMessage(already);
          progressDialog.show();
          // remove start service, this is a test
          BTFunctions.startAlarmMgr(MainActivity.this);
          dismissTimed(result);
          break;
        case 4:
          Vars.enc = true;
          BTFunctions.startAlarmMgr(MainActivity.this);
          dismissTimed(result);
          break;
        case 5:
          // TODO errors
          ErrorsReport.writeLog(MainActivity.this, Vars.stackTrace);
          break;
      }
    }

    private void dismissTimed(final Integer integer){
      Timer timer = new Timer();
      TimerTask task = new TimerTask(){
        @Override
        public void run(){
          progressDialog.dismiss();
          // Ask for encryption phrase
          switch(integer){
           // case 0: // remove only testing
            //case 1: // remove only testing
            //case 3: // remove only testing
            case 2:
            case 3:
            case 4:
              Looper.prepare();
              checkSuccess(true);
              break;
          }
        }
      };
      timer.schedule(task, 1919);
    }
  }
}
