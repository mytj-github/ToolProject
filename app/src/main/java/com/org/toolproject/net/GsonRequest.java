package com.org.toolproject.net;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.org.toolproject.utils.FileCopyUtils;
import com.org.toolproject.utils.MD5Utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by tangjian on 2017/3/5.
 */
public class GsonRequest<T> extends Request<T> {
    public final Gson gson = new Gson();
    private final Class<? extends T> mClazz;
    private final Map<String,String> mParams;
    private final Map<String,String> mHeaders;
    private final Response.Listener<T> mListener;
    private boolean mIsCache;
    private Context mContext;


    public GsonRequest(int method, String url, Map<String, String> params, Map<String, String> headers, Class<? extends T> clazz,
                       Response.Listener<T> listener, Response.ErrorListener errorListener, boolean isCache, Context context) {
        super(method, url, errorListener);
        mClazz = clazz;
        mParams = params;
        mHeaders = headers;
        mListener = listener;
        mIsCache = isCache;
        mContext = context;
    }


    public Class<? extends T> getmClazz() {
        return mClazz;
    }

    public Map<String, String> getmParams() {
        return mParams;
    }

    public Map<String, String> getmHeaders() throws AuthFailureError {
        return mHeaders==null? super.getHeaders():mHeaders;
    }

    @Override
    protected void deliverResponse(T response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            T result = gson.fromJson(json, mClazz);//按正常响应解析
            if (mIsCache) {
                //如果解析成功，并且需要缓存则将json字符串缓存到本地
                FileCopyUtils.copy(response.data, new File(mContext.getCacheDir(), "" + MD5Utils.encode(getUrl())));
            }
            return Response.success(
                    result,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        } catch (IOException e) {
            return Response.error(new ParseError(e));
        }
    }
}
