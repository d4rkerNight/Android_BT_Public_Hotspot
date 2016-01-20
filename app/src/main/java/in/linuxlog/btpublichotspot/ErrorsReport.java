package in.linuxlog.btpublichotspot;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.provider.Settings.Secure;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import in.linuxlog.btpublichotspot.BTVariables.Vars;

public abstract class ErrorsReport{

  // TODO check if the file is bigger than 1MB, move to ErrorsReport.java
  // Write stack trace to log file
  public static void writeLog(Context context, String stackTrace){
    // Get system time
    Long time = System.currentTimeMillis() / 1000;
    String timeStamp = time.toString();
    String path = context.getFilesDir().getPath() + "/";
    String filename = "logcatBT-WiFi.log";
    Integer fileSize = 0;
    File file = new File(path + filename);
    if(file.exists()){
      // Get file size
      fileSize = Integer.parseInt(String.valueOf(file.length() / (1024 * 1024)));
    }
    FileOutputStream outputStream;
    try{
      if(fileSize > 1){
        // delete file content if it's bigger than 1MB
        PrintWriter deleteContent = new PrintWriter(path + filename);
        deleteContent.print("");
        deleteContent.close();
      }
      outputStream = context.openFileOutput(filename, Context.MODE_APPEND);
      outputStream.write("\n".getBytes());
      outputStream.write(timeStamp.getBytes());
      outputStream.write(stackTrace.getBytes());
    }catch(IOException e){
      Log.e("BT writeLog Error: ", e.toString());
      stackTrace = Log.getStackTraceString(e);
      alertReport(context, stackTrace);
    }
    alertReport(context, stackTrace);
  }

  // Alert if user wants to send error log
  public static void alertReport(final Context context, final String stackTrace){
    Vars.title = context.getResources().getString(R.string.title_send_log);
    Vars.msg = context.getResources().getString(R.string.msg_send_log);
    // Display alertDialog error
    new AlertDialog.Builder(context)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setTitle(Vars.title)
      .setMessage(Vars.msg)
      .setPositiveButton(Vars.yes, new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which){
          sendError(context, stackTrace);
        }})
      .setNegativeButton(Vars.no, null)
      .show();
  }

  // Send stack trace via email client
  public static void sendError(Context context, String stackTrace){
    // Get android id for the email subject
    String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("message/rfc822");
    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"d4rk3rl33t@gmail.com"});
    intent.putExtra(Intent.EXTRA_SUBJECT, androidId);
    intent.putExtra(Intent.EXTRA_TEXT, stackTrace);
    try{
      Vars.msg = context.getResources().getString(R.string.msg_send_email);
      context.startActivity(Intent.createChooser(intent, Vars.msg));
    }catch(ActivityNotFoundException e){
      Vars.msg = context.getResources().getString(R.string.msg_email_no_client);
      Toast.makeText(context, Vars.msg, Toast.LENGTH_SHORT).show();
    }
  }
}
