package com.org.toolproject.ui;

import com.org.toolproject.net.IResponse;

import java.util.List;

/**
 * Created by tangjian on 2017/3/14.
 */
public class WeatherResponse implements IResponse {

    public int errno;
    //public String request_id;
   // public String timestamp;

    public DataEntity data;

    public class DataEntity{

        public String image;
        public List<WeatherEntity> weather;

        public class WeatherEntity{

            public String time;
            public String weather;
            public String temperature;
            public String date;
            public Pm25Entity pm25;


            public class Pm25Entity{
                public String value;
                public String level;
                public String levelnum;
            }
        }
    }

}
