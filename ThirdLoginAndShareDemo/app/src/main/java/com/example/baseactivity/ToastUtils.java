package com.example.baseactivity;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.example.thirdloginandsharedemo.R;


/**
 * Created by Administrator on 2015/9/26.
 */
public class ToastUtils {
    public static void toast(View layout,String toast, Context context) {
        Snackbar snackbar= Snackbar.make(layout, toast, Snackbar.LENGTH_SHORT);
        setSnackbarMessageTextColor(snackbar, Color.parseColor("#ffffff"));
        snackbar.getView().setBackgroundColor(context.getResources().getColor( android.R.color.holo_red_dark ));
        snackbar.show();
    }

    public static void setSnackbarMessageTextColor(Snackbar snackbar, int color) {
        View view = snackbar.getView();
        ((TextView) view.findViewById(R.id.snackbar_text)).setTextColor(color);
    }
}
