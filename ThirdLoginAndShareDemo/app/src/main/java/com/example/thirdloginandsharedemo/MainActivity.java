package com.example.thirdloginandsharedemo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaUser;
import com.baidu.frontia.api.FrontiaAuthorization;
import com.baidu.frontia.api.FrontiaAuthorizationListener;
import com.example.baseactivity.AhView;
import com.example.baseactivity.BaseActivity;
import com.mob.tools.utils.UIHandler;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import aboutbaidu.AllApk;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import com.example.Util;
/**
 * 第三方登录
 *
 * */
@AhView(R.layout.activity_main)
public class MainActivity extends BaseActivity implements PlatformActionListener,Handler.Callback {
    @InjectView(R.id.m_third_login_result)
    TextView mThirdLoginResult;
    //----------------------新浪微博授权获取用户信息相关------------------------
    private static final int MSG_TOAST = 1;
    private static final int MSG_ACTION_CCALLBACK = 2;
    private static final int MSG_CANCEL_NOTIFY = 3;
    private Platform mPf;
    //----------------------------百度第三方登录相关---------------------------------
    private FrontiaAuthorization mAuthorization;
    private final static String Scope_Basic = "basic";
    private final static String Scope_Netdisk = "netdisk";
    //------------------------------QQ第三方登录相关---------
    private UserInfo mInfo;
    private static Tencent mTencent;
    /** IWXAPI 是第三方app和微信通信的openapi接口 */
    private static boolean isServerSideLogin = false;
    private String openid = null;


    //---------------------------微信第三方相关
    public static IWXAPI api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        regToWx();
        initView();
    }
    private void initView() {
        //注册百度
        mAuthorization = Frontia.getAuthorization();// Tencent类是SDK的主要实现类，开发者可通过Tencent类访问腾讯开放的OpenAPI。
        // 其中APP_ID是分配给第三方应用的appid，类型为String。
        mTencent = Tencent.createInstance(AllApk.QQ_APP_ID, this.getApplicationContext());
//        mInfo = new UserInfo(this, MainActivity.mTencent.getQQToken());
    }
    @OnClick({R.id.m_third_sina_login , R.id.m_third_sina_exit ,  R.id.m_third_qq_login , R.id.m_third_qq_exit ,
            R.id.m_third_weixin_login , R.id.m_third_weixin_exit , R.id.m_third_baidu_login , R.id.m_third_baidu_exit})
    void onClick(View view){
        switch (view.getId()){
            /** 新浪微博第三方登录 */
            case R.id.m_third_sina_login:
                thirdSinaLogin();
                break;
            /** 新浪微博第三方登录退出 */
            case R.id.m_third_sina_exit:
                mPf = ShareSDK.getPlatform(MainActivity.this, SinaWeibo.NAME);
                //如果要删除授权信息，重新授权
                mPf.getDb().removeAccount();
                mThirdLoginResult.setText("退出登录");
                break;
            /** QQ第三方登录 */
            case R.id.m_third_qq_login:
                loginQQ();
                break;
            /** QQ第三方退出 */
            case R.id.m_third_qq_exit:
                mTencent.logout(this);
                mThirdLoginResult.setText("退出登录");
                break;
            /** 微信第三方登录 */
            case R.id.m_third_weixin_login:
                if(!api.isWXAppInstalled()){
                    mToast("请安装微信客户端之后再进行登录");
                    return;
                }
                getCode();
                break;
            /** 微信第三方退出 */
            case R.id.m_third_weixin_exit:

                break;
            /** 百度第三方登录 */
            case R.id.m_third_baidu_login:
                baiduLogin();
                break;
            /** 百度第三方退出 */
            case R.id.m_third_baidu_exit:
                baiduExit();
                break;
            default:
                break;
        }
    }
    //-----------------------------------------------------新浪微博授权相关-----------------------
    /** 新浪微博授权、获取用户信息页面 */
    private void thirdSinaLogin() {
        //初始化新浪平台
        Platform pf = ShareSDK.getPlatform(MainActivity.this, SinaWeibo.NAME);
        pf.SSOSetting(true);
        //设置监听
        pf.setPlatformActionListener(MainActivity.this);
        //获取登陆用户的信息，如果没有授权，会先授权，然后获取用户信息
        pf.authorize();
    }
    /** 新浪微博授权成功回调页面 */
    @Override
    public void onComplete(Platform platform, int action, HashMap<String, Object> hashMap) {
        /** res是返回的数据，例如showUser(null),返回用户信息，对其解析就行
         *   http://sharesdk.cn/androidDoc/cn/sharesdk/framework/PlatformActionListener.html
         *   1、不懂如何解析hashMap的，可以上网搜索一下
         *   2、可以参考官网例子中的GetInforPage这个类解析用户信息
         *   3、相关的key-value,可以看看对应的开放平台的api
         *     如新浪的：http://open.weibo.com/wiki/2/users/show
         *     腾讯微博：http://wiki.open.t.qq.com/index.php/API%E6%96%87%E6%A1%A3/%E5%B8%90%E6%88%B7%E6%8E%A5%E5%8F%A3/%E8%8E%B7%E5%8F%96%E5%BD%93%E5%89%8D%E7%99%BB%E5%BD%95%E7%94%A8%E6%88%B7%E7%9A%84%E4%B8%AA%E4%BA%BA%E8%B5%84%E6%96%99
         *
         */
        Message msg = new Message();
        msg.what = MSG_ACTION_CCALLBACK;
        msg.arg1 = 1;
        msg.arg2 = action;
        msg.obj = platform;
        UIHandler.sendMessage(msg, this);
    }
    /** 取消授权 */
    @Override
    public void onCancel(Platform platform, int action) {
        Message msg = new Message();
        msg.what = MSG_ACTION_CCALLBACK;
        msg.arg1 = 3;
        msg.arg2 = action;
        msg.obj = platform;
        UIHandler.sendMessage(msg, this);
    }
    /** 授权失败 */
    @Override
    public void onError(Platform platform, int action, Throwable t) {
        t.printStackTrace();
        t.getMessage();
        Message msg = new Message();
        msg.what = MSG_ACTION_CCALLBACK;
        msg.arg1 = 2;
        msg.arg2 = action;
        msg.obj = t;
        UIHandler.sendMessage(msg, this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch(msg.what) {
            case MSG_TOAST: {
                String text = String.valueOf(msg.obj);
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_ACTION_CCALLBACK: {
                switch (msg.arg1) {
                    case 1: {
                        // 成功, successful notification
                        //授权成功后,获取用户信息，要自己解析，看看oncomplete里面的注释
                        //ShareSDK只保存以下这几个通用值
                        Platform pf = ShareSDK.getPlatform(MainActivity.this, SinaWeibo.NAME);
                        Log.e("sharesdk use_id", pf.getDb().getUserId()); //获取用户id
                        Log.e("sharesdk use_name", pf.getDb().getUserName());//获取用户名称
                        Log.e("sharesdk use_icon", pf.getDb().getUserIcon());//获取用户头像
                        mThirdLoginResult.setText("授权成功"+"\n"+"用户id:" + pf.getDb().getUserId() + "\n" + "获取用户名称" + pf.getDb().getUserName() + "\n" + "获取用户头像" + pf.getDb().getUserIcon());
                        //mPf.author()这个方法每一次都会调用授权，出现授权界面
                        //如果要删除授权信息，重新授权
                        //mPf.getDb().removeAccount();
                        //调用后，用户就得重新授权，否则下一次就不用授权
                    }
                    break;
                    case 2: {
                        mThirdLoginResult.setText("登录失败");
                    }
                    break;
                    case 3: {
                        // 取消, cancel notification
                        mThirdLoginResult.setText("取消授权");
                    }
                    break;
                }
            }
            break;
            case MSG_CANCEL_NOTIFY: {
                NotificationManager nm = (NotificationManager) msg.obj;
                if (nm != null) {
                    nm.cancel(msg.arg1);
                }
            }
            break;
        }
        return false;
    }
    // 在状态栏提示分享操作,the notification on the status bar
    private void showNotification(long cancelTime, String text) {
        try {
            Context app = getApplicationContext();
            NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
            final int id = Integer.MAX_VALUE / 13 + 1;
            nm.cancel(id);
            long when = System.currentTimeMillis();
            Notification notification = new Notification(R.mipmap.ic_launcher, text, when);
            PendingIntent pi = PendingIntent.getActivity(app, 0, new Intent(), 0);
            notification.setLatestEventInfo(app, "sharesdk test", text, pi);
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            nm.notify(id, notification);
            if (cancelTime > 0) {
                Message msg = new Message();
                msg.what = MSG_CANCEL_NOTIFY;
                msg.obj = nm;
                msg.arg1 = id;
                UIHandler.sendMessageDelayed(msg, cancelTime, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //----------------------------------百度登录相关--------------------------------
    /**
     * 百度第三方登录相关
     */
    protected void baiduLogin() {
        ArrayList<String> scope = new ArrayList<String>();
        scope.add(Scope_Basic);
        scope.add(Scope_Netdisk);
        mAuthorization.authorize(this, FrontiaAuthorization.MediaType.BAIDU.toString(), scope, new FrontiaAuthorizationListener.AuthorizationListener() {
            @Override
            public void onSuccess(FrontiaUser result) {
                if (null != mThirdLoginResult) {
                    mThirdLoginResult.setText("social id: " + result.getId() + "\n" + "token: " + result.getAccessToken() + "\n" + "expired: " + result.getExpiresIn());
                }
            }
            @Override
            public void onFailure(int errCode, String errMsg) {
                if (null != mThirdLoginResult) {
                    mThirdLoginResult.setText("errCode:" + errCode + ", errMsg:" + errMsg);
                }
            }
            @Override
            public void onCancel() {
                if (null != mThirdLoginResult) {
                    mThirdLoginResult.setText("cancel");
                }
            }
        });
    }


    /** ------------------------QQ第三方登录-------------------- */
    public void loginQQ(){
        /** 判断是否登陆过 */
        if (!mTencent.isSessionValid()){
            mTencent.login(this, "all",loginListener);
        }/** 登陆过注销之后在登录 */
        else {
            mTencent.logout(this);
            mTencent.login(this, "all",loginListener);
        }
    }
    IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject values) {
            initOpenidAndToken(values);
            updateUserInfo();
        }
    };
    /** QQ登录第二步：存储token和openid */
    public static void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires) && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
            }
        } catch(Exception e) {
        }
    }
    /** QQ登录第三步：获取用户信息 */
    private void updateUserInfo() {
        if (mTencent != null && mTencent.isSessionValid()) {
            IUiListener listener = new IUiListener() {
                @Override
                public void onError(UiError e) {
                    Message msg = new Message();
                    msg.obj = "把手机时间改成获取网络时间";
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                }

                @Override
                public void onComplete(final Object response) {
                    Message msg = new Message();
                    msg.obj = response;
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                }
                @Override
                public void onCancel() {
                    Message msg = new Message();
                    msg.obj = "获取用户信息失败";
                    msg.what = 2;
                    mHandler.sendMessage(msg);
                }
            };
            mInfo = new UserInfo(this, mTencent.getQQToken());
            mInfo.getUserInfo(listener);
        } else {

        }
    }
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                JSONObject response = (JSONObject) msg.obj;
                if (response.has("nickname")) {
                    try {
                        log("获取用户信息成功，返回结果："+response.toString());
                        mThirdLoginResult.setText("登录成功\n"+"昵称:"+response.getString("nickname")+"\n头像地址:"+response.get("figureurl_qq_1"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }else if(msg.what == 1){
                mThirdLoginResult.setText(msg+"");
            }else if(msg.what == 2){
                mThirdLoginResult.setText(msg+"");
            }
        }

    };
    /** QQ登录第一步：获取token和openid */
    private class BaseUiListener implements IUiListener {
        @Override
        public void onComplete(Object response) {
            if (null == response) {
                mToast("登录失败");
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                mToast("登录失败");
                return;
            }
            log("QQ登录成功返回结果-" + response.toString());
            doComplete((JSONObject)response);
        }
        protected void doComplete(JSONObject response) {}
        @Override
        public void onError(UiError e) {
            Util.toastMessage(MainActivity.this, "onError: " + e.errorDetail);
            Util.dismissDialog();
        }
        @Override
        public void onCancel() {
            Util.toastMessage(MainActivity.this, "onCancel: ");
            Util.dismissDialog();
            if (isServerSideLogin) {
                isServerSideLogin = false;
            }
        }
    }
    /** -------------------------QQ第三方登录结束-------------------- */

    /** -------------------------微信第三方登录---------------------- */
    /**
     *
     * 微信平台应用授权登录接入代码示例
     *
     * */
    private void regToWx(){
        // 通过WXAPIFactory工厂,获得IWXAPI的实例
        api = WXAPIFactory.createWXAPI(MainActivity.this, AllApk.WEIXIN_APP_ID, true);
        // 将应用的appid注册到微信
        api.registerApp(AllApk.WEIXIN_APP_ID);
    }
    //获取微信访问getCode
    private void getCode(){
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "carjob_wx_login";
        api.sendReq(req);
    }
    /** -------------------------微信第三方登录结束-------------------- */
    /**
     * 百度第三方退出相关
     */
    protected void baiduExit() {
        boolean result = mAuthorization.clearAuthorizationInfo(FrontiaAuthorization.MediaType.BAIDU.toString());
        if (result) {
            mThirdLoginResult.setText("百度退出成功");
        } else {
            mThirdLoginResult.setText("百度退出失败");
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShareSDK.stopSDK(this);
    }
}
