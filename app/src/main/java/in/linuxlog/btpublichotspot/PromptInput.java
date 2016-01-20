package in.linuxlog.btpublichotspot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import in.linuxlog.btpublichotspot.BTVariables.Vars;

public abstract class PromptInput extends AlertDialog.Builder implements DialogInterface.OnClickListener{

  //
  public PromptInput(Context context, String title, String msg, String type){
    super(context);

    setTitle(title);
    setMessage(msg);
    Vars.type = type;

    Vars.inputText = new EditText(context);
    if(type.equals("phrase")){
      Vars.inputText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
      Vars.imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    setView(Vars.inputText);

    setPositiveButton(R.string.ok, this);
    setNegativeButton(R.string.cancel, this);
  }

  // Dismiss input dialog
  public void onCancelClicked(DialogInterface dlg){
    dlg.dismiss();
  }

  @Override
  public void onClick(DialogInterface dlg, int which){
    // Focus on field & open keyboard
    if(which == DialogInterface.BUTTON_POSITIVE){
      if(onOkClicked(Vars.inputText.getText().toString())){
        Vars.input = Vars.inputText.getText().toString();
//        Var2.inputText.requestFocusFromTouch();
//        Var2.imm.showSoftInput(Var2.inputText, 0);
        dlg.dismiss();
        // Alert phrase is wrong
        if(Vars.type.equals("phrase") && Vars.dec){
          BTFunctions.wrongPass();
        }
      }
    }else{
      onCancelClicked(dlg);
    }
  }

  abstract public boolean onOkClicked(String input);
}
