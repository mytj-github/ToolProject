package com.org.toolproject.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.org.toolproject.R;
import com.org.toolproject.base.AbsBaseAdapter;
import com.org.toolproject.base.BaseHolder;

import java.util.List;

/**
 * Created by tangjian on 2016/10/30.
 */
public class PersonsAdapter extends AbsBaseAdapter<Persons> {

    public Context mContext;

    public PersonsAdapter(Context mContext, List<Persons> data) {
        super(mContext, data);
        this.mContext = mContext;
    }

    @Override
    protected BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PersonsHolder(mContext);
    }

    class PersonsHolder extends BaseHolder<Persons> {

        TextView tv_name;
        TextView tv_sex;
        TextView tv_age;

        public PersonsHolder(Context context) {
            super(context);
        }

        @Override
        public View initView() {
            View view = View.inflate(getContext(), R.layout.persons_adpter_layout, null);
            tv_name = (TextView) view.findViewById(R.id.name);
            tv_sex = (TextView) view.findViewById(R.id.sex);
            tv_age = (TextView) view.findViewById(R.id.age);
            return view;
        }

        @Override
        public void bindData(Persons data) {
            tv_name.setText(data.getName());
            tv_sex.setText(data.getSex());
            tv_age.setText(String.valueOf(data.getAge()));
        }
    }

}
