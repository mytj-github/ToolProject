package com.org.toolproject.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.org.toolproject.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public List<Persons> data = new ArrayList<Persons>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for(int i=0;i<5;i++){
            Persons p = new Persons();
            p.setName("第"+i+"名");
            p.setSex("男"+i);
            p.setAge(i);
            data.add(p);
        }
        initData(data);
    }

    public void initData(List<Persons> data){


        ListView mListview = (ListView) findViewById(R.id.listView);
        mListview.setAdapter(new PersonsAdapter(this,data));
    }
}
