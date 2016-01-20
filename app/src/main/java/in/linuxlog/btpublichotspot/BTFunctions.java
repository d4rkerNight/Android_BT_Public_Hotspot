package in.linuxlog.btpublichotspot;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import in.linuxlog.btpublichotspot.BTVariables.Vars;

// TODO save iv into different field
public class BTFunctions extends MainActivity{

  // Ask again for phrase input, exit after 3 failed attempts
  public static void wrongPass(){
    Vars.title1 = Vars.context.getResources().getString(R.string.title_wrg_pass);
    if(Vars.msg1 == null){ Vars.msg = Vars.context.getResources().getString(R.string.msg_wrg_left_plur); }
    String ok = Vars.context.getResources().getString(R.string.ok);
    new AlertDialog.Builder(Vars.context)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setTitle(Vars.title1)
      .setMessage((Vars.cntAttempt-- - 1) + " " + Vars.msg)
      .setPositiveButton(ok, new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which){
          if(Vars.cntAttempt != 0){
            Vars.msg1 = Vars.context.getResources().getString(R.string.msg_wrg_left_sing);
            decryptData(Vars.context, Vars.uname, Vars.passwd, Vars.iv);
          }
        }
      })
      .setCancelable(false)
      .show();
  }

  // Decrypt the data
  public static void decryptData(final Context context, final String uname,
                                 final String passwd, final String iv){
    // Some vars
    Vars.context = context;
    Vars.uname = uname;
    Vars.passwd = passwd;
    Vars.iv = iv;

    // Get activity context editText fields
    View viewEmail = ((Activity) context).getWindow().getDecorView().findViewById(R.id.emailText);
    View viewPasswd = ((Activity) context).getWindow().getDecorView().findViewById(R.id.passwdText);
    final EditText emailText = (EditText) viewEmail;
    final EditText passwdText = (EditText) viewPasswd;

    // Get & set title & message from strings.xml
    Vars.title = context.getResources().getString(R.string.enc_title);
    Vars.msg = context.getResources().getString(R.string.dec_msg);

    // Prompt for a secret phrase
    PromptInput pdlg = new PromptInput(context, Vars.title, Vars.msg, "phrase"){
      @Override
      public boolean onOkClicked(String input){
        if(!input.isEmpty()){
          Vars.var = input;
          String[] dataDec = dec(context, uname, passwd, input, iv);
          if(dataDec != null){
            Vars.dec = false;
            // Set editText field with decrypted data
            emailText.setText(dataDec[0], TextView.BufferType.EDITABLE);
            passwdText.setText(dataDec[1], TextView.BufferType.EDITABLE);
          }
        }
        return true;
      }
    };
    pdlg.setCancelable(false);
    pdlg.show();
  }

  public static void encryptData(final Context context, final String uname,
                                 final String passwd){
    // Get & set title & message from strings.xml
    Vars.title = context.getResources().getString(R.string.enc_title);
    Vars.msg = context.getResources().getString(R.string.enc_msg);

    // Prompt for a secret phrase
    PromptInput prompt = new PromptInput(context, Vars.title, Vars.msg, "phrase"){
      @Override
      public boolean onOkClicked(String input){
        Vars.var = input;
        enc(context, uname, passwd, input);
        return true;
      }
    };
    prompt.setCancelable(false);
    prompt.show();
  }

  @Nullable
  public static String[] dec(Context context, String uname, String passwd, String phrase, String iv64){

    // Some vars
    byte[] decrypted;
    String[] decryptedData = new String[2];
    String[] encrypted64 = {uname, passwd};

    try{
      // Setup Encryption
      SecretKey skey = getSecretKey(context, phrase);
      byte[] iv = Base64.decode(iv64, Base64.DEFAULT);
      IvParameterSpec ivSpec = new IvParameterSpec(iv);
      Vars.cipher = Cipher.getInstance(Vars.CIPHER_ALGO);
      Vars.cipher.init(Cipher.DECRYPT_MODE, skey, ivSpec);
      for(int i = 0; i < 2; i++){
        decrypted = Vars.cipher.doFinal(Base64.decode(encrypted64[i], Base64.DEFAULT));
        decryptedData[i] = new String(decrypted, "UTF-8");
      }
    }catch(Exception e){
      e.printStackTrace();
      Vars.stackTrace = Log.getStackTraceString(e);
      //writeLog(context, Var1.stackTrace);
      Log.e("BT Decryption Error: ", e.toString());
      return null;
    }
    return decryptedData;
  }

  private static void enc(Context context, String uname, String passwd, String phrase){

    // some vars
    byte[] user;
    byte[] pass;
    String[] encryptedData = new String[3];
    final ProgressDialog progressDialog;
    progressDialog = new ProgressDialog(context);

    try{
      // Setup Encryption
      SecretKey skey = getSecretKey(context, phrase);
      byte[] iv = generateIV(context);
      //Cipher
      assert iv != null;
      IvParameterSpec ivSpec = new IvParameterSpec(iv);
      Vars.cipher = Cipher.getInstance(Vars.CIPHER_ALGO);
      Vars.cipher.init(Cipher.ENCRYPT_MODE, skey, ivSpec);
      // Encrypt Data
      user = Vars.cipher.doFinal(uname.getBytes("UTF-8"));
      pass = Vars.cipher.doFinal(passwd.getBytes("UTF-8"));
      encryptedData[0] = Base64.encodeToString(iv, Base64.DEFAULT);
      encryptedData[1] = Base64.encodeToString(user, Base64.DEFAULT);
      encryptedData[2] = Base64.encodeToString(pass, Base64.DEFAULT);
    }catch(InvalidKeyException | UnsupportedEncodingException |
        NoSuchAlgorithmException | NoSuchPaddingException |
        IllegalBlockSizeException | BadPaddingException |
        InvalidAlgorithmParameterException e){
      // TODO error
      e.printStackTrace();
      Vars.stackTrace = Log.getStackTraceString(e);
      ErrorsReport.writeLog(context, Vars.stackTrace);
      Log.e("BT Encrypt Error: ", e.toString());
    }
    // Write encrypted data to pref file
    SharedPreferences pref = context.getSharedPreferences(Vars.PREF_DATA, MODE_PRIVATE);
    SharedPreferences.Editor editor = pref.edit();
    editor.putString(Vars.PREF_IV, encryptedData[0]);
    editor.putString(Vars.PREF_USERNAME, encryptedData[1]);
    editor.putString(Vars.PREF_PASSWD, encryptedData[2]);
    editor.apply();
    Vars.msg = context.getResources().getString(R.string.msg_saved_sess);
    progressDialog.setMessage(Vars.msg);
    progressDialog.show();
    dismissTimed(progressDialog);
  }

  @Nullable
  private static SecretKey getSecretKey(Context context, String phrase){
    try{
      PBEKeySpec keySpec = new PBEKeySpec(phrase.toCharArray(), Vars.Salt.getBytes("UTF-8"), Vars.PBE_CNT, Vars.PBE_KEY_LEN);
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(Vars.SEC_KEY_ALGO);
      SecretKey key = keyFactory.generateSecret(keySpec);
      SecretKey skey = new SecretKeySpec(key.getEncoded(), Vars.KEY_ALGO);
      return skey;
    }catch(Exception e){
      e.printStackTrace();
      Vars.stackTrace = Log.getStackTraceString(e);
      ErrorsReport.writeLog(context, Vars.stackTrace);
      Log.e("BT Error: ", e.toString());
      return null;
    }
  }

  // Generate IV
  @Nullable
  private static byte[] generateIV(Context context){
    try{
      SecureRandom random = SecureRandom.getInstance(Vars.ALGO);
      byte[] iv = new byte[Vars.IV_LEN];
      random.nextBytes(iv);
      return iv;
    }catch(Exception e){
      e.printStackTrace();
      Vars.stackTrace = Log.getStackTraceString(e);
      ErrorsReport.writeLog(context, Vars.stackTrace);
      Log.e("BT genIV Error: ", e.toString());
      return null;
    }
  }

  // AlertMsg empty field
  public static void emptyFields(Context context, Integer integer){
    Vars.title = context.getResources().getString(R.string.title_empty_field);
    Vars.ok = context.getResources().getString(R.string.ok);
    // Get activity context editText fields
    View viewEmail = ((Activity) context).getWindow().getDecorView().findViewById(R.id.emailText);
    View viewPasswd = ((Activity) context).getWindow().getDecorView().findViewById(R.id.passwdText);
    final EditText emailText = (EditText) viewEmail;
    final EditText passwdText = (EditText) viewPasswd;
    // Set keyboard
    final InputMethodManager inputManager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
    if(integer == 0){
      Vars.msg = context.getResources().getString(R.string.msg_invalid_empty_field);
      new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(Vars.title)
        .setMessage(Vars.msg)
        .setPositiveButton(Vars.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            emailText.post(new Runnable() {
              @Override
              public void run() {
                // Focus on field & open keyboard
                emailText.requestFocusFromTouch();
                inputManager.showSoftInput(emailText, 0);
              }
            });
          }
        })
        .show();
    }else if(integer == 1){
      Vars.msg = context.getResources().getString(R.string.msg_email_empty_field);
      new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(Vars.title)
        .setMessage(Vars.msg)
        .setPositiveButton(Vars.ok, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialog, int which){
            emailText.post(new Runnable(){
              @Override
              public void run(){
                // Focus on field & open keyboard
                emailText.requestFocusFromTouch();
                inputManager.showSoftInput(emailText, 0);
              }
            });
          }
        })
        .show();
    }else if(integer == 2){
      Vars.msg = context.getResources().getString(R.string.msg_passwd_empty_field);
      new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(Vars.title)
        .setMessage(Vars.msg)
          .setPositiveButton(Vars.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              passwdText.post(new Runnable() {
                @Override
                public void run() {
                  // Focus on field & open keyboard
                  passwdText.requestFocusFromTouch();
                  inputManager.showSoftInput(passwdText, 0);
                }
              });
            }
          })
          .show();
    }
  }

  // Email field textWatcher
  public static boolean checkEmail(final Context context){
    View viewEmail = ((Activity) context).getWindow().getDecorView().findViewById(R.id.emailText);
    final EditText emailText = (EditText) viewEmail;
    emailText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        // If focus is lost check email syntax
        emailText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
          @Override
          public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
              // Check email syntax
              isValidEmail(context, emailText);
            }
          }
        });
      }
    });
    return !emailText.getText().toString().isEmpty();
  }

  // Check email for emptiness field
  @NonNull
  public static Integer isValidEmail(Context context, EditText chk_email){
    if(chk_email.getText().toString().isEmpty()){
      Vars.msg = context.getResources().getString(R.string.msg_email_empty_field);
      chk_email.setError(Vars.msg);
      return 1;
    }else if(!isEmailValid(chk_email.getText().toString())){
      Vars.msg = context.getResources().getString(R.string.msg_invalid_empty_field);
      chk_email.setError(Vars.msg);
      return 0;
    }else{
      return 2;
    }
  }

  // Check email syntax
  static boolean isEmailValid(CharSequence val_email){
    return Patterns.EMAIL_ADDRESS.matcher(val_email).matches();
  }

  // Start Service
  public static void startAlarmMgr(Context context){
    Intent alarmIntent;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    int interval = 60000; // 1 min
    alarmIntent = new Intent(context, ConnAlarmMgr.class);
    pendingIntent = PendingIntent.getBroadcast(context, 0,
      alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
      interval, pendingIntent);
  }

  // Stop Service
  public static void stopAlarmMgr(Context context){
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    // stop AlarmManager & quit app
    Intent alarmIntent = new Intent(context, ConnAlarmMgr.class);
    pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
    alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    alarmManager.cancel(pendingIntent);
  }

  // Confirm reset Data
  public static void confirmReset(final Context context){
    String title = context.getResources().getString(R.string.title_confirm);
    String msg = context.getResources().getString(R.string.msg_confirm);
    // Reset data alertDialog
    new AlertDialog.Builder(context)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setTitle(title)
      .setMessage(msg)
      .setPositiveButton(Vars.yes, new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which){
          stopAlarmMgr(context);
          SettingToolbar.resetShared(context);
        }})
      .setNegativeButton(Vars.no, null)
      .show();
  }

  //
  public static void dismissTimed(final ProgressDialog pdlg){
    Timer timer = new Timer();
    TimerTask task = new TimerTask(){
      @Override
      public void run(){
        pdlg.dismiss();
      }
    };
    timer.schedule(task, 1919);
  }

  //
  public static void enablingWifi(Context context){
    ProgressDialog progressDialog;
    Vars.msg = context.getResources().getString(R.string.wait_enabling_wifi);
    progressDialog = new ProgressDialog(context);
    progressDialog.setMessage(Vars.msg);
//    progressDialog.setCancelable(false);
    progressDialog.show();
  }
}
