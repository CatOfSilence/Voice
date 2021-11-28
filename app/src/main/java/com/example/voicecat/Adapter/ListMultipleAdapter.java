package com.example.voicecat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.voicecat.Bean.SongInfo;
import com.example.voicecat.R;

import java.util.HashMap;
import java.util.List;

public class ListMultipleAdapter extends BaseAdapter {

    private List<SongInfo> mData;
    private Context mcontext;

    public ListMultipleAdapter(Context mcontext, List<SongInfo> mData) {
        this.mData = mData;
        this.mcontext = mcontext;
    }

    @Override
    public int getCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(mcontext).inflate(R.layout.listview_item_multiple, parent, false);
        TextView tv_pick = convertView.findViewById(R.id.tv_voice_multiple);
        TextView tv_number = convertView.findViewById(R.id.tv_multiple_number);
        TextView tv_singer = convertView.findViewById(R.id.tv_multiple_singer);
        TextView tv_length = convertView.findViewById(R.id.tv_multiple_length);
        CheckBox cb_multiple = convertView.findViewById(R.id.cb_multiple);
        tv_number.setText(String.valueOf(position + 1));
        tv_pick.setText(mData.get(position).SongName);
        String singer = mData.get(position).SongSinger;
        if (singer.equals("<unknown>")) {
            tv_singer.setText("未知");
        } else {
            tv_singer.setText(singer);
        }
        tv_pick.setText(mData.get(position).SongName);

        int time = mData.get(position).SongLength;
        long minutes = (time / 1000) / 60;
        long seconds = (time / 1000) % 60;

        if (minutes < 10) {
            if (seconds < 10) {
                if (seconds < 1) {
                    tv_length.setText(" - " + minutes + ":01");
                } else {
                    tv_length.setText(" - " + minutes + ":0" + seconds);
                }
            } else {
                tv_length.setText(" - " + minutes + ":" + seconds);
            }
        } else {
            if (seconds < 10) {
                tv_length.setText(" - " + minutes + ":0" + seconds);
            } else {
                tv_length.setText(" - " + minutes + ":" + seconds);
            }
        }
        cb_multiple.setChecked(mData.get(position).SongCheck);

        cb_multiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongInfo songInfo = mData.get(position);
                songInfo.SongCheck = !songInfo.SongCheck;
                cb_multiple.setChecked(mData.get(position).SongCheck);
            }
        });


        return convertView;
    }
}
