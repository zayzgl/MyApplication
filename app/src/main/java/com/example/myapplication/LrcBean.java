package com.example.myapplication;

//歌词

public class LrcBean {
    private String lrc;
    private String translateLrc;
    private long start;
    private long end;

    public LrcBean(String lrc,long start) {
        this.lrc = lrc;
        this.start = start;
    }

    public String getLrc() {
        return lrc;
    }

    public void setLrc(String lrc) {
        this.lrc = lrc;
    }

    public String getTranslateLrc() {
        return translateLrc;
    }

    public void setTranslateLrc(String translateLrc) {
        this.translateLrc = translateLrc;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
}
