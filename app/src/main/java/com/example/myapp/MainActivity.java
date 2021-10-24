/*package com.example.myapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView songName,singerName;       //歌名、歌手
    private ImageView cdImage;                  //图片
    private TextView lyricPrev,lyricCurrent,lyricNext;       //上一句歌词，当前歌词，下一句歌词
    private SeekBar musicProgress;                           //进度条
    private TextView currentTime,totalTime;                  //进度条起始时间，歌曲总时长
    private ImageButton prevBtn,playPauseBtn,nextBtn;        //上一首，暂停/播放，下一首

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}*/
package com.example.myapp;

//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
//import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FrameLayout content;
    private TextView tv1,tv2;
    private FragmentManager fm;
    private FragmentTransaction ft;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        content=findViewById(R.id.content);

        tv1=findViewById(R.id.menu1);
        tv2=findViewById(R.id.menu2);

        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);

        fm=getSupportFragmentManager();//若是继承FragmentActivity，fm=getFragmentManger();
        ft=fm.beginTransaction();
        ft.replace(R.id.content,new frag1());//默认情况下Fragment1
        ft.commit();
    }
    @Override
    public void onClick(View v){
        ft=fm.beginTransaction();
        switch (v.getId()){
            case R.id.menu1:
                ft.replace(R.id.content,new frag1());
                break;
            case R.id.menu2:
                ft.replace(R.id.content,new frag2());
                break;
            default:
                break;
        }
        ft.commit();
    }
}


