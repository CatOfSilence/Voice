package com.example.voicecat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.example.voicecat.Bean.Song;
import com.example.voicecat.Utils.AudioUtils;
import com.example.voicecat.Utils.MySqlite;
import com.example.voicecat.Utils.MyToast;

import java.io.IOException;
import java.util.ArrayList;

public class ScanMusic extends AppCompatActivity {

    private Button bt_scan_all, bt_scan_custom;
    private ImageView iv_scanMusic, iv_back;
    private Animation animation;

    private MySqlite mySqlite;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_music);

        initView();
        initData();
        initListener();

    }

    private void initData() {

        mySqlite = new MySqlite(ScanMusic.this);
        db = mySqlite.getWritableDatabase();

    }


    public void initView() {

        iv_scanMusic = findViewById(R.id.iv_scanMusic2);
        iv_back = findViewById(R.id.iv_scan_back);
        bt_scan_all = findViewById(R.id.bt_scan_all);
        bt_scan_custom = findViewById(R.id.bt_scan_custom);

        //创建旋转动画
        animation = new RotateAnimation(0, 359);
        animation.setDuration(1000);
        animation.setRepeatCount(-1);//动画的反复次数
        animation.setFillAfter(true);//设置为true，动画转化结束后被应用

    }


    private void initListener() {

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bt_scan_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scanAllMusic();

            }
        });
        bt_scan_custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(ScanMusic.this,ReadFileActivity.class));
                finish();

            }
        });
    }
    public void scanAllMusic(){

        iv_scanMusic.startAnimation(animation);
        bt_scan_custom.setVisibility(View.INVISIBLE);
        bt_scan_all.setText("正在扫描中......");
        bt_scan_all.setClickable(false);

        Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                MyToast.sendMsg(ScanMusic.this, "扫描完成，返回主页面");
                finish();

            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    MediaScannerConnection.scanFile(ScanMusic.this, new String[]{Environment
                            .getExternalStorageDirectory().getAbsolutePath()}, null, null);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//如果是4.4及以上版本
                        Intent mediaScanIntent = new Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(Environment.getExternalStorageDirectory()); //out is your output file
                        mediaScanIntent.setData(contentUri);
                        ScanMusic.this.sendBroadcast(mediaScanIntent);
                    } else {
                        sendBroadcast(new Intent(
                                Intent.ACTION_MEDIA_MOUNTED,
                                Uri.parse("file://"
                                        + Environment.getExternalStorageDirectory())));
                    }

                    ArrayList<Song> songs = new ArrayList<>();
                    songs = AudioUtils.getAllSongs(ScanMusic.this);

                    for (int i = 0; i < songs.size(); i++) {
                        //音频长度大于500ms则添加至数据库
                        if (songs.get(i).getDuration() > 1) {
                            ContentValues values = new ContentValues();
                            values.put(MySqlite.SongName, songs.get(i).getTitle());
                            values.put(MySqlite.SongSinger, songs.get(i).getSinger());
                            values.put(MySqlite.SongLength, songs.get(i).getDuration());
                            values.put(MySqlite.SongPath, songs.get(i).getFileUrl());
                            db.insert(MySqlite.TABLE_NAME, null, values);
                        }

                    }
                    db.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.sendEmptyMessage(0);
            }
        }).start();


    }
    private void openAssetMusics() {

        try {
            //播放 assets/a2.mp3 音乐文件
            AssetManager am = getAssets();
            AssetFileDescriptor fd = am.openFd("music/GG.mp3");
            System.out.println("GG:"+fd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}