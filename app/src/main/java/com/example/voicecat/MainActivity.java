package com.example.voicecat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.voicecat.Adapter.TabFragmentPagerAdapter;
import com.example.voicecat.Bean.CurrentItem;
import com.example.voicecat.Bean.Song;
import com.example.voicecat.Service.FloatingListService;
import com.example.voicecat.Utils.AudioUtils;
import com.example.voicecat.Utils.MySqlite;
import com.example.voicecat.Utils.MyToast;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private View view_all, view_pick;
    private ArrayList<View> mView;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Button bt_add;
    private TextView ed_search;
    private ImageView iv_multiple, iv_photo, iv_set;

    private List<Fragment> list;

    //权限成员变量
    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    List<String> mPermissionList = new ArrayList<>();
    private static final int PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tablayout);

        view_all = View.inflate(this, R.layout.fragment_viewpager__all, null);
        view_pick = View.inflate(this, R.layout.fragment_viewpager__pick, null);

        mView = new ArrayList<>();
        mView.add(view_all);
        mView.add(view_pick);

        list = new ArrayList<>();
        list.add(new Viewpager_Fragment_all());
        list.add(new Viewpager_Fragment_pick());

        viewPager.setAdapter(new TabFragmentPagerAdapter(getSupportFragmentManager(), list));
        viewPager.setCurrentItem(0);
        tabLayout.setupWithViewPager(viewPager);

        startFloatingListService();
    }

    //返回Activity结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(MainActivity.this, "请手动开启悬浮窗权限", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "悬浮窗权限开启成功", Toast.LENGTH_SHORT).show();
                startService(new Intent(MainActivity.this, FloatingListService.class));
            }
            checkPermission();
        }

    }

    //判断和开启悬浮窗权限
    public void startFloatingListService() {
        if (FloatingListService.isStarted) {
            checkPermission();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                MyToast.sendMsg(MainActivity.this, "请手动开启悬浮窗权限");
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
            } else {
                startService(new Intent(MainActivity.this, FloatingListService.class));
                checkPermission();
            }
        }
    }

    // 检查存储权限
    private void checkPermission() {
        mPermissionList.clear();
        //判断哪些权限未授予
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        /**
         * 判断是否为空
         */
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_REQUEST);
        }
    }

    /**
     * 响应授权
     * 这里不管用户是否拒绝，都进入首页，不再重复申请权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST:
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private void initView() {
        bt_add = findViewById(R.id.bt_add);
        ed_search = findViewById(R.id.ed_search);
        iv_multiple = findViewById(R.id.iv_multiple);
        iv_photo = findViewById(R.id.iv_photo);
        iv_set = findViewById(R.id.iv_set);


        bt_add.setOnClickListener(this);
        ed_search.setOnClickListener(this);
        iv_multiple.setOnClickListener(this);
        iv_photo.setOnClickListener(this);
        iv_set.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_add:
                startActivity(new Intent(MainActivity.this, ScanMusic.class));
                break;
            case R.id.ed_search:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                break;
            case R.id.iv_multiple:
                CurrentItem.CurrentItem = viewPager.getCurrentItem();
                startActivity(new Intent(MainActivity.this, MultipleChoiceActivity.class));
                break;
            case R.id.iv_photo:
                startActivity(new Intent(MainActivity.this, Introduction.class));
                break;
            case R.id.iv_set:
                startActivity(new Intent(MainActivity.this, SetActivity.class));
                break;

        }
    }
}