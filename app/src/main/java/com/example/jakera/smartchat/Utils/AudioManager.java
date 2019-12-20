package com.example.jakera.smartchat.Utils;


import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by jakera on 18-2-2.
 */

/**
 * 使用MediaRecorder类录音，输出amr格式文件。录音20s，输出的音频文件大概为33K（已写测试代码）
 * ffmpeg -i D:\Testaudio\dizi.mp3 -ar 8000 -ac 1 -acodec pcm_u8 d:\testaudio\dizi.wav
 * linux下，可用sox对音频文件进行转换及处理
 */

public class AudioManager {
    private MediaRecorder mMediaRecorder;

    private String mDir;
    private String mCurrentFilePath;

    private boolean isPrepared;

    private String TAG="AudioManager";

    private static AudioManager mInstance;
    private AudioManager(String dir){
        mDir = dir;
    }

    /**
     * 回调准备完毕
     * @author Administrator
     *
     */
    public interface AudioStateListener{
        void wellPrepared();
    }

    public AudioStateListener mListener;

    public void setOnAudioStateListener(AudioStateListener listener){
        mListener = listener;
    }



    public static AudioManager getInstance(String dir){
        if (mInstance == null) {
            synchronized (AudioManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioManager(dir);
                }
            }
        }
        return mInstance;
    }

    public void prepareAudio(){

        try {
            isPrepared = false;
            File dir = new File(mDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = generateFileName();
            File file = new File(dir, fileName);

            mCurrentFilePath = file.getAbsolutePath();
            mMediaRecorder = new MediaRecorder();
            //设置输出文件
            mMediaRecorder.setOutputFile(file.getAbsolutePath());
            //设置MediaRecorder的音频源为麦克风
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置音频的格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            //设置采样率
            mMediaRecorder.setAudioSamplingRate(8000);
            //设置音频的编码为amr
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.prepare();

            mMediaRecorder.start();
            //准备结束
            isPrepared = true;
            if (mListener != null) {
                mListener.wellPrepared();
            }

        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 随机生成文件的名称
     * @return
     */
    private String generateFileName() {
        // TODO Auto-generated method stub
        return UUID.randomUUID().toString() + ".amr";
        //return "123.amr";
    }



    public int getVoiceLevel(int maxLevel){
        if (isPrepared) {
            try {
                //mMediaRecorder.getMaxAmplitude() 1-32767
                return maxLevel*mMediaRecorder.getMaxAmplitude()/32768+1;
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
        return 1;
    }

    public void release(){
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    public void cancel(){
        release();
        if (mCurrentFilePath != null) {
            File file = new File(mCurrentFilePath);
            file.delete();
            mCurrentFilePath = null;
        }

    }

    public String getCurrentFilePath() {
        // TODO Auto-generated method stub
        return mCurrentFilePath;
    }
}
