package com.example.voicecat.Utils;

import android.os.Environment;

import com.example.voicecat.Bean.FileBean;

import java.io.File;
import java.util.ArrayList;

//扫描自定义文件
public class ScanFile {

    private static final File PATH = Environment.getExternalStorageDirectory();// 获取SD卡总目录。
    private File[] files;
    private ArrayList<FileBean> mData;
    private ArrayList<String> absolutionPath;
    private ArrayList<String> mMusic = new ArrayList<>();


    public ArrayList<FileBean> getFileData() {

        mData = new ArrayList<>();
        files = PATH.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                FileBean fileBean = new FileBean();
                fileBean.filename = handleString(f);
                mData.add(fileBean);
            }
        }
        return mData;
    }

    //处理路径字符串，返回目录名
    private String handleString(File file) {
        String s = file.toString();
        int index = s.lastIndexOf("/");
        String result = s.substring(index + 1);
        return result;
    }

    //获取所有文件夹的绝对路径
    public ArrayList<String> getAbsolutionPath() {

        absolutionPath = new ArrayList<>();
        files = PATH.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                FileBean fileBean = new FileBean();
                absolutionPath.add(String.valueOf(f));
            }
        }
        return absolutionPath;
    }


////    通过遍历存储获取文件，效率太低放弃使用此方法

//    public void getMusicPath(File file) {
//        String music = null;
//
//        for (int i = 0; i < file.listFiles().length; i++) {
//            File childFile = file.listFiles()[i];
//            if (childFile.isDirectory()) {
//
//                try {
//                    getMusicPath(childFile);
//                } catch (Exception e) {
//                    Log.e("读取错误","权限不足");
//                    e.printStackTrace();
//                    break;
//                }
//
//            } else {
//                if (childFile.toString().endsWith(".mp3")) {
//                    music = childFile.getAbsolutePath();
//                    mMusic.add(music);
//
//                }
//            }
//        }
//    }
}
