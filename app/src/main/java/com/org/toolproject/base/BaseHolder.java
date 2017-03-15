package com.org.toolproject.base;

import android.content.Context;
import android.view.View;

/**
 * Created by tangjian on 2016/10/29.
 */
public abstract class BaseHolder<T> {

    public View rootView;
    public Context mContext;

    public BaseHolder(Context context) {
        this.mContext = context;
        this.rootView = initView();
        rootView.setTag(this);
    }

    public Context getContext(){
        return  mContext;
    }

    public abstract View initView();

    public void bindData(T data){

    }
}
