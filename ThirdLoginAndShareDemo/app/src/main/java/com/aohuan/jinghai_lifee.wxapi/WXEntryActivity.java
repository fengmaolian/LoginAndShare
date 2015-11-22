package com.aohuan.jinghai_lifee.wxapi;
import com.alibaba.fastjson.JSON;
import com.example.JsonUtil;
import com.example.baseactivity.BaseActivity;
import com.example.bean.WeiXinLoginGetTokenBean;
import com.example.bean.WeiXinLoginGetUserinfoBean;
import com.example.thirdloginandsharedemo.MainActivity;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import java.io.IOException;

import aboutbaidu.AllApk;
/**
 * create by fml
 * time:2015-10-28
 * */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        mContext = this;
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        MainActivity.api.handleIntent(getIntent(), this);
    }  
    @Override  
    public void onReq(BaseReq req) {  
    	
    }
    /** 微信登录第一步：获取code */
    @Override
    public void onResp(BaseResp resp) {
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                String code = ((SendAuth.Resp)resp).code;
                BaseActivity.log("获取code成功："+code);
                if(code != null){
                    new AsynctaskToken().execute("https://api.weixin.qq.com/sns/oauth2/access_token?" + "appid=" + AllApk.WEIXIN_APP_ID + "&secret=" + AllApk.WEIXIN_APP_SECRET + "&grant_type=authorization_code" + "&code=" + code);
                }
                break;
            default:
                break;
        }

    }

    /** 微信登录第二步：获取token */
    class AsynctaskToken extends AsyncTask<Object , Object , Object> {
        @Override
        protected Object doInBackground(Object... params) {
            HttpGet httpRequest = new HttpGet(params[0].toString());
            try{
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(httpRequest);
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    BaseActivity.log("请求个人信息成功");
                    String strResult = EntityUtils.toString(httpResponse.getEntity());
                    return strResult;
                }
                else{
                    BaseActivity.log("请求个人信息失败");
                    return "请求出错";
                }
            }
            catch(ClientProtocolException e){
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Object obj = null;
            try {
                obj = JsonUtil.toObjectByJson(o.toString(), WeiXinLoginGetTokenBean.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            WeiXinLoginGetTokenBean bean = (WeiXinLoginGetTokenBean)obj;
            BaseActivity.log("获取token成功：\n" + "token:"+bean.getAccess_token()+"\nopenid"+bean.getOpenid());
            String url = "https://api.weixin.qq.com/sns/userinfo?"+"access_token="+bean.getAccess_token()+"&openid="+bean.getOpenid();
            new AsynctaskInfo().execute(url);
        }
        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
        }
    }
    /** 微信登录第三步：获取用户信息 */
    class AsynctaskInfo extends AsyncTask<Object , Object , Object> {
        @Override
        protected Object doInBackground(Object... params) {
            HttpGet httpRequest = new HttpGet(params[0].toString());
            try{
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(httpRequest);
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    BaseActivity.log("请求个人信息成功");
                    String strResult = EntityUtils.toString(httpResponse.getEntity());
                    return strResult;
                }
                else{
                    BaseActivity.log("请求个人信息失败");
                    return "请求出错";
                }
            }
            catch(ClientProtocolException e){
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Object obj = null;
            try {
                obj = JsonUtil.toObjectByJson(o.toString(), WeiXinLoginGetUserinfoBean.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            WeiXinLoginGetUserinfoBean bean = (WeiXinLoginGetUserinfoBean)obj;
            BaseActivity.log("获取用户信息成功：\n" + "昵称:"+bean.getNickname()+"\n头像路径"+bean.getHeadimgurl());
            Toast.makeText(mContext,"获取用户信息成功：\n"+"昵称："+bean.getNickname() + "\n头像路径："+bean.getHeadimgurl(),Toast.LENGTH_LONG).show();
            finish();
        }
        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
        }
    }
}
