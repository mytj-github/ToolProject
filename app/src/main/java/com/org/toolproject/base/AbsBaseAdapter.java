package com.org.toolproject.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tangjian on 2016/10/29.
 */
public abstract class AbsBaseAdapter<T> extends BaseAdapter {

    private Context mContext;
    private List<T> data = new ArrayList<>();


    public AbsBaseAdapter(Context mContext, List<T> data) {
        this.mContext = mContext;
        this.data = data;
    }

    public  Context getContext(){
        return  mContext;
    }

    public List<T> getData(){
        return data;
    }

    public void notifyDataSetChanged(List<T> mData) {
        this.data = mData;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public T getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseHolder holder =null;
        if(convertView!=null){
            holder = (BaseHolder)convertView.getTag();
        }else{
            holder = onCreateViewHolder(parent,getItemViewType(position));
        }
           holder.bindData(getItem(position));
        return holder.rootView;
    }

    protected abstract BaseHolder onCreateViewHolder(ViewGroup parent, int viewType);
}
