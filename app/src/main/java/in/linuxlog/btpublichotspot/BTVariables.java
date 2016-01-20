package in.linuxlog.btpublichotspot;

import android.content.Context;
import android.os.Handler;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import javax.crypto.Cipher;

public class BTVariables{

  static class Vars{
    public static String iv;
    public static String username;
    public static String password;
    public static Context context;
    public static Context mActivityContext;
    public static Boolean enc = false;
    public static Boolean dec = false;
    public static String type;
    public static String input;
    public static EditText inputText;
    public static InputMethodManager imm;
    public static String title, title1;
    public static String msg, msg1;
    public static String var;
    public static String uname;
    public static String passwd;
    public static String setLang;
    public static String chk;
    public static String stackTrace = "";
    public static Integer cntAttempt = 3;
    public static String[] achk = {"", "", "chk"};
    public static Handler handler;
    public static EditText emailText = null;
    public static EditText passwdText = null;
    public static String no;
    public static String yes;
    public static String ssid;
    public static Integer res;
    public static Integer initCheck = 0;
    public static TextView ssidText;
    public static String tag;
    public static String ok;
    public static String link = "https://www.btopenzone.com:8443/home";
    public static String Salt = "03xy9z52twq8r4s1uv67"; // TODO ask for salt
    public static String CIPHER_ALGO = "AES/CBC/PKCS5Padding";
    public static String KEY_ALGO = "AES";
    public static String SEC_KEY_ALGO = "PBEWithSHA256And256BitAES-CBC-BC";
    public static String ALGO = "SHA1PRNG";
    public static Integer PBE_CNT = 100;
    public static Integer PBE_KEY_LEN = 256;
    public static Integer IV_LEN = 16;
    public static Cipher cipher;

    // SharedPreferences constants
    public static String PREF_LANG = "language";
    public static String PREF_LOCALE = "locale";
    public static String PREF_DATA = "loginData";
    public static String PREF_IV = "iv";
    public static String PREF_USERNAME = "uname";
    public static String PREF_PASSWD = "passwd";
  }
}
