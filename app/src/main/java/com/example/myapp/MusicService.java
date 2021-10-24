

/*public class MusicService extends Service {
    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}*/
package com.example.myapp;

        import android.animation.ObjectAnimator;
        import android.app.Service;
        import android.content.Intent;
        import android.media.MediaPlayer;
        import android.net.Uri;
        import android.os.Binder;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.os.Handler;
        import android.os.IBinder;
        import android.os.Message;
        import android.util.Log;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.annotation.RequiresApi;

        import java.io.File;
        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Random;
        import java.util.Timer;
        import java.util.TimerTask;

public class MusicService extends Service {
    private MediaPlayer player;
    private Timer timer;
    public static boolean overFlag = false;//判断歌曲是否播放结束
    public static int duration, currentPosition;//当前播放时长、歌曲总时长
    private String path;//歌曲路径
    File file=new File("/storage/emulated/0/Music");//音乐放在模拟机的0/Music路径下
    final File[] files=file.listFiles();//音乐从拟机的0/Music路径放到files中
    public boolean sdCardExist;//判断sd卡是否存在
    private int cur_mode = 0;//播放模式 规定 0 为单曲循环 1 为列表循环 2 为随机播放
    //private int imgPos;//当前歌曲在歌单中的位置
    public MusicService() {}
    @Override
    public  IBinder onBind(Intent intent){
        return new MusicControl();
    }
    @Override
    public void onCreate(){
        super.onCreate();
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//sd卡是否存在
            sdCardExist=Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        }
        if(sdCardExist)
        {
            File sdDir = Environment.getExternalStorageDirectory();//获取根目录
            sdDir.toString();
           // Log.e("main","得到的根目录路径:"+sdDir);
        }
        if (files == null){
            Log.e("error","空目录");
        }
        List<String> s = new ArrayList<>();
        for(int i =0;i<files.length;i++){
            //添加sd卡中的音乐到s中
            s.add(files[i].getName());
            System.out.println(files[i].getName());
        }
        player=new MediaPlayer();//创建音乐播放器对象
        player.reset();//重置player
        player.setLooping(true);//播放模式初始为单曲循环
    }

    class MusicControl extends Binder{//Binder是一种跨进程的通信方式
        /*private final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if (msg.what == LISTENER) {
                    isSongFinished();
                }
                return false;
            }
        });

        private void isSongFinished() {
            if(overFlag){
                if (cur_mode == 0) {
                    //单曲循环
                    if (!player.isLooping()) {
                        player.setLooping(true);
                    }
                }else if (cur_mode == 1){
                    //列表循环
                    if (player.isLooping()) {
                        player.setLooping(false);
                    }
                    nextSong();
                }else if(cur_mode == 2) {
                    //随机播放
                    if(player.isLooping()){
                        player.setLooping(false);
                    }
                    toNextRandomSong();
                }
            }
            //handler.sendEmptyMessageDelayed(LISTENER, 1000);
        }*/
        public void addTimer() { //添加计时器用于设置音乐播放器中的播放进度条
            if (timer == null) {
                timer = new Timer();//创建计时器对象
                TimerTask task = new TimerTask() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        if (player == null) return;
                        duration = player.getDuration();//获取歌曲总时长
                        currentPosition = player.getCurrentPosition();//获取播放进度
                        System.out.println("duration:"+duration);
                        System.out.println("currentDuration"+currentPosition);
                        if (Math.abs(currentPosition - duration) <= 1000) {//判断歌曲受否播放结束
                            overFlag = true;
                        }
                        isSongFinished();//当前歌曲播放结束后
                        Message msg = Music_Activity.handler.obtainMessage();//创建消息对象
                        //将音乐的总时长和播放进度、歌曲位置封装至消息对象中
                        Bundle bundle = new Bundle();
                        bundle.putInt("duration", duration);
                        bundle.putInt("currentPosition", currentPosition);
                       // bundle.putInt("imgPos",imgPos);
                        msg.setData(bundle);
                        //将消息发送到主线程的消息队列
                        Music_Activity.handler.sendMessage(msg);
                    }
                };
                //开始计时任务后的5毫秒，第一次执行task任务，以后每500毫秒执行一次
                timer.schedule(task, 5, 500);
            }
        }
        public void isSongFinished(){
            if(overFlag){
                System.out.println("isOver");
                if (cur_mode == 0) {
                    //单曲循环
                    if (!player.isLooping()) {
                        player.setLooping(true);
                    }
                }else if (cur_mode == 1){
                    //列表循环
                    if (player.isLooping()&&overFlag) {
                        player.setLooping(false);
                    }
                    try {
                        toNextSong();//播放下一首
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if(cur_mode == 2) {
                    //随机播放
                    if(player.isLooping()&&overFlag){
                        player.setLooping(false);
                    }
                    try {
                        toNextRandomSong();//随机播放下一首
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        public void initMusic(int pos) {//设置歌曲路径
           // path = Environment.getExternalStorageDirectory().getAbsolutePath();
            path="/storage/emulated/0/Music";
            path = path + "/" + files[pos].getName();
        }
        public void play(){
            try{
                if(player == null){
                    player = new MediaPlayer();
                }
                player.reset();//重置音乐播放器
                player.setDataSource(path);//设置播放路径
                player.prepare();// 音乐文件准备
                player.start();//播放音乐
               // imgPos = Music_Activity.i;
                addTimer();//添加计时器
                setPlayMode(cur_mode);
                overFlag = false;
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {//监听歌曲是否播放结束
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) { player.release();
                    }
                });
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        public void pausePlay(){
            player.pause();//暂停播放音乐
        }
        public void continuePlay(){
            player.start();//继续播放音乐
        }
        public void seekTo(int progress){
            player.seekTo(progress);//设置音乐的播放位置
        }
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void toNextSong() throws IOException {//列表循环播放下一首歌
            player.stop();
            if (Music_Activity.i == frag1.name.length-1) {
                Music_Activity.i = 0;
            } else {
                Music_Activity.i ++;
            }
            player = new MediaPlayer();
            initMusic(Music_Activity.i);
            play();
        }
        public void toNextRandomSong() throws IOException {//随机播放下一首歌
            player.stop();
            Random random = new Random();
            int size = frag1.name.length;
            int randomNum = random.nextInt(size);
            while (Music_Activity.i == random.nextInt(size)){//随机的下一首不能为当前正在播放的歌
                randomNum = random.nextInt(size);
            }
            Music_Activity.i = randomNum;//更新当前播放的歌曲在歌单中的位置
            player = new MediaPlayer();
            initMusic(Music_Activity.i);
            play();
        }
        public void setPlayMode(int mode) { cur_mode = mode;}
        public int getCurrentMode() {
            return cur_mode;
        }
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(player==null) return;
        if(player.isPlaying()) player.stop();//停止播放音乐
        player.release();//释放占用的资源
        player=null;//将player置为空
    }
}


