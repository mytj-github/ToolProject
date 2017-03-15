package com.org.toolproject.ui;

import android.content.Context;

import com.org.toolproject.net.HttpLoader;
import com.org.toolproject.net.HttpParams;
import com.org.toolproject.utils.Assert;

/**
 * Created by tangjian on 2017/3/14.
 */
public class Api {

    private static  Api sInstance;
    private HttpLoader mLoader;


    private  Api(Context context){
         this.mLoader = HttpLoader.getsInstance(context);
    }

    private  Api() {
    }
    /**
     * 返回单例对象
     *
     * @param context   上下文
     * @return  返回单例对象
     */
    public static synchronized Api getInstance(Context context) {
        if (sInstance == null) {
            Assert.notNull(context);
            sInstance = new Api(context);
        }
        return sInstance;
    }

    public void CancleRequest(Object tag){
        if(mLoader!=null){
            mLoader.cancelRequest(tag);
        }

    }


    /**
     * 生成一个HttpParams 对象，默认包含通用参数信息
     *
     * @return  返回生成的HttpParams对象
     */
    public HttpParams generateHttpParams() {
        HttpParams params = new HttpParams();
        params.addHeader("myHeader1", "xxx");//根据实际需求，在这里初始化通用Header设置参数
        params.addHeader("myHeader2", "xxx");
        params.addHeader("myHeader3", "xxx");
        params.addHeader("myHeader4", "xxx");

        return params;
    }


}
