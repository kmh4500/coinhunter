package com.redsandbox.treasure.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.ProgressBar;

import com.redsandbox.treasure.R;

public class LoadingDialog extends Dialog {
    public LoadingDialog(Context context) {
        super(context, R.style.custom_dialog_theme);
        this.setContentView(new ProgressBar(context));
    }
}
