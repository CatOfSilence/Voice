package com.example.voicecat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.voicecat.Bean.FileBean;
import com.example.voicecat.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileListAdapter extends BaseAdapter {

    public static final String SONG = "song";
    public static final String CHECK = "checkbox";

    private ArrayList<HashMap<String, Object>> mDataSource;
    private Context mContext;

    public FileListAdapter(Context context, ArrayList<HashMap<String, Object>> list) {
        this.mContext = context;
        this.mDataSource = list;
    }

    @Override
    public int getCount() {
        if (mDataSource == null) {
            return 0;
        }
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        if (mDataSource == null) {
            return new HashMap<String, Object>();
        }
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = newView(position, parent);
        } else {
            view = convertView;
        }
        bindView(view, position);
        return view;
    }

    private View newView(int position, ViewGroup parent) {
        View view = null;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        ViewHolder mViewHolder = new ViewHolder();
        view = mInflater.inflate(R.layout.readfile_item, parent, false);
        mViewHolder.textView = (TextView) view.findViewById(R.id.tv_readfile);
        mViewHolder.checkBox = (CheckBox) view.findViewById(R.id.cb_readfile);
        view.setTag(mViewHolder);
        return view;
    }

    private void bindView(View view, int position) {
        HashMap<String, Object> data = mDataSource.get(position);
        String str = (String) data.get(SONG);
        Boolean tag = (Boolean) data.get(CHECK);
        ViewHolder mViewHolder = (ViewHolder) view.getTag();
        mViewHolder.textView.setText(str);
        mViewHolder.checkBox.setChecked(tag);
    }


    class ViewHolder {
        public TextView textView;
        public CheckBox checkBox;
    }
}