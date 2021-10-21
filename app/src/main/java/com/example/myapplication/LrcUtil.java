package com.example.myapplication;

//歌词解析

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LrcUtil {
    public static List<LrcBean> parseStr2List(String lrcStr,TextView songName,TextView singerName) {
        String title,artist;
        if (TextUtils.isEmpty(lrcStr)) return null;
        List<LrcBean> list = new ArrayList<>();
        String lrcText = lrcStr.replaceAll("&#58;", ":")
                .replaceAll("&#10;", "\n")
                .replaceAll("&#46;", ".")
                .replaceAll("&#32;", " ")
                .replaceAll("&#45;", "-")
                .replaceAll("&#13;", "\r")
                .replaceAll("&#39;", "'")
                .replaceAll("&nbsp;", " ")  //空格替换
                .replaceAll("&apos;", "'")  //分号替换
                .replaceAll("&&", "/")      //空格替换
                .replaceAll("\\|", "/");
        String[] split = lrcText.split("\n");
        boolean isWithTranslation = false;
        for (int i = 0; i < split.length; i++) {
            String lrcInfo = split[i];
            if (" ".equals(lrcInfo) || TextUtils.isEmpty(lrcInfo)) continue;
            if ( lrcInfo.contains("[al:") || lrcInfo.contains("[by:")) {
                continue;
            }
            if(lrcInfo.contains("[ti:")) {
                title = lrcInfo.substring(lrcInfo.indexOf(":") + 1, lrcInfo.indexOf("]"));
                songName.setText(title);
            }
            if(lrcInfo.contains("[ar:")) {
                artist = lrcInfo.substring(lrcInfo.indexOf(":") + 1, lrcInfo.indexOf("]"));
                singerName.setText(artist);
            }
            String lrc = lrcInfo.substring(lrcInfo.indexOf("]") + 1);
            if (TextUtils.isEmpty(lrc) || " ".equals(lrc) || "//".equals(lrc)) continue;

            //Log.d(i+"行",lrc);

            String min = lrcInfo.substring(lrcInfo.indexOf("[")+1,lrcInfo.indexOf("[")+3);
            String seconds = lrcInfo.substring(lrcInfo.indexOf(":")+1,lrcInfo.indexOf(":")+3);
            String mills = lrcInfo.substring(lrcInfo.indexOf(".")+1,lrcInfo.indexOf(".")+3);
            long startTime = Long.parseLong(min) * 60 * 1000 + Long.parseLong(seconds) * 1000 + Long.parseLong(mills) *10;
            LrcBean lrcBean = new LrcBean (lrc,startTime);
            list.add(lrcBean);
            if(list.size() > 1) list.get(list.size() -2 ).setEnd(startTime);
            if(i == split.length -1 ) list.get(list.size() - 1).setEnd(startTime + 100000);
        }
        return list;
    }

    public static String parseLrcFile(String songname) {
        /*if (TextUtils.isEmpty(MusicPath) || !MusicPath.contains(".lrc")) return null;
        Log.i("NIFO","扫描歌词");
        System.out.println("你的歌词存储路径——》" + MusicPath);*/

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),songname+".lrc");
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        BufferedReader reader;
        StringBuilder LrcStr = new StringBuilder();
        try {
            fis = new FileInputStream(file.getCanonicalPath());
            bis = new BufferedInputStream(fis);
            reader = new BufferedReader(new InputStreamReader(bis, StandardCharsets.UTF_8));
            String str = reader.readLine();
            while (str != null) {
                LrcStr.append(str).append("\n");
                str = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                    if (bis != null) bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return LrcStr.toString();
    }
};
