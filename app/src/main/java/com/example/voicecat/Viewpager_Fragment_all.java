package com.example.voicecat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.voicecat.Adapter.ListAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.example.voicecat.Bean.SongInfo;
import com.example.voicecat.Utils.MySqlite;

public class Viewpager_Fragment_all extends Fragment {

    private boolean isCreated = false;

    private ListView listView;
    private List<SongInfo> mData;
    private MySqlite mySqlite;
    private SQLiteDatabase db;

    private ListAdapter adapter;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private ConstraintLayout cl1, cl2;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCreated = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_viewpager__all, container, false);

        initView(view);
        initData(view);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        updataListView();

    }

    public void initView(View view) {

        listView = view.findViewById(R.id.listview_all);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("点击了第" + position);
                System.out.println(mData.get(position).SongName);
                Intent intent_musicPlayer = new Intent(getActivity(),MusicPlayer_Activity.class);
                intent_musicPlayer.putExtra("mData",(Serializable) mData);
                intent_musicPlayer.putExtra("position",position);
                startActivity(intent_musicPlayer);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("长按了第" + position);
                showDialog(position);
                return true;
            }
        });
    }

    public void initData(View view) {

        mData = new ArrayList<>();

        adapter = new ListAdapter(getContext(), mData);
        listView.setAdapter(adapter);

    }

    public void updataListView() {

        mySqlite = new MySqlite(getContext());
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

    public void showDialog(int position) {

        mySqlite = new MySqlite(getContext());
        db = mySqlite.getWritableDatabase();

        builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.long_click_dialog_item, null);
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

                ContentValues values = new ContentValues();
                values.put(MySqlite.SongName, mData.get(position).SongName);
                values.put(MySqlite.SongSinger, mData.get(position).SongSinger);
                values.put(MySqlite.SongLength, mData.get(position).SongLength);
                values.put(MySqlite.SongPath, mData.get(position).SongPath);
                db.insert(MySqlite.TABLE_Collection, null, values);
                db.close();

                dialog.dismiss();

            }
        });
        cl2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //全部音频界面监听事件

                db.delete(MySqlite.TABLE_NAME, "SongPath=?", new String[]{mData.get(position).SongPath});
                db.delete(MySqlite.TABLE_Collection, "SongPath=?", new String[]{mData.get(position).SongPath});
                db.close();
                updataListView();

                dialog.dismiss();

            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (!isCreated) {
            return;
        }

        if (isVisibleToUser) {
            updataListView();
        }
    }
}