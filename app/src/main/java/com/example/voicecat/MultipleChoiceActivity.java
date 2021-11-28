package com.example.voicecat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.voicecat.Adapter.ListMultipleAdapter;
import com.example.voicecat.Bean.CurrentItem;
import com.example.voicecat.Bean.SongInfo;
import com.example.voicecat.Utils.MySqlite;
import com.example.voicecat.Utils.MyToast;

import java.util.ArrayList;

public class MultipleChoiceActivity extends AppCompatActivity {

    private ListView listView;
    private Button bt_collection, bt_delete;
    private TextView tv_multiple;
    private ImageView iv_back;

    private ListMultipleAdapter adapter;
    private ArrayList<SongInfo> mData;
    private int currentItem = 0;

    private MySqlite mySqlite;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_choice);

        currentItem = CurrentItem.CurrentItem;
        initView();
        initData();
    }

    private void initView() {

        listView = findViewById(R.id.listview_multiple);
        bt_collection = findViewById(R.id.bt_multiple_collection);
        bt_delete = findViewById(R.id.bt_multiple_delete);
        tv_multiple = findViewById(R.id.tv_multiple_all);
        iv_back = findViewById(R.id.iv_multiple_back);

        if (currentItem == 1) {
            bt_collection.setText("取消收藏");
            bt_delete.setVisibility(View.GONE);
        }
        //单击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SongInfo songInfo = mData.get(position);
                songInfo.SongCheck = !songInfo.SongCheck;

                adapter.notifyDataSetChanged();
            }
        });
        //收藏、取消收藏按键
        bt_collection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isChoose = false;
                if (mData.size() > 0) {

                    for (int i = 0; i < mData.size(); i++) {
                        if (mData.get(i).SongCheck) {
                            isChoose = true;
                            break;
                        }
                    }
                    if (isChoose) {
                        bt_collection.setClickable(false);
                        bt_delete.setClickable(false);

                        Handler handler = new Handler() {
                            @Override
                            public void handleMessage(@NonNull Message msg) {
                                super.handleMessage(msg);
                                if (CurrentItem.CurrentItem == 0) {

                                    MyToast.sendMsg(MultipleChoiceActivity.this, "收藏成功，返回主页面");

                                } else {

                                    MyToast.sendMsg(MultipleChoiceActivity.this, "取消收藏成功，返回主页面");

                                }
                                finish();
                            }
                        };

                        if (CurrentItem.CurrentItem == 0) {
                            //全部音频界面
                            try {

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mySqlite = new MySqlite(MultipleChoiceActivity.this);
                                        db = mySqlite.getWritableDatabase();
                                        for (int i = 0; i < mData.size(); i++) {
                                            if (mData.get(i).SongCheck) {
                                                ContentValues values = new ContentValues();
                                                values.put(MySqlite.SongName, mData.get(i).SongName);
                                                values.put(MySqlite.SongSinger, mData.get(i).SongSinger);
                                                values.put(MySqlite.SongLength, mData.get(i).SongLength);
                                                values.put(MySqlite.SongPath, mData.get(i).SongPath);
                                                db.insert(MySqlite.TABLE_Collection, null, values);

                                            }
                                        }
                                        db.close();
                                        handler.sendEmptyMessage(0);
                                    }
                                }).start();

                            } catch (Exception e) {
                                e.printStackTrace();
                                MyToast.sendMsg(MultipleChoiceActivity.this, "收藏失败，发生未知错误");
                                finish();
                            }
                        } else {
                            //收藏音频界面
                            try {

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mySqlite = new MySqlite(MultipleChoiceActivity.this);
                                        db = mySqlite.getWritableDatabase();
                                        for (int i = 0; i < mData.size(); i++) {

                                            if (mData.get(i).SongCheck) {
                                                db.delete(MySqlite.TABLE_Collection, "SongPath=?", new String[]{mData.get(i).SongPath});
                                            }

                                        }
                                        db.close();
                                        handler.sendEmptyMessage(0);
                                    }
                                }).start();

                            } catch (Exception e) {
                                e.printStackTrace();
                                MyToast.sendMsg(MultipleChoiceActivity.this, "取消收藏失败，发生未知错误");
                                finish();
                            }
                        }
                    } else {
                        MyToast.sendMsg(MultipleChoiceActivity.this, "未选中任何音频");
                    }
                } else {
                    MyToast.sendMsg(MultipleChoiceActivity.this, "当前音频列表为空，请添加手动音频");
                    finish();
                }
            }
        });

        //删除音频按键
        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChoose = false;

                if (mData.size() > 0) {

                    for (int i = 0; i < mData.size(); i++) {
                        if (mData.get(i).SongCheck) {
                            isChoose = true;
                            break;
                        }
                    }
                    if (isChoose) {
                        bt_collection.setClickable(false);
                        bt_delete.setClickable(false);
                        try {
                            Handler handler = new Handler() {
                                @Override
                                public void handleMessage(@NonNull Message msg) {
                                    super.handleMessage(msg);
                                    MyToast.sendMsg(MultipleChoiceActivity.this, "删除成功，返回主页面");
                                    finish();
                                }
                            };
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mySqlite = new MySqlite(MultipleChoiceActivity.this);
                                    db = mySqlite.getWritableDatabase();
                                    for (int i = 0; i < mData.size(); i++) {

                                        if (mData.get(i).SongCheck) {
                                            db.delete(MySqlite.TABLE_NAME, "SongPath=?", new String[]{mData.get(i).SongPath});
                                            db.delete(MySqlite.TABLE_Collection, "SongPath=?", new String[]{mData.get(i).SongPath});
                                        }
                                    }
                                    db.close();
                                    handler.sendEmptyMessage(0);
                                }
                            }).start();

                        } catch (Exception e) {
                            e.printStackTrace();
                            MyToast.sendMsg(MultipleChoiceActivity.this, "删除失败，发生未知错误");
                            finish();
                        }
                    } else {
                        MyToast.sendMsg(MultipleChoiceActivity.this, "未选中任何音频");
                    }
                } else {
                    MyToast.sendMsg(MultipleChoiceActivity.this, "当前音频列表为空，请添加手动音频");
                    finish();
                }

            }
        });

        //全选按键
        tv_multiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mData.size() > 0) {
                    if (tv_multiple.getText().equals("全选")) {
                        for (int i = 0; i < mData.size(); i++) {
                            tv_multiple.setText("取消全选");
                            SongInfo songInfo = mData.get(i);
                            songInfo.SongCheck = true;
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        for (int i = 0; i < mData.size(); i++) {
                            tv_multiple.setText("全选");
                            SongInfo songInfo = mData.get(i);
                            songInfo.SongCheck = false;
                            adapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    MyToast.sendMsg(MultipleChoiceActivity.this, "当前音频列表为空，请添加手动音频");
                    finish();
                }
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {

        mData = new ArrayList<>();

        adapter = new ListMultipleAdapter(this, mData);
        listView.setAdapter(adapter);
        updataListView();
    }

    //更新ListView
    public void updataListView() {

        mySqlite = new MySqlite(this);
        db = mySqlite.getWritableDatabase();

        mData.clear();

        String table = null;
        if (currentItem == 0) {
            table = MySqlite.TABLE_NAME;
        } else {
            table = MySqlite.TABLE_Collection;
        }
        Cursor cursor = db.query(table,
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
                songInfo.SongCheck = false;
                mData.add(songInfo);
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
        db.close();

    }
}