package com.example.voicecat;

import android.app.AlertDialog;
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
import android.widget.TextView;

import com.example.voicecat.Adapter.ListAdapter;
import com.example.voicecat.Bean.SongInfo;
import com.example.voicecat.Utils.MySqlite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Viewpager_Fragment_pick extends Fragment {

    private ListView listView;
    private List<SongInfo> mData;
    private MySqlite mySqlite;
    private SQLiteDatabase db;

    private ListAdapter adapter;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private ConstraintLayout cl1, cl2;


    private boolean isCreated = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCreated = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_viewpager__pick, container, false);

        initView(view);
        initData(view);
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        updataListView();

    }



    private void initView(View view) {

        listView = view.findViewById(R.id.listview_pick);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("点击了第"+position);
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
                System.out.println("长按了第"+position);
                showDialog(position);
                return true;
            }
        });

    }

    private void initData(View view) {

        mData = new ArrayList<>();

        adapter = new ListAdapter(getContext(), mData);
        listView.setAdapter(adapter);

    }

    public void updataListView() {

        mySqlite = new MySqlite(getContext());
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


    public void showDialog(int position) {

        mySqlite = new MySqlite(getContext());
        db = mySqlite.getWritableDatabase();

        builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.long_click_dialog_item, null);
        TextView tv_dialog = view.findViewById(R.id.tv_dialog);
        cl1 = view.findViewById(R.id.cl_dialog1);
        cl2 = view.findViewById(R.id.cl_dialog2);
        builder.setView(view);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_view);
        tv_dialog.setText("取消收藏");
        cl2.setVisibility(View.GONE);

        dialog.show();

        cl1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //全部音频界面监听事件

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