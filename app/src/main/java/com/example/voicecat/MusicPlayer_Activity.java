package com.example.voicecat;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voicecat.Bean.SongInfo;
import com.example.voicecat.Utils.MyToast;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class MusicPlayer_Activity extends AppCompatActivity {

    private ImageView iv_back, iv_recode;
    private TextView tv_songName, tv_songSinger;
    private TextView tv_currentTime, tv_time;
    private Button bt_play, bt_last, bt_next;
    private SeekBar seekBar;

    private ArrayList<SongInfo> mData;
    private int position = 0;
    private int current = 0;
    private int duration = 0;
    private Handler handler;
    private boolean onPlay = false;
    private boolean onStop = false;

    private final int UPDATE = 0x02;

    private MediaPlayer mediaPlayer;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        initView();
        initData();

    }

    public void initView() {
        iv_back = findViewById(R.id.iv_musicplay_back);
        iv_recode = findViewById(R.id.iv_recode);
        tv_songName = findViewById(R.id.tv_music_player_songName);
        tv_songSinger = findViewById(R.id.tv_music_player_singer);
        tv_currentTime = findViewById(R.id.tv_currentMusicTime);
        tv_time = findViewById(R.id.tv_MusicTime);
        bt_play = findViewById(R.id.bt_playMusic);
        bt_last = findViewById(R.id.bt_lastMusic);
        bt_next = findViewById(R.id.bt_nextMusic);
        seekBar = findViewById(R.id.SeekBar_musicPlayer);

        //唱片动画
        animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(60000);
        animation.setRepeatCount(-1);
        animation.setFillAfter(true);
        iv_recode.startAnimation(animation);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //调整进度条监听事件
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    setCurrentTime(seekBar.getProgress());

                    if (seekBar.getProgress() == seekBar.getMax()) {
                        onPlay = false;
                        bt_play.setBackgroundResource(R.drawable.iv_playmusic_click);
                    }

                }
            }
        });

        //播放按钮
        bt_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playMusic();

            }
        });

        //上一首
        bt_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position != 0) {//判断是否第一首
                    position = position - 1;
                    mediaPlayer.pause();
                    Intent intent_musicPlayer = new Intent(MusicPlayer_Activity.this, MusicPlayer_Activity.class);
                    intent_musicPlayer.putExtra("mData", (Serializable) mData);
                    intent_musicPlayer.putExtra("position", position);
                    startActivity(intent_musicPlayer);
                    finish();
                }else{
                    MyToast.sendMsg(MusicPlayer_Activity.this,"已经是第一首了");
                }

            }
        });

        //下一首
        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position+1 != mData.size()) {//判断是否最后一首
                    position = position + 1;
                    mediaPlayer.pause();
                    Intent intent_musicPlayer = new Intent(MusicPlayer_Activity.this, MusicPlayer_Activity.class);
                    intent_musicPlayer.putExtra("mData", (Serializable) mData);
                    intent_musicPlayer.putExtra("position", position);
                    startActivity(intent_musicPlayer);
                    finish();
                }else{
                    MyToast.sendMsg(MusicPlayer_Activity.this,"已经是最后一首了");
                }
            }
        });
    }

    public void initData() {
        mData = new ArrayList<>();
        mediaPlayer = new MediaPlayer();
        mData = (ArrayList<SongInfo>) getIntent().getSerializableExtra("mData");
        position = getIntent().getIntExtra("position", 0);
//        mediaPlayer = MediaPlayer.create(this, Uri.fromFile(new File(mData.get(position).SongPath)));
        try {
            mediaPlayer.setDataSource(String.valueOf(new File(mData.get(position).SongPath)));
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initMusic();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (onStop && !onPlay) {
                        if (mediaPlayer != null) {
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                    }

                    if (mediaPlayer != null && onPlay) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        duration = mediaPlayer.getDuration();
                        current = mediaPlayer.getCurrentPosition();
                    }
                    if (onPlay) {
                        Message msg = new Message();
                        msg.what = UPDATE;
                        handler.sendMessage(msg);
                        try {
                            Thread.sleep(800);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }
        }).start();
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case UPDATE:
                        System.out.println("current" + current);
                        System.out.println("duration" + duration);
                        if (mediaPlayer.isPlaying()) {

                            seekBar.setProgress(current);
                            setCurrentTime(current);

                        }
                        if (duration - current < 500) {
                            bt_play.setBackgroundResource(R.drawable.iv_playmusic_click);
                            seekBar.setProgress(seekBar.getMax());
                            onPlay = false;
                        }
                        break;
                }
            }
        };

        playMusic();
    }

    public void initMusic() {
        //设置歌曲长度
        seekBar.setMax(mediaPlayer.getDuration());
        //设置歌名
        tv_songName.setText(mData.get(position).SongName);
        //设置歌手
        if (mData.get(position).SongSinger.equals("<unknown>")) {
            tv_songSinger.setText("未知");
        } else {
            tv_songSinger.setText(mData.get(position).SongSinger);
        }
        //设置总时长
        getTime();

    }

    public void playMusic() {
        if (!onPlay) {

            if (seekBar.getProgress() == seekBar.getMax()) {
                mediaPlayer.seekTo(0);
            }

            mediaPlayer.start();

            onPlay = true;
            bt_play.setBackgroundResource(R.drawable.iv_pausemusic_click);

        } else {
            onPlay = false;
            bt_play.setBackgroundResource(R.drawable.iv_playmusic_click);
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }

    public void setCurrentTime(int currentPosition) {
        int time = currentPosition;
        long minutes = (time / 1000) / 60;
        long seconds = (time / 1000) % 60;
        if (minutes < 10) {
            if (seconds < 10) {

                if (seconds < 1) {
                    tv_currentTime.setText("0" + minutes + ":01");
                } else {
                    tv_currentTime.setText("0" + minutes + ":0" + seconds);
                }

            } else {
                tv_currentTime.setText("0" + minutes + ":" + seconds);
            }
        } else {
            if (seconds < 10) {
                tv_currentTime.setText("" + minutes + ":0" + seconds);
            } else {
                tv_currentTime.setText("" + minutes + ":" + seconds);
            }
        }
    }

    public void getTime() {
        int time = mData.get(position).SongLength;
        long minutes = (time / 1000) / 60;
        long seconds = (time / 1000) % 60;
        if (minutes < 10) {
            if (seconds < 10) {

                if (seconds < 1) {
                    tv_time.setText("0" + minutes + ":01");
                } else {
                    tv_time.setText("0" + minutes + ":0" + seconds);
                }

            } else {
                tv_time.setText("0" + minutes + ":" + seconds);
            }
        } else {
            if (seconds < 10) {
                tv_time.setText("" + minutes + ":0" + seconds);
            } else {
                tv_time.setText("" + minutes + ":" + seconds);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            onPlay = false;
            onStop = true;
        }
    }
}