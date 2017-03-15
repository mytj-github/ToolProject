package com.org.toolproject.net;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;
import com.org.toolproject.utils.MD5Utils;

import java.io.File;
import java.io.IOException;

/**
 * Created by tangjian on 2016/11/1.
 */
public class VolleyImageCacheImpl implements ImageLoader.ImageCache {

    //磁盘缓存大小
    private static final int DISK_MAX_SIZE=10*1024*1024;//默认10M
    //缓存类
    private LruCache<String,Bitmap> mLruCache;
    private DiskLruCache mDiskLruCache;


    public VolleyImageCacheImpl(Context context) {
       //获取应用可占内存的1/8作为缓存
        int maxsize = (int) (Runtime.getRuntime().maxMemory()/8);
        mLruCache = new LruCache<String, Bitmap>(maxsize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                //测量bitmap的大小
                return bitmap.getRowBytes()*bitmap.getHeight() ;
            }
        };

        try {
            mDiskLruCache = DiskLruCache.open(getDiskCacheDir(context,"bitmaps"),getAppVersion(context),1,DISK_MAX_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 该方法会判断当前sd卡是否存在，然后选择缓存地址
     *
     * @param context
     * @return
     */
    public File getDiskCacheDir(Context context,String uniqueName){
        String cachePath;
        File externalCacheDir = context.getExternalCacheDir();
        if((Environment.MEDIA_MOUNTED.contentEquals(Environment.getExternalStorageState())||!Environment.isExternalStorageRemovable())
                &&externalCacheDir!=null){
             cachePath =externalCacheDir.getPath();
        }else{
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath+ File.separator+uniqueName);
    }


    /**
     * 从缓存（内存缓存，磁盘缓存）中获取Bitmap
     */
    @Override
    public Bitmap getBitmap(String url) {
        if(mLruCache.get(url)!=null){
            //从缓存中取出
            return mLruCache.get(url);
        }else{
            String key = MD5Utils.encode(url);
            try {
                if(mDiskLruCache!=null && mDiskLruCache.get(key)!=null){
                    DiskLruCache.Snapshot  snapshot = mDiskLruCache.get(key);
                    Bitmap bitmap = null;
                    if(snapshot!=null){
                        bitmap = BitmapFactory.decodeStream(snapshot.getInputStream(0));
                        mLruCache.put(url,bitmap);
                        return bitmap;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void putBitmap(String s, Bitmap bitmap) {

    }

    /**
     * 获取应用版本号
     *
     * @param context
     * @return
     */
    public int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }
}
