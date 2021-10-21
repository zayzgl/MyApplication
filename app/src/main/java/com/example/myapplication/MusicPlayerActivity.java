package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;


import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.database.Cursor;
import android.provider.MediaStore;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.reflect.Field;
import java.util.ArrayList;


public class MusicPlayerActivity extends AppCompatActivity{
    private TextView songName,singerName;       //歌名、歌手
    private ImageView cdImage;                  //图片
    //private TextView lyricPrev,lyricCurrent,lyricNext;       //上一句歌词，当前歌词，下一句歌词
    private SeekBar musicProgress;                           //进度条
    private TextView currentTime,totalTime;                  //进度条起始时间，歌曲总时长
    private ImageButton prevBtn,playPauseBtn,nextBtn,playFormBtn;        //上一首，暂停/播放，下一首

    private ObjectAnimator animator;                         //控制CD旋转
    private MediaPlayer player;

    private int currentPlaying = 0;
    private ArrayList<Integer> playList = new ArrayList<>();

    private boolean isPausing = false ,isPlaying = false;

    private LrcUtil lrc = new LrcUtil();

    private String[] title;
    private String[] artist;
    private Field[] fields = R.raw.class.getFields();
    private List<LrcBean> list;
    private LrcView lyricShow;
    /*
    //by wu
    private boolean islistCircle = true, isSingleCircle=false,isRandomPlay=false;              //正在列表循环、正在单曲循环、正在随机播放
    //private MyServiceConn conn;                              //将Service里的内容传入Activity
    private MusicPlayerService.MusicControl musicControl;
    private boolean isUnbind =false;                         //记录服务是否被解绑
    */

    private void preparePlaylist() {
        for (int count = 0; count < fields.length; count++) {
            Log.i("Raw Asset", fields[count].getName());
            try {
                int resId = fields[count].getInt(fields[count]);
                playList.add(resId);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        title = new String[fields.length];
        artist = new String[fields.length];
        //setText();
    }

    private void prepareMedia() {
        if (isPlaying) {
            player.stop();
            player.reset();
        }
        player = MediaPlayer.create(getApplicationContext(), playList.get(currentPlaying));
        int musicDuration = player.getDuration();
        musicProgress.setMax(musicDuration);
        int sec = musicDuration / 1000;
        int min = sec / 60;
        sec -= min * 60;
        String musicTime = String.format("%02d:%02d", min, sec);
        totalTime.setText(musicTime);
        String lyric = LrcUtil.parseLrcFile(fields[currentPlaying].getName());
        list = LrcUtil.parseStr2List(lyric,songName,singerName);
        lyricShow.setPlayer(player);
        lyricShow.setLrc(list);
        lyricShow.init();
        /*Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST}, null, null, null);
        if (cursor != null ) {
            cursor.moveToFirst();
            do {
                Log.i("NIFO","读取信息2");
                String title = cursor.getString(0);   //获得歌名
                String artist = cursor.getString(1);  //获得歌手
                songName.setText(title);
                singerName.setText(artist);
            } while (cursor.moveToNext());
        }
            if (!cursor.isClosed()) {
                cursor.close();
            }*/
            player.start();
        }

    /*private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent)

        {
            String action = intent.getAction();

            String cmd = intent.getStringExtra("command");

            Log.d("mIntentReceiver", action + " / " + cmd);

            String title = intent.getStringExtra("title");

            String artist = intent.getStringExtra("artist");

            String album = intent.getStringExtra("album");

            String track = intent.getStringExtra("track");
            songName.setText(title);
            singerName.setText(artist);

            Log.d("Music",artist+":"+album+":"+track);

        }

    };*/

    private void updateTimer() {
        this.runOnUiThread(new Runnable() {
            int currentMs = player.getCurrentPosition();
            int sec = currentMs / 1000;
            int min = sec / 60;
            @Override
            public void run() {
                sec -= min * 60;
                String time = String.format("%02d:%02d", min, sec - min * 60);
                musicProgress.setProgress(currentMs);
                currentTime.setText(time);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        init();
        preparePlaylist();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isPlaying) {
                    updateTimer();
                }
            }
        };
        new Timer().scheduleAtFixedRate(timerTask, 0, 500);
        /*IntentFilter itFilter = new IntentFilter();
        itFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mReceiver, itFilter);*/
    }

    /*protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }*/

    void init()
    {
        songName=findViewById(R.id.tv_song_name);
        singerName=findViewById(R.id.tv_song_singer);
        cdImage=findViewById(R.id.cd);
        lyricShow=findViewById(R.id.lyric);
        musicProgress=findViewById(R.id.process);
        prevBtn=findViewById(R.id.last_song);
        playPauseBtn= findViewById(R.id.play);
        nextBtn=findViewById(R.id.next_song);
        currentTime=findViewById(R.id.process_beg);
        totalTime=findViewById(R.id.process_total);

        onClickControl onClick = new onClickControl();
        playPauseBtn.setOnClickListener(onClick);
        prevBtn.setOnClickListener(onClick);
        nextBtn.setOnClickListener(onClick);

        OnSeekBarChangeControl onSbChange = new OnSeekBarChangeControl();
        musicProgress.setOnSeekBarChangeListener(onSbChange);

        //CD旋转设置
        animator=ObjectAnimator.ofFloat(cdImage,"rotation",0,360.0F);
        animator.setDuration(10000);  //10s转动时长
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(-1);  //一直转动

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"tonghuazhen.lrc");


       /* Intent intent = new Intent(getApplicationContext(), MusicPlayerService.class);
        conn = new MyServiceConn();         //创建服务连接对象
        bindService(intent,conn,BIND_AUTO_CREATE);      //绑定服务*/

    }

    private class onClickControl implements View.OnClickListener{
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.play:
                    Log.i("INFO","开始播放");
                    if(!isPlaying && !isPausing)        //暂停且从未被播放
                    {
                        playPauseBtn.setImageResource(R.drawable.play);  //切换播放键
                        animator.start();
                        prepareMedia();
                        /*songName.setText(title[currentPlaying]);
                        singerName.setText(artist[currentPlaying]);*/
                    }
                    else if(!isPausing && isPlaying)    //暂停且被播放过
                    {
                        playPauseBtn.setImageResource(R.drawable.play);   //切换播放键
                        animator.resume();
                        player.start();
                    }else                               //播放
                    {
                        playPauseBtn.setImageResource(R.drawable.pause);    //切换暂停键
                        animator.pause();
                        player.pause();
                    }
                    isPausing = !isPausing;
                    isPlaying = true;
                    break;
                case R.id.last_song:
                    Log.i("INFO","前一首");
                    playPauseBtn.setImageResource(R.drawable.play);   //切换播放键
                    animator.start();
                    isPausing = true;
                    if(currentPlaying !=0 )
                        currentPlaying = --currentPlaying % playList.size();
                    else
                        currentPlaying = playList.size() - 1;
                    prepareMedia();
                    /*
                    songName.setText(title[currentPlaying]);
                    singerName.setText(artist[currentPlaying]);*/
                    break;
                case R.id.next_song:
                    Log.i("INFO","下一首");
                    playPauseBtn.setImageResource(R.drawable.play);   //切换播放键
                    animator.start();
                    isPausing = true;
                    currentPlaying = ++currentPlaying % playList.size();
                    prepareMedia();
                    /*songName.setText(title[currentPlaying]);
                    singerName.setText(artist[currentPlaying]);*/
                    break;
                /*case R.id.play_form:
                    if(islistCircle){//列表循环
                        playFormBtn.setImageResource(R.drawable.list_circle);

                    }else if(isSingleCircle){//单曲循环
                        playFormBtn.setImageResource(R.drawable.single_circle);

                    }else{//随机播放
                        playFormBtn.setImageResource(R.drawable.random_play);

                    }*/
                 default:
                     Log.i("INFO","错误！");
            }
        }
    }

    private class OnSeekBarChangeControl implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                player.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            player.pause();
            animator.pause();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            player.start();
            if (seekBar.getProgress() < 10) {
                animator.start();
            } else {
                animator.resume();
            }
        }
    }
    /*private void setLyric(String name)
    {
        System.out.println("识别歌词");
        String ori_lrc = lrc.parseLrcFile(name);
        lrc.parseStr2List(ori_lrc,songName,singerName);
        list=lrc.parseStr2List(ori_lrc,songName,singerName);
        while (player.isPlaying()) {
            int time = player.getCurrentPosition();
            if (time % 10 == 0) {
                System.out.println("显示歌词");
                if (time < list.get(0).getStart() || time > list.get(list.size() - 1).getStart())
                    continue;
                else {
                    for (int i = 0; i < list.size(); i++) {
                        if (time >= list.get(i).getStart() && time < list.get(i).getEnd()) {
                            lyricCurrent.setText(list.get(i).getLrc());
                            if (i > 0)
                                lyricPrev.setText(list.get(i - 1).getLrc());
                            if (i < list.size() - 1)
                                lyricNext.setText(list.get(i + 1).getLrc());
                        }
                    }
                }
            }
        }
    }*/
    /*
    public void setText()
    {
        int id,i = 0;
        /*Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST}, null, null, null);*/
        //Uri music_uri = Uri.parse("content://storage/emulated/0/Music");
        /*Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,  null ,
                null ,  null ,null);
        if (cursor != null ) {
            cursor.moveToFirst();
            do {
                Log.i("NIFO","读取信息2");
                id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                System.out.println(id);
                title[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                artist[i] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                System.out.println(path);
                System.out.println(i+title[i]);
                System.out.println(i+artist[i]);
                i++;
                /*title[i++] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));  //获得歌名
                artist[i++] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)); //获得歌手
                System.out.println(i+title[i-1]);
                System.out.println(i+artist[i-1]);*/
            /*} while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
    }*/


    /*
    private class MyServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            musicControl=(MusicPlayerService.MusicControl) service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name){

        }
    }

    private void unbind(boolean isUnbind){
        if(!isUnbind){//判断服务是否被解绑
            musicControl.pausePlay();//暂停播放音乐
            unbindService(conn);//解绑服务
        }
    }

    @Override
    public void onDestroy(){
        unbind(isUnbind);//解绑服务
        super.onDestroy();
    }*/
}
