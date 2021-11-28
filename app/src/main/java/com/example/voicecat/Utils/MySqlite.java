package com.example.voicecat.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySqlite extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "SongList.db";
    public static final String TABLE_NAME = "Songs";//全部音频
    public static final String TABLE_Collection = "SongsCollection";//收藏音频
    public static final String SongName = "SongName";
    public static final String SongSinger = "SongSinger";
    public static final String SongLength = "SongLength";
    public static final String SongPath = "SongPath";

    public MySqlite(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + TABLE_NAME + " (Id integer primary key autoincrement, SongName text, SongSinger text, SongLength integer, SongPath text UNIQUE)";
        db.execSQL(sql);
        String sqlCollection = "create table if not exists " + TABLE_Collection + " (Id integer primary key autoincrement, SongName text, SongSinger text, SongLength integer, SongPath text UNIQUE)";
        db.execSQL(sqlCollection);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
        String sqlCollection = "DROP TABLE IF EXISTS " + TABLE_Collection;
        db.execSQL(sqlCollection);
        onCreate(db);
    }
}
