package com.rufus.shredmachine.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.rufus.shredmachine.ShredMachineApplication;

/**
 * Created by rufuszhu on 15-09-13.
 */
public class KeyBoard {
    public static void closeKeyBoard(EditText myEditText) {
        InputMethodManager imm = (InputMethodManager) ShredMachineApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
    }

    public static void openKeyBoard(EditText myEditText) {
        InputMethodManager inputMethodManager = (InputMethodManager) ShredMachineApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(myEditText.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
    }
}
