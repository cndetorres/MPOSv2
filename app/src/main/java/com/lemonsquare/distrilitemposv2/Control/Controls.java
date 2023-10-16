package com.lemonsquare.distrilitemposv2.Control;

import android.content.Context;
import android.content.DialogInterface;

public class Controls {


    public static void  messagebox(String alerttext,Context context,String header) {

        new android.support.v7.app.AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(header)
                .setMessage(alerttext)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }

                })
                .show();
    }
}
