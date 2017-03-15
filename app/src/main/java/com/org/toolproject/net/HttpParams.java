package com.org.toolproject.net;

/**
 * Created by tangjian on 2016/11/1.
 * http Request参数的封装，可以是Get,b也可以是post
 */

/**http Request参数的封装，可以是Get,b也可以是post*/

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**http header参数的封装，*/
public class HttpParams {

    /**
     * 封装 http params
     * */
    private final Map<String, String> mRequestParams = new HashMap<>();
    /**
     * 封装 http header
     * */
    private final Map<String, String> mHeaderParams = new HashMap<>();

    /**
     *获取请求参数某个key对应的value值
     * @param key
     * @return
     */
    public String get(String key) {
        return mRequestParams.get(key);
    }


    /**
     * 设置一个key=value的http参数
     * @param key
     * @param value
     * @return 返回HttpParams本身，便于链式编程
     */
    public HttpParams put(String key,String value){
        mRequestParams.put(key,value);
        return  this;
    }

    /**
     * 获取头部参数某个key对应的value值
     * @param key
     * @return
     */
    public String getHeader(String key){
        return mHeaderParams.get(key);
    }


    /**
     * 设置一个key=value的http参数
     * @param key
     * @param value
     * @return 返回HttpParams本身，便于链式编程
     */
    public HttpParams addHeader(String key ,String value){
        mHeaderParams.put(key,value);
        return this;
    }

    /**
     * 返回一个get请求格式的字符串，如：？age=18&name=seny
     * @return  get请求的字符串结构
     */
    public String toGetParams(){
       StringBuilder builder = new StringBuilder();
        builder.append("?");
        for(Map.Entry<String, String> entry:mRequestParams.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            if(TextUtils.isEmpty(key)||TextUtils.isEmpty(value)){
                continue;
            }
            try {
                builder.append(URLEncoder.encode(key,"UTF-8"));
                builder.append("=");
                builder.append(URLEncoder.encode(value,"UTF-8"));
                builder.append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        String str = builder.toString();
        if(str.length()>1&&str.endsWith("&")){
            str=builder.substring(0,str.length()-1);
        }
        return str;
    }

    /**
     * 返回封装http 请求参数集合
     * @return
     */
    public  Map<String,String> getparams(){
        return mRequestParams;
    }

    /**
     * 返回封装http header的Map集合
     * @return
     */
    public Map<String,String> getHeader(){
        return  mHeaderParams;
    }
}
