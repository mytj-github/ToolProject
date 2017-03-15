package com.org.toolproject.net;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.org.toolproject.utils.FileCopyUtils;
import com.org.toolproject.utils.MD5Utils;

import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by tangjian on 2016/11/1.
 * 网络请求核心类，负责封装Get,post请求(GsonRequest),支持添加自定义Request,初始化RequestQueue及ImagerLoder
 */
public class HttpLoader {

    private static HttpLoader sInstance;

    /**
     * 保存ImageView上正在发起的网络请求
     */
    private final Map<ImageView, ImageLoader.ImageContainer> mImageContainers = new HashMap<>();

    /**
     * 过滤重复请求。保存当前正在消息队列中执行的Request,key为对应的RequestCode.
     */
    private final Map<Integer, Request<?>> mInFlightRequests = new HashMap<>();


    /**
     * 消息对列，全局使用一个
     */
    private RequestQueue mRequestQueue;

    /**
     * 图片加载工具,自定义缓存机制
     */
    private ImageLoader mImageLoader;

    private Context mContext;

    public HttpLoader(Context context) {
        this.mContext = context.getApplicationContext();
        this.mRequestQueue = Volley.newRequestQueue(mContext);
        this.mImageLoader = new ImageLoader(this.mRequestQueue, new VolleyImageCacheImpl(mContext));

    }

    public static synchronized HttpLoader getsInstance(Context context) {

        if (sInstance == null) {
            if (context != null) {
                sInstance = new HttpLoader(context);
            }

        }
        return sInstance;
    }

    public Request<?> addRequest(Request<?> request) {
        if (mRequestQueue != null && request != null) {
            mRequestQueue.add(request);
        }
        return request;
    }

    public Request<?> addRequest(Request<?> request, int requestCode) {
        if (mRequestQueue != null && request != null) {
            mRequestQueue.add(request);
            mInFlightRequests.put(requestCode, request);
        }
        return request;
    }

    public void cancelRequest(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
        Iterator<Map.Entry<Integer, Request<?>>> it = mInFlightRequests.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Request<?>> entry = it.next();
            Object rTag = entry.getValue().getTag();
            if (rTag != null && rTag.equals(tag)) {
                it.remove();
            }
        }
    }


    public ImageLoader getmImageLoader(){
        return  mImageLoader;
    }


    public void display(ImageView view ,String requestUrl){
        display(view,requestUrl,0,0);
    }

    private void display(ImageView view, String requestUrl, int defaultImageResId, int errorImageResId) {
        display(view, requestUrl, defaultImageResId, errorImageResId, view.getWidth(), view.getHeight(), ImageView.ScaleType.FIT_XY);
    }

    public void display(final ImageView view, String requestUrl, final int defaultImageResId, final int errorImageResId, int maxWidth, int maxHeight, ImageView.ScaleType scaleType) {
        if (mImageContainers.containsKey(view)) {//如果已经在给该View请求一张网络图片
            mImageContainers.get(view).cancelRequest();//那么就把之前的取消掉，保证一个ImageView身上只有一个任务。
        }
        ImageLoader.ImageContainer imageContainer = mImageLoader.get(requestUrl, new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorImageResId != 0) {
                    view.setImageResource(errorImageResId);
                    ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).setDuration(800).start();//渐变动画
                }
                mImageContainers.remove(view);//请求失败，移除
            }

            @Override
            public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    view.setImageBitmap(response.getBitmap());
                    ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).setDuration(800).start();//渐变动画
                    mImageContainers.remove(view);//请求成功，移除
                } else if (defaultImageResId != 0) {
                    view.setImageResource(defaultImageResId);
                }
            }
        }, maxWidth, maxHeight);
        mImageContainers.put(view, imageContainer);//将View身上的请求任务进行保存
    }


    private Request<?> request(int method,String url,HttpParams params,Class<? extends IResponse> clazz,final int requestCode,final HttpListener listener,boolean isCache){
        Request request = mInFlightRequests.get(requestCode);
        if(request == null){
            request = makeGsonRequest(method, url, params, clazz, requestCode, listener, isCache);
            //如果是GET请求，则首先尝试解析本地缓存供界面显示，然后再发起网络请求
            if(method == Request.Method.GET){
                tryLoadCacheResponse(request, requestCode, listener);
            }
            return addRequest(request,requestCode);
        }else{
            return  request;
        }
    }


    /**
     * 发送get方式的GsonRequest请求,默认不缓存请求结果,如果想缓存请求结果,请调用{@link #get(String, HttpParams, Class, int, HttpListener, boolean)}
     *
     * @param url         请求地址
     * @param params      GET请求参数，拼接在URL后面。可以为null
     * @param clazz       Clazz类型，用于GSON解析json字符串封装数据
     * @param requestCode 请求码 每次请求对应一个code作为改Request的唯一标识
     * @param listener    处理响应的监听器
     */
    public Request<?> get(String url, HttpParams params, Class<? extends IResponse> clazz, final int requestCode, final HttpListener listener) {
        return request(Request.Method.GET, url, params, clazz, requestCode, listener, false);
    }

    /**
     * 发送get方式的GsonRequest请求
     *
     * @param url         请求地址
     * @param params      GET请求参数，拼接在URL后面。可以为null
     * @param clazz       Clazz类型，用于GSON解析json字符串封装数据
     * @param requestCode 请求码 每次请求对应一个code作为改Request的唯一标识
     * @param listener    处理响应的监听器
     * @param isCache     是否需要缓存本次响应的结果,没有网络时会使用本地缓存
     */
    public Request<?> get(String url, HttpParams params, Class<? extends IResponse> clazz, final int requestCode, final HttpListener listener, boolean isCache) {
        return request(Request.Method.GET, url, params, clazz, requestCode, listener, isCache);
    }

    /**
     * 发送post方式的GsonRequest请求，默认缓存请求结果
     *
     * @param url         请求地址
     * @param params      请求参数，可以为null
     * @param clazz       Clazz类型，用于GSON解析json字符串封装数据
     * @param requestCode 请求码 每次请求对应一个code作为改Request的唯一标识
     * @param listener    处理响应的监听器
     */
    public Request<?> post(String url, HttpParams params, Class<? extends IResponse> clazz, final int requestCode, final HttpListener listener) {
        return request(Request.Method.POST, url, params, clazz, requestCode, listener, false);//POST请求不缓存
    }


    /**
     * 尝试从缓存中读取json数据
     *
     * @param request 要寻找缓存的request
     */
    private void tryLoadCacheResponse(Request request, int requestCode, HttpListener listener) {
        if (listener != null && request != null) {
            try {
                //获取缓存文件
                File cacheFile = new File(mContext.getCacheDir(), "" + MD5Utils.encode(request.getUrl()));
                StringWriter sw = new StringWriter();
                //读取缓存文件
                FileCopyUtils.copy(new FileReader(cacheFile), sw);
                if (request instanceof GsonRequest) {
                    //如果是GsonRequest，那么解析出本地缓存的json数据为GsonRequest
                    GsonRequest gr = (GsonRequest) request;
                    IResponse response = (IResponse) gr.gson.fromJson(sw.toString(), gr.getmClazz());
                    //传给onResponse，让前面的人用缓存数据
                    listener.onGetResponseSuccess(requestCode, response);
                }
            } catch (Exception e) {
            }
        }

    }

    private GsonRequest makeGsonRequest(int method,String url,HttpParams params,Class<? extends IResponse> clazz,int requestCode,HttpListener listener,final boolean isCache ){
        ResponseListener responseListener = new ResponseListener(listener,requestCode);
        Map<String,String> paramsMap=null;
        Map<String,String> headerMap=null;
        if(params!=null){
            if(method== Request.Method.GET){
                url =url+params.toGetParams();//如果是get请求，则把参数拼在url后面
            }else {
                paramsMap = params.getparams();
            }
            headerMap = params.getHeader();
        }
        GsonRequest<IResponse> request = new GsonRequest<>(method, url, paramsMap, headerMap, clazz, responseListener, responseListener, isCache, mContext);
        request.setRetryPolicy(new DefaultRetryPolicy());//设置超时时间，重试次数，重试因子（1,1*2,2*2,4*2）等
        return request;
    }

    /**
     * 成功获取到服务器响应结果的监听，供UI层注册.
     */
    public interface HttpListener {
        /**
         * 当成功获取到服务器响应结果的时候调用
         *
         * @param requestCode response对应的requestCode
         * @param response    返回的response
         */
        void onGetResponseSuccess(int requestCode, IResponse response);

        /**
         * 网络请求失败，做一些释放性的操作，比如关闭对话框
         *
         * @param requestCode 请求码
         * @param error       异常详情
         */
        void onGetResponseError(int requestCode, VolleyError error);
    }


    private  class  ResponseListener implements Response.Listener<IResponse>,Response.ErrorListener{

        private HttpListener listener;
        private int requestCode;

        public ResponseListener(HttpListener listener, int requestCode) {
            this.listener = listener;
            this.requestCode = requestCode;
        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            volleyError.printStackTrace();
            mInFlightRequests.remove(requestCode);
            if(listener!=null){
                listener.onGetResponseError(requestCode,volleyError);
            }

        }

        @Override
        public void onResponse(IResponse iResponse) {
            mInFlightRequests.remove(requestCode);
         //   Log.e("Request success from network!");
            if(iResponse!=null) {
                if (listener != null) {
                    listener.onGetResponseSuccess(requestCode, iResponse);
                }
            }
        }
    }
}
