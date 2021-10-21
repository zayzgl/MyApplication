package com.example.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.util.List;

public class LrcView extends View {
    private static final String TAG = "LrcView";

    private List<LrcBean> list;
    private TextPaint gPaint; //普通
    private TextPaint hPaint; //高亮
    private int width = 0, height = 0;
    private int currentPosition = 0;
    private MediaPlayer player;
    private int lastPosition = 0;
    private int highLineColor;
    private int lrcColor;
    private int mode = 0;
    private final static int textSize = 42;
    private final static int rowSpacing = 110;//行距宽度;
    private boolean isTouched = false,Tips = false,isFirstShow = true;

    public void setHighLineColor(int highLineColor) {
        this.highLineColor = highLineColor;
    }

    public void setLrcColor(int lrcColor) {
        this.lrcColor = lrcColor;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setTouched(boolean touched) {
        isTouched = touched;
    }

    public void setPlayer(MediaPlayer player) {this.player = player;}
    /**
     * 显示加载提示 {"加载歌词中..."}
     * @param tips boolean 是否显示加载歌词提示
     * */
    public void setTips(boolean tips) {
        if (Tips) return;

        this.Tips = tips;
        if (list != null) {
            if (list.size() > 0) list.clear();
        }
        Log.d("LrcView", "加载歌词提示");
        init();
    }

    /**
     * 设置标准歌词字符串
     *【判断】什么时候不更新歌词，当前歌词与获取到的歌词一模一样 或者 当前显示获取歌词提示{boolean Tips}
     * @param lrcBeans LrcBean形式的歌词
     * @return true 表示可以更新歌词， false则不更新歌词
     */
    public boolean setLrc(List<LrcBean> lrcBeans) {
        if (!MatchesLrcBeans(lrcBeans)) {
            if (list != null && list.size() > 0) list.clear();
            this.list = lrcBeans;
            if (Tips) Tips = false;
            if (isFirstShow) isFirstShow = false;
            Log.d("LrcView", "在线歌词更新成功");
            return true;
        }else return false;
    }
    /**
     * 判断两个List<LrcBean>是否相同
     * @param lrcBeans LrcBean形式的歌词
     * @return true 表示两个List<LrcBean>相同， false则不相同
     * */
    public boolean MatchesLrcBeans(List<LrcBean> lrcBeans){
        if(list == null && lrcBeans == null) return false;//1.都为空 返回true
        //2.list或者lrcBeans任意一个为空，其size不相等 返回false
        if (list == null || lrcBeans == null || list.size() != lrcBeans.size()) return false;
        //3.当 它们的size都为0时 返回 true
        if (list.size() == 0) return true;

        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getStart() != lrcBeans.get(i).getStart()) return false;
            if(list.get(i).getEnd() != lrcBeans.get(i).getEnd()) return false;
            if(!list.get(i).getLrc().equals(lrcBeans.get(i).getLrc())) return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public LrcView(Context context) {
        this(context, null);
        Log.d(TAG, "LrcView: 1");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "LrcView: 3");
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LrcView);
        highLineColor = ta.getColor(R.styleable.LrcView_highLineColor, getResources().getColor(R.color.white,null));
        lrcColor = ta.getColor(R.styleable.LrcView_lrcColor, getResources().getColor(android.R.color.darker_gray,null));
        mode = ta.getInt(R.styleable.LrcView_lrcMode,mode);
        ta.recycle();
        gPaint = new TextPaint();
        gPaint.setAntiAlias(true);
        gPaint.setColor(lrcColor);
        gPaint.setTextSize(textSize);//像素为单位
        gPaint.setTextAlign(Paint.Align.CENTER);
        hPaint = new TextPaint();
        hPaint.setAntiAlias(true);
        hPaint.setColor(highLineColor);
        hPaint.setTextSize(textSize);
        hPaint.setTextAlign(Paint.Align.CENTER);
    }
    /**
     * 绘制歌词
     * */
    @Override
    protected void onDraw(Canvas canvas) {
        if (width == 0 || height == 0) {
            width = getMeasuredWidth();
            height = getMeasuredHeight();
            Log.d(TAG, "onDraw: "+width+" "+(width / textSize));
        }
        //暂无歌词与加载歌词提示
        if (list == null || list.size() == 0) {
            if (!Tips) {
                gPaint.setFlags(Paint.UNDERLINE_TEXT_FLAG);//FLAG ：带下划线
                canvas.drawText("暂无歌词", width >> 1, height >> 1, gPaint);
            }else {canvas.drawText("加载歌词中...", width >> 1, height >> 1, gPaint); }
            return;
        }
        //绘制歌词
        if (!isTouched) {
            gPaint.setFlags(Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            //获取当前的播放进度 和 到该播放哪句歌词的位置list.get(x) -> currentPosition
            int currentMillis = getCurrentPosition();
            //画出高亮和普通部分歌词
            drawLrc(canvas);
            //获得正唱歌词的开始时间（当前播放进度应该显示的歌词）
            long start = list.get(currentPosition).getStart();
            long different = currentMillis - start;
            int differentPosition = currentPosition - lastPosition;
            //Log.d(TAG, "onDraw: "+currentMillis+", "+start);
            /*
             * v : 行与行之间的跳转速度
             * different <= 500(ms): 高亮歌词才开始唱的时候
             * different > 500(ms) : 高亮歌词至少唱了 500ms 直到该句歌词结束
             * differentPosition 不能少，在调整进度时，会有平滑滚动过渡*/
            float v = different > 500 ? currentPosition * rowSpacing :
                    (lastPosition + differentPosition * (different / 500f)) * rowSpacing;
            //if( different <= 500 ) Log.d(TAG, "onDraw: "+(different / 500f)+" "+differentPosition);
            //Log.d(TAG, "v = "+v+", "+((currentMillis - start) > 500));
            setScrollY((int) v);//设置滚动速度
            /*
             * {@link getScrollY()} 返回此视图的滚动顶部位置。
             * 这是视图显示部分的上边缘。你不需要在上面画任何像素，因为它们在屏幕上你的视图框架之外。*/
            if (getScrollY() == currentPosition * rowSpacing) {
                //Log.d("", "onDraw: lastPosition"+lastPosition);
                lastPosition = currentPosition;
            }
        }
        postInvalidateDelayed(100);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (list != null) {
            list.clear();
            list = null;
        }
        if (gPaint != null) {
            gPaint.reset();
            gPaint = null;
        }
        if (hPaint != null) {
            hPaint.reset();
            hPaint = null;
        }
        System.gc();
    }

    private void drawLrc(Canvas canvas/*, boolean translation, int currentMillis*/) {
        float x = width >> 1,y;
        String lrc;
        TextPaint paint;
        for (int i = 0; i < list.size(); i++) {
            y = height / 2 + rowSpacing * i;
            //Log.d(TAG, "drawLrc: "+y+", "+i);
            lrc = list.get(i).getLrc();
            paint = i == currentPosition ? hPaint : gPaint;
            canvas.drawText(lrc, x, y, paint);
        }
    }

    public void init() {
        currentPosition = 0;
        lastPosition = 0;
        setScrollY(0);
        invalidate();
    }

    /**根据player的播放位置，获得歌词的播放位置（与startTime比较）*/
    private int getCurrentPosition() {
        int currentMillis;
        try {
            //1.首先确定当前的播放进度
            currentMillis = player.getCurrentPosition();
            //2.根据播放进度确定该高亮显示哪条歌词（currentPosition）
            if (currentMillis < list.get(0).getStart()) {
                currentPosition = 0;
                return currentMillis;
            }
            if (currentMillis > list.get(list.size() - 1).getStart()) {
                currentPosition = list.size() - 1;
                return currentMillis;
            }
            for (int i = 0; i < list.size(); i++) {
                if (currentMillis >= list.get(i).getStart() && currentMillis < list.get(i).getEnd()) {
                    currentPosition = i;
                    return currentMillis;
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            postInvalidateDelayed(100);
        }
        return 0;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
        //触摸事件
    }
}
/*
import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class LrcView extends View {
    private TextView songName,singerName;
    private TextView lyricPrev,lyricCurrent,lyricNext;
    private LrcUtil lrc = new LrcUtil();
    private List<LrcBean> list;
    private MediaPlayer player;

    public LrcView(Context context)
    {
        super(context,null);
    }
    public void ondraw(String name,MediaPlayer player)
    {
        songName=findViewById(R.id.tv_song_name);
        singerName=findViewById(R.id.tv_song_singer);
        lyricPrev=findViewById(R.id.lyric_previous);
        lyricCurrent=findViewById(R.id.lyric_current);
        lyricNext=findViewById(R.id.lyric_next);
        String ori_lrc = lrc.parseLrcFile(name);
        lrc.parseStr2List(ori_lrc,songName,singerName);
        this.list=lrc.parseStr2List(ori_lrc,songName,singerName);
        this.player = player;
        setLyric();
    }
    protected void setLyric() {
        while (player.isPlaying()) {
            int time = player.getCurrentPosition();
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
    }}*/
