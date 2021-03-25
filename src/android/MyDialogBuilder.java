package com.liveperson.plugin;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * Created by vinh.trinh on 6/9/2017.
 */

public class MyDialogBuilder {
    private Context context;
    private String title, message, positiveText, negativeText;
    private DialogInterface.OnClickListener positiveListener, negativeListener;
    private DialogInterface.OnDismissListener dismissListener;
    private boolean autoDismiss = true, cancelable = true, negativeButtonTextAllCaps = true;
    private boolean single = false;
    String package_name;

    public MyDialogBuilder(Context context, String title, String message) {
        this.context = context;
        this.title = title;
        this.message = message;
    }

    public MyDialogBuilder(Context context, int title, int message) {
        this.context = context;
        if (title != 0)
            this.title = context.getString(title);
        this.message = context.getString(message);
    }

    public MyDialogBuilder positiveText(String val) {
        this.positiveText = val;
        return this;
    }

    public MyDialogBuilder negativeText(String val) {
        this.negativeText = val;
        return this;
    }

    public MyDialogBuilder positiveText(int val) {
        this.positiveText = context.getString(val);
        return this;
    }

    public MyDialogBuilder negativeText(int val) {
        this.negativeText = context.getString(val);
        return this;
    }

    public MyDialogBuilder onPositive(DialogInterface.OnClickListener listener) {
        this.positiveListener = listener;
        return this;
    }

    public MyDialogBuilder onNegative(DialogInterface.OnClickListener listener) {
        this.negativeListener = listener;
        return this;
    }

    public MyDialogBuilder autoDismiss(boolean val) {
        this.autoDismiss = val;
        return this;
    }

    public MyDialogBuilder cancelable(boolean val) {
        this.cancelable = val;
        return this;
    }

    public MyDialogBuilder single(boolean val) {
        this.single = val;
        return this;
    }

    public MyDialogBuilder setNegativeButtonTextAllCaps(boolean val) {
        this.negativeButtonTextAllCaps = val;
        return this;
    }

    public MyDialogBuilder onDismiss(DialogInterface.OnDismissListener listener) {
        this.dismissListener = listener;
        return this;
    }

    public Dialog build() {
        package_name = context.getPackageName();
        final Dialog customDialog = new Dialog(context, context.getResources().getIdentifier("AppDialogTheme", "style", package_name));
        customDialog.setContentView(context.getResources().getIdentifier("custom_alert_dialog", "layout", package_name));
        TextView titleView = customDialog.findViewById( context.getResources().getIdentifier("title", "id", package_name));
        TextView messageView =customDialog.findViewById( context.getResources().getIdentifier("message", "id", package_name));
        Button positiveBtn = customDialog.findViewById(context.getResources().getIdentifier("positive_btn", "id", package_name));
        Button negativeBtn = customDialog.findViewById(context.getResources().getIdentifier("negative_btn", "id", package_name));
        if (TextUtils.isEmpty(title)) {
            titleView.setVisibility(View.GONE);
        } else {
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(title);
        }
        if (TextUtils.isEmpty(message)) {
            messageView.setVisibility(View.GONE);
        } else {
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(message);
        }
        if (single) {
            negativeBtn.setVisibility(View.GONE);
            positiveBtn.setBackground(null);
        }
        if (!TextUtils.isEmpty(positiveText)) positiveBtn.setText(positiveText);
        if (!TextUtils.isEmpty(negativeText)) negativeBtn.setText(negativeText);
        positiveBtn.setOnClickListener(v -> {
            if (positiveListener != null)
                positiveListener.onClick(customDialog, DialogInterface.BUTTON_POSITIVE);
            if (autoDismiss) customDialog.dismiss();
        });
        negativeBtn.setOnClickListener(v -> {
            if (negativeListener != null)
                negativeListener.onClick(customDialog, DialogInterface.BUTTON_NEGATIVE);
            if (autoDismiss) customDialog.dismiss();
        });
        customDialog.setCancelable(cancelable);
        customDialog.setCanceledOnTouchOutside(cancelable);
        if (cancelable && dismissListener != null)
            customDialog.setOnDismissListener(dismissListener);
        if(!negativeButtonTextAllCaps){
            negativeBtn.setAllCaps(false);
        }
        return customDialog;
    }
}
