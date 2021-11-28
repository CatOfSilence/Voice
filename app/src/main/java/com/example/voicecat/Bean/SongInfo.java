package com.example.voicecat.Bean;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class SongInfo implements Serializable {
    public String SongName; //歌名
    public String SongSinger;//歌手
    public int SongLength;//歌曲时长
    public String SongPath;//歌曲路径

    public Boolean SongCheck;//是否选中
}
