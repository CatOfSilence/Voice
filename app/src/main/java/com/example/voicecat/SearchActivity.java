package com.example.voicecat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.voicecat.Adapter.ListAdapter;
import com.example.voicecat.Bean.SongInfo;
import com.example.voicecat.Utils.MySqlite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private TextView tv_cancel;
    private EditText ed_search;

    private ListView listView;
    private List<SongInfo> mData;
    private List<SongInfo> currentData;
    private MySqlite mySqlite;
    private SQLiteDatabase db;

    private ListAdapter adapter;
    private ListAdapter currentAdapter;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private ConstraintLayout cl1, cl2;

    private int current = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initView();
        initData();
        updataListView();
    }

    private void initData() {

        mData = new ArrayList<>();
        currentData = new ArrayList<>();

        adapter = new ListAdapter(SearchActivity.this, mData);
        currentAdapter = new ListAdapter(SearchActivity.this, currentData);
        listView.setAdapter(adapter);

    }


    private void initView() {
        tv_cancel = findViewById(R.id.tv_search_cancel);
        ed_search = findViewById(R.id.ed_search_);
        listView = findViewById(R.id.listview_search);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<SongInfo> list = new ArrayList<>();
                if(current==0){
                    list = mData;
                }else{
                    list = currentData;
                }
                System.out.println("点击了第" + position);
                System.out.println(list.get(position).SongName);
                Intent intent_musicPlayer = new Intent(SearchActivity.this,MusicPlayer_Activity.class);
                intent_musicPlayer.putExtra("mData",(Serializable) list);
                intent_musicPlayer.putExtra("position",position);
                startActivity(intent_musicPlayer);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("长按了第" + position);
                showMyDialog(position);
                return true;
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ed_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = ed_search.getText().toString();
                if(s.length()>0) {
                    currentData.clear();
                    for (int i = 0; i < mData.size(); i++) {
                        String songName = mData.get(i).SongName;
                        songName = songName.toUpperCase();
                        if (songName.indexOf(str.toUpperCase())!=-1) {
                            currentData.add(mData.get(i));
                        }
                    }
                    current = 1;
                    listView.setAdapter(currentAdapter);
                }else{
                    current = 0;
                    listView.setAdapter(adapter);
                }
            }
        });
    }

    public void updataListView() {

        mySqlite = new MySqlite(this);
        db = mySqlite.getWritableDatabase();

        mData.clear();
        Cursor cursor = db.query(MySqlite.TABLE_NAME,
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


    public void showMyDialog(int position) {

        mySqlite = new MySqlite(this);
        db = mySqlite.getWritableDatabase();

        builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.long_click_dialog_item, null);
        cl1 = view.findViewById(R.id.cl_dialog1);
        cl2 = view.findViewById(R.id.cl_dialog2);
        builder.setView(view);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_view);
        dialog.show();

        cl1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //全部音频界面监听事件
                List<SongInfo> songInfos = new ArrayList<>();
                if(current==0){
                    songInfos = mData;
                }else{
                    songInfos = currentData;
                }
                ContentValues values = new ContentValues();
                values.put(MySqlite.SongName, songInfos.get(position).SongName);
                values.put(MySqlite.SongSinger, songInfos.get(position).SongSinger);
                values.put(MySqlite.SongLength, songInfos.get(position).SongLength);
                values.put(MySqlite.SongPath, songInfos.get(position).SongPath);
                db.insert(MySqlite.TABLE_Collection, null, values);
                db.close();
                dialog.dismiss();

                if(current==0){
                    adapter.notifyDataSetChanged();
                }else{
                    currentAdapter.notifyDataSetChanged();
                }
            }
        });
        cl2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //全部音频界面监听事件
                List<SongInfo> songInfos = new ArrayList<>();
                if(current==0){
                    songInfos = mData;
                }else{
                    songInfos = currentData;
                }
                db.delete(MySqlite.TABLE_NAME, "SongPath=?", new String[]{songInfos.get(position).SongPath});
                db.delete(MySqlite.TABLE_Collection, "SongPath=?", new String[]{songInfos.get(position).SongPath});
                db.close();
                updataListView();
                dialog.dismiss();

                if(current==0){
                    adapter.notifyDataSetChanged();
                }else{
                    currentData.remove(position);
                    currentAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}