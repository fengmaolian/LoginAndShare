package com.example.baseactivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import com.baidu.frontia.Frontia;
import com.example.thirdloginandsharedemo.R;

import aboutbaidu.AllApk;
import butterknife.ButterKnife;
import cn.sharesdk.framework.ShareSDK;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES;

public abstract class BaseActivity extends FragmentActivity {
    private static Context context;
    private static long toastNextTime;
    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        context = this; //??
//		setContentView(setLayoutAoHuan());
        try {
            setContentView(InjectHelper.injectObject(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ButterKnife.inject(BaseActivity.this);
        Frontia.init(this.getApplicationContext(), AllApk.BAI_DU_KEY);
        //Mob平台授权
        ShareSDK.initSDK(this);
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.title_bg);//通知栏所需颜色
        }
    }
    //	public abstract int setLayoutAoHuan();
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
    public void mIntent(Class cla) {
        Intent intent = new Intent(getApplicationContext(), cla);
        startActivity(intent);
    }
    public static void mToast(String toast) {
        if (System.currentTimeMillis() - toastNextTime > 1000){
            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
            toastNextTime = System.currentTimeMillis();
        }
    }
    public static void mToast(View layout,String toast) {
        ToastUtils.toast(layout, toast, context);
    }
    public static void log(String toast) {
        Log.i("TAG", toast);
    }
    public void mStartIntent(Class<?> cla) {
        Intent intent = new Intent(context, cla);
        startActivity(intent);
    }
}
