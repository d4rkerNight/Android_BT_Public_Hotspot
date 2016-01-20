package in.linuxlog.btpublichotspot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import in.linuxlog.btpublichotspot.BTVariables.Vars;

public class SettingActivity extends Activity{

  @Override
  protected void onCreate(Bundle savedInstance){
    super.onCreate(savedInstance);
    setContentView(R.layout.activity_setting);

    RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
    final RadioButton rb_en = (RadioButton) findViewById(R.id.rad_english);
    final RadioButton rb_it = (RadioButton) findViewById(R.id.rad_italian);
    final SharedPreferences pref = getSharedPreferences(Vars.PREF_LANG, Context.MODE_PRIVATE);
    Vars.setLang = pref.getString(Vars.PREF_LOCALE, null);
    if(Vars.setLang == null || Vars.setLang.equals("en")){
      rb_en.setChecked(true);
    }else{
      rb_it.setChecked(true);
    }
    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId){
        // Set language
        Configuration lang = new Configuration();
        if(checkedId == R.id.rad_english){
          pref.edit().putString("locale", "en").apply();
        }else{
          pref.edit().putString("locale", "it").apply();
        }
        onConfigurationChanged(lang);
      }
    });
  }

  @Override
  public void onConfigurationChanged(Configuration lang){
    super.onConfigurationChanged(lang);
    getBaseContext().getResources().updateConfiguration(lang, getBaseContext().getResources().getDisplayMetrics());
    System.exit(0);
    startActivity(new Intent(this, MainActivity.class));
  }
}
