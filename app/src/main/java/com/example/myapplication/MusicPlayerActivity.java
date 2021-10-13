package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MusicPlayerActivity extends AppCompatActivity {
    private TextView songName,singerName;       //歌名、歌手
    private ImageView cdImage;                  //图片
    private TextView lyricPrev,lyricCurrent,lyricNext;       //上一句歌词，当前歌词，下一句歌词
    private SeekBar musicProgress;                           //进度条
    private TextView currentTime,totalTime;                  //进度条起始时间，歌曲总时长
    private ImageButton prevBtn,playPauseBtn,nextBtn;        //上一首，暂停/播放，下一首

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
    }

    void init()
    {
        songName=findViewById(R.id.tv_song_name);
        singerName=findViewById(R.id.tv_song_name);
        cdImage=findViewById(R.id.cd);
        lyricPrev=findViewById(R.id.lyric_previous);
        lyricCurrent=findViewById(R.id.lyric_current);
        lyricNext=findViewById(R.id.lyric_next);
        musicProgress=findViewById(R.id.process);
        prevBtn=findViewById(R.id.last_song);
        playPauseBtn=findViewById(R.id.play);
        nextBtn=findViewById(R.id.next_song);
        currentTime=findViewById(R.id.process_beg);
        totalTime=findViewById(R.id.process_total);
    }

}
