package com.example;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import cn.sharesdk.framework.TitleLayout;
import cn.sharesdk.framework.authorize.AuthorizeAdapter;

import static com.mob.tools.utils.R.getStringRes;


public class MyAdapter extends AuthorizeAdapter{

    public void onCreate() {
        //隐藏标题栏的 “Powered by ShareSDK”
        hideShareSDKLogo();

        //获取标题栏控件
//        TitleLayout llTitle = getTitleLayout();
        //隐藏整个标题栏
//		llTitle.setVisibility(View.GONE);

        //标题栏的文字修改
//        int resID= getStringRes(getActivity(), "second_title");//这个字段定义在strings.xml文件里面
//        llTitle.getTvTitle().setText(resID);

        //标题栏背景颜色的修改(默认是一张黑色背景的图片,我们修改成灰色背景)
//        llTitle.setBackgroundColor(Color.GRAY);

        //标题栏文字居中
//        llTitle.getTvTitle().setGravity(Gravity.CENTER);

        //返回按钮的隐藏
//		llTitle.getChildAt(0).setVisibility(View.GONE);

        //隐藏返回按钮后面的黑线
//		llTitle.getChildAt(1).setVisibility(View.GONE);

        //标题文字的隐藏  方法一
//		llTitle.getChildAt(2).setVisibility(View.GONE);

        //标题文字的隐藏  方法二
//		llTitle.getTvTitle().setVisibility(View.GONE);

        //使授权页面弹出时的动画失效
//		disablePopUpAnimation();

        //页面弹出时动画的修改
        disablePopUpAnimation();
        View rv = (View) getBodyView().getParent();
        TranslateAnimation ta = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, -1,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        ta.setDuration(1000);//动画时间
        rv.setAnimation(ta);

    }

}
