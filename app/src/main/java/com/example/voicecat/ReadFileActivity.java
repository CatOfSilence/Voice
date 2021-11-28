package com.example.voicecat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.example.voicecat.Adapter.FileListAdapter;
import com.example.voicecat.Bean.FileBean;
import com.example.voicecat.Bean.Song;
import com.example.voicecat.Utils.AudioUtils;
import com.example.voicecat.Utils.MySqlite;
import com.example.voicecat.Utils.MyToast;
import com.example.voicecat.Utils.ScanFile;

public class ReadFileActivity extends Activity implements Runnable, OnItemClickListener {

    public static final String SONG = "song";
    public static final String CHECK = "checkbox";

    private ArrayList<HashMap<String, Object>> mData;
    private ArrayList<String> absolutionPath;
    private ArrayList<String> finalPath;
    private List<FileBean> Data;
    private Boolean mCheckable = false;

    private ListView mListView;
    private LinearLayout linear;
    private FileListAdapter mAdapter;
    private Thread thread;
    private Handler handler;
    private ScanFile scanFile;

    private MySqlite mySqlite;
    private SQLiteDatabase db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initView() {

        setContentView(R.layout.activity_read_file);
        mListView = findViewById(R.id.list_readfile);
        linear = findViewById(R.id.linear_read);
        mListView.setOnItemClickListener(this);

        linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scanMusic();

            }
        });
    }

    private void initData() {
        mData = new ArrayList<HashMap<String, Object>>();
        Data = new ArrayList<>();
        absolutionPath = new ArrayList<>();
        finalPath = new ArrayList<>();
        scanFile = new ScanFile();
        thread = new Thread(this);

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                for (int i = 0; i < Data.size(); i++) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put(SONG, Data.get(i).filename);
                    map.put(CHECK, mCheckable);
                    mData.add(map);
                }

                mAdapter = new FileListAdapter(getApplicationContext(), mData);
                mListView.setAdapter(mAdapter);

            }
        };

        thread.start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        HashMap<String, Object> item = mData.get(position);
        Boolean isChecked = (Boolean) item.get(CHECK);
        item.put(CHECK, !isChecked);

        System.out.println("当前按下的是：" + position);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void run() {
        Data = scanFile.getFileData();
        absolutionPath = scanFile.getAbsolutionPath();
        handler.sendEmptyMessage(0);
    }

    private void scanMusic() {

        boolean isChoose = false;
        for (int i = 0; i < mData.size(); i++) {
            boolean check = (boolean) mData.get(i).get(CHECK);
            if (check) {
                isChoose = true;
                break;
            }
        }

        if (isChoose) {

            try {
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);

                        MyToast.sendMsg(ReadFileActivity.this, "扫描完成，返回主页面");
                        finish();
                    }
                };
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        mySqlite = new MySqlite(ReadFileActivity.this);
                        db = mySqlite.getWritableDatabase();

                        MediaScannerConnection.scanFile(ReadFileActivity.this, new String[]{Environment
                                .getExternalStorageDirectory().getAbsolutePath()}, null, null);

                        ArrayList<Song> songs = new ArrayList<>();
                        songs = AudioUtils.getAllSongs(ReadFileActivity.this);

                        getFinalPath();

                        for (int i = 0; i < finalPath.size(); i++) {
                            String checkPath = finalPath.get(i);
                            for (int j = 0; j < songs.size(); j++) {
                                String songPath = songs.get(j).getFileUrl();
                                if (songPath.indexOf(checkPath) != -1) {

                                    if (songs.get(j).getDuration() > 1) {
                                        ContentValues values = new ContentValues();
                                        values.put(MySqlite.SongName, songs.get(j).getTitle());
                                        values.put(MySqlite.SongSinger, songs.get(j).getSinger());
                                        values.put(MySqlite.SongLength, songs.get(j).getDuration());
                                        values.put(MySqlite.SongPath, songs.get(j).getFileUrl());
                                        db.insert(MySqlite.TABLE_NAME, null, values);
                                    }

                                }
                            }
                        }
                        db.close();
                        handler.sendEmptyMessage(0);

                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
                MyToast.sendMsg(ReadFileActivity.this, "扫描失败，发生未知错误");
                finish();
            }

        } else {
            MyToast.sendMsg(ReadFileActivity.this, "请选择要扫描的文件夹");
        }
    }

    private void getFinalPath() {
        if (absolutionPath.size() == mData.size()) {
            finalPath.clear();
            for (int i = 0; i < absolutionPath.size(); i++) {
                boolean check = (boolean) mData.get(i).get(CHECK);
                if (check) {
                    finalPath.add(absolutionPath.get(i));
                }
            }
        } else {
            Log.e("文件错误：", "绝对路径数量不一致");
        }
    }

//    private void scanMusic() {
//
//        if (absolutionPath.size() == mData.size()) {
//            finalPath.clear();
//            for (int i = 0; i < absolutionPath.size(); i++) {
//                boolean check = (boolean) mData.get(i).get(CHECK);
//                if (check) {
//                    finalPath.add(absolutionPath.get(i));
//                }
//            }
//        } else {
//            Log.e("文件错误：", "绝对路径数量不一致");
//        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                for (int i = 0; i < finalPath.size(); i++) {
//                    System.out.println("路径："+finalPath.get(i));
//                    File music_Directory = new File(finalPath.get(i));
//                    scanFile.getMusicPath(music_Directory);
//                }
//                music = scanFile.getmMusic();
//                for (String s : music) {
//                    System.out.println("音乐：" + s);
//                }
//
//            }
//        }).start();
//
//    }

}