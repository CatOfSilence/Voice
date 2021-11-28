package com.example.voicecat.Service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.example.voicecat.Adapter.ListAdapter;
import com.example.voicecat.Bean.SongInfo;
import com.example.voicecat.R;
import com.example.voicecat.Utils.MySqlite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dongzhong on 2018/5/30.
 */

public class FloatingListService extends Service {

    //判断服务是否已开启
    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View view;
    //判断是否隐藏view
    private boolean onDisPlay = false;

    //数据库
    private MySqlite mySqlite;
    private SQLiteDatabase db;
    //存储listview信息
    private List<SongInfo> mData;
    private ListAdapter adapter;
    //音乐播放器
    private MediaPlayer mediaPlayer;


    //开始触控的坐标，移动时的坐标（相对于屏幕左上角的坐标）
    private int mTouchStartX, mTouchStartY, mTouchCurrentX, mTouchCurrentY;
    //开始时的坐标和结束时的坐标（相对于自身控件的坐标）
    private int mStartX, mStartY, mStopX, mStopY;
    //判断悬浮窗口是否移动，这里做个标记，防止移动后松手触发了点击事件
    private boolean isMove;
    private int mFloatWinWidth, mFloatWinHeight;//悬浮窗的宽高

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        //计算得出悬浮窗口的宽高
        DisplayMetrics metric = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metric);
        int screenWidth = metric.widthPixels;
        mFloatWinWidth = screenWidth * 1 / 3;
        mFloatWinHeight = mFloatWinWidth * 4 / 3;

        //针对不同android版本
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        //窗口基本配置
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 150;
        layoutParams.height = 150;
        layoutParams.x = metric.widthPixels;
        layoutParams.y = 300;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    private void showFloatingWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                view = LayoutInflater.from(this).inflate(R.layout.floating_service, null);
                ListView listview = view.findViewById(R.id.listview_service);
                ImageView imageView = view.findViewById(R.id.iv_service);
                LinearLayout linear = view.findViewById(R.id.linear_service);
                linear.setVisibility(View.GONE);
                windowManager.addView(view, layoutParams);

                mData = new ArrayList<>();
                mediaPlayer = new MediaPlayer();

                adapter = new ListAdapter(this, mData);
                listview.setAdapter(adapter);
                updataListView();

                imageView.setOnTouchListener(new FloatingOnTouchListener());

                //实时更新
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);
                        updataListView();
                    }
                };
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            handler.sendEmptyMessage(0);
                        }
                    }
                }).start();

                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mediaPlayer.reset();
                        try {
                            mediaPlayer.setDataSource(mData.get(position).SongPath);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                //展开，收缩
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!onDisPlay) {
                            linear.setVisibility(View.VISIBLE);
                            onDisPlay = !onDisPlay;
                            layoutParams.width = 500;
                            layoutParams.height = 800;
                        } else {
                            linear.setVisibility(View.GONE);
                            onDisPlay = !onDisPlay;
                            layoutParams.width = 150;
                            layoutParams.height = 150;
                            mediaPlayer.pause();
                        }
                        windowManager.updateViewLayout(view, layoutParams);
                    }
                });
            }
        }
    }

    public void updataListView() {
        //更新ListView
        mySqlite = new MySqlite(this);
        db = mySqlite.getWritableDatabase();

        mData.clear();
        Cursor cursor = db.query(MySqlite.TABLE_Collection,
                new String[]{
                        MySqlite.SongName,
                        MySqlite.SongSinger,
                        MySqlite.SongLength,
                        MySqlite.SongPath},
                null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                SongInfo songInfo = new SongInfo();
                songInfo.SongName = cursor.getString(cursor.getColumnIndex(MySqlite.SongName));
                songInfo.SongSinger = cursor.getString(cursor.getColumnIndex(MySqlite.SongSinger));
                songInfo.SongLength = cursor.getInt(cursor.getColumnIndex(MySqlite.SongLength));
                songInfo.SongPath = cursor.getString(cursor.getColumnIndex(MySqlite.SongPath));
                mData.add(songInfo);
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
        db.close();
    }
    //滑动监听
    private class FloatingOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    isMove = false;
                    mTouchStartX = (int) event.getRawX();
                    mTouchStartY = (int) event.getRawY();
                    mStartX = (int) event.getX();
                    mStartY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mTouchCurrentX = (int) event.getRawX();
                    mTouchCurrentY = (int) event.getRawY();
                    layoutParams.x += mTouchCurrentX - mTouchStartX;
                    layoutParams.y += mTouchCurrentY - mTouchStartY;
                    windowManager.updateViewLayout(view, layoutParams);

                    mTouchStartX = mTouchCurrentX;
                    mTouchStartY = mTouchCurrentY;
                    break;
                case MotionEvent.ACTION_UP:
                    mStopX = (int) event.getX();
                    mStopY = (int) event.getY();
                    if (Math.abs(mStartX - mStopX) >= 1 || Math.abs(mStartY - mStopY) >= 1) {
                        isMove = true;
                    }
                    break;
            }

            return isMove;
        }
    }
}
