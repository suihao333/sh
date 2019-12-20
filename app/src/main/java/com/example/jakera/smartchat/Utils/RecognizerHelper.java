package com.example.jakera.smartchat.Utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.example.jakera.smartchat.Entry.AnimalOrPlant;
import com.example.jakera.smartchat.Entry.Car;
import com.example.jakera.smartchat.Entry.Dish;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by jakera on 18-1-25.
 */


/**
 * 科大讯飞，现在（2017年1月）支持且仅支持pcm和wav格式的音频流文件。
   那么我要从视频中获取文字， 所以需要先从视频中提取格式为pcm或wav格式的音频流文件。
 */

public class RecognizerHelper {
    private SpeechRecognizer mSpeechRecognizer;
    private RecognizerDialog mRecognizerDialog;
    private RecognizerDialogListener mRListener;
    private AudioDecode audioDecode;

    public final static String type = "type";
    public final static int CAR = 0;
    public final static int ANIMAL = 1;
    public final static int PLANT = 2;
    public final static int FOOD = 3;


    private String TAG="RecognizerHelper";

    private int re_number;

    private Context context;

    private MyRecognizerListener myRecognizerListener;


    public RecognizerHelper(Context context,SpeechRecognizer speechRecognizer,RecognizerDialog recognizerDialog) {
        this.context=context;
        this.mSpeechRecognizer = speechRecognizer;
        this.mRecognizerDialog=recognizerDialog;
    }

    public RecognizerHelper(SpeechRecognizer speechRecognizer) {
        this.mSpeechRecognizer = speechRecognizer;
        myRecognizerListener = new MyRecognizerListener();
    }

    public void setListener(RecognizerDialogListener listener){
        this.mRecognizerDialog.setListener(listener);
    }

    public void setVoiceToTextListener(getVoiceToTextResult listener) {
        this.myRecognizerListener.setListener(listener);
    }

    public interface getVoiceToTextResult {
        void getRecognizeResult(String result);
    }

    public void startSpeechRecognizer(){
        setIatParam();
        mRecognizerDialog.show();
    }


    private void setIatParam(){
        mRecognizerDialog.setParameter(SpeechConstant.PARAMS,null);

        mRecognizerDialog.setParameter(SpeechConstant.ENGINE_TYPE,SpeechConstant.TYPE_CLOUD);

        mRecognizerDialog.setParameter(SpeechConstant.RESULT_TYPE,"json");

        mRecognizerDialog.setParameter(SpeechConstant.LANGUAGE,"zh_ch");

        mRecognizerDialog.setParameter(SpeechConstant.ACCENT,"mandarin");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mRecognizerDialog.setParameter(SpeechConstant.VAD_BOS,"4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mRecognizerDialog.setParameter(SpeechConstant.VAD_EOS,"2000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mRecognizerDialog.setParameter(SpeechConstant.ASR_PTT,"1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mRecognizerDialog.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");

        mRecognizerDialog.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageState()+"/smart_chat_recorder_audios/"+generateFileName()+".wav");
    }

    /**
     * {"sn":1,"ls":false,"bg":0,"ed":0,"ws":[{"bg":0,"cw":[{"sc":0.00,"w":"今天"}]},{"bg":0,"cw":[{"sc":0.00,"w":"天气"}]},{"bg":0,"cw":[{"sc":0.00,"w":"好"}]},{"bg":0,"cw":[{"sc":0.00,"w":"吗"}]}]}
     *
     * @param json
     * @return
     */
    public static String parseIatResult(String json){
        StringBuffer ret=new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);
            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }

    /**
     *识别asset下的语音
     * @param filename
     */

    public void recognizeStream(String filename){
        mSpeechRecognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
        re_number = mSpeechRecognizer.startListening(myRecognizerListener);
        if (re_number!= ErrorCode.SUCCESS){
            Log.i(TAG,"fail to recognizer.....");
        }else {
            //byte[] audioData=RecognizerUtil.readAudioFile(context,filename);
            byte[] audioData = RecognizerUtil.readAudioFilePath(filename);
            Log.i(TAG,"recognizer......");
            if (audioData!=null){
                // 一次（也可以分多次）写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），
                // 位长16bit，单声道的wav或者pcm
                // 写入8KHz采样的音频时，必须先调用setParameter(SpeechConstant.SAMPLE_RATE, "8000")设置正确的采样率
                // 注：当音频过长，静音部分时长超过VAD_EOS将导致静音后面部分不能识别。
                // 音频切分方法：FucUtil.splitBuffer(byte[] buffer,int length,int spsize);
                Log.i(TAG,"audioData is not null,length:"+audioData.length);
                mSpeechRecognizer.writeAudio(audioData,0,audioData.length);
                mSpeechRecognizer.stopListening();
            }else {
                mSpeechRecognizer.cancel();
                Log.i(TAG,"fail to read stream......");
            }
        }
    }

    //参考科大迅飞Demo   https://github.com/leoleohan/Voice2Txt
    public void recognizerFromAmr(String path) {

        try {

            mSpeechRecognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
            //记得在录制时也要设置好采样率
            mSpeechRecognizer.setParameter(SpeechConstant.SAMPLE_RATE, "8000");
            mSpeechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, path);

            audioDecode = AudioDecode.newInstance();

            File file = new File(path);
            Log.i(TAG, path);
            if (file.exists()) {
                Log.i(TAG, "file exists.");
            } else {
                Log.w(TAG, "file not exists!!!");
                return;
            }

            audioDecode.prepare(path);
            audioDecode.setOnCompleteListener(new AudioDecode.OnCompleteListener() {
                @Override
                public void completed(final ArrayList<byte[]> pcmData) {
                    re_number = mSpeechRecognizer.startListening(myRecognizerListener);
                    if (re_number != ErrorCode.SUCCESS) {
                        Log.i(TAG, "fail to recognizer.....");
                    } else {
                        Log.i(TAG, "recognizer......");
                        for (byte[] data : pcmData) {
                            final ArrayList<byte[]> buffers = RecognizerUtil.splitBuffer(data, data.length, 4800);
                            for (byte[] buffer : buffers) {
                                Log.i(TAG, "正在转码");
                                mSpeechRecognizer.writeAudio(buffer, 0, buffer.length);
                            }
                        }
                        mSpeechRecognizer.stopListening();
                    }
                    audioDecode.release();
                }
            });
            audioDecode.startAsync();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    /**
     * 听写监听器
     */
    private class MyRecognizerListener implements RecognizerListener {

        private getVoiceToTextResult listener;

        public void setListener(getVoiceToTextResult listener) {
            this.listener = listener;
        }
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
            Log.i(TAG,"you are talking now,and the  volume is:"+i);
        }

        @Override
        public void onBeginOfSpeech() {
            Log.i(TAG,"start talking.....");
        }

        @Override
        public void onEndOfSpeech() {
            Log.i(TAG,"stop talking.....");
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            String result = parseIatResult(recognizerResult.getResultString());
            listener.getRecognizeResult(result);
        }

        @Override
        public void onError(SpeechError speechError) {
            Log.i(TAG,"errorcode:"+speechError.getErrorCode()+",description:"+speechError.getErrorDescription()+",speechError:"+speechError.toString());
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };


    /**
     * 随机生成文件的名称
     * @return
     */
    private String generateFileName() {
        // TODO Auto-generated method stub
        return UUID.randomUUID().toString();
    }


    /**
     * 百度识别接口
     *
     * @param filePath 待识别图片
     * @return
     */
    public static void carRecognize(String filePath, CarRecognizeListener listener) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/car";
        try {
            // 本地文件路径
//            String filePath = "[本地文件路径]";
            byte[] imgData = BaiduFileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            String param = "image=" + imgParam + "&top_num=" + 5;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = BaiduAuthService.getAuth();

            String result = HttpUtil.post(url, accessToken, param);
            Car car = new Car();
            car = GsonUtils.fromJson(result, Car.class);
            listener.onSuccess(car);
            // Log.i("车型识别",car.result.get(0).score+car.result.get(0).name+car.color_result);
            //System.out.println(result);
            //03-09 20:32:28.111 31568-32355/com.example.jakera.smartchat I/System.out: {"log_id": 1961208421427382323, "result": [{"score": 0.9966436624527, "name": "兰博基尼Aventador", "year": "2007"}, {"score": 0.0014182010199875, "name": "三菱Endeavor", "year": "2013"}, {"score": 0.00065424933563918, "name": "兰博基尼Murcielago", "year": "2004-2010"}, {"score": 0.00056240992853418, "name": "兰博基尼Reventon", "year": "无年份信息"}, {"score": 0.00025521276984364, "name": "兰博基尼Huracan", "year": "2015"}], "color_result": "蓝色"}

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public interface CarRecognizeListener {
        public void onSuccess(Car car);
    }


    /**
     * 动物识别
     */

    public static void animalRecognize(String filePath, AnimalOrPlantRecognizeListener listener) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/animal";
        try {
            byte[] imgData = BaiduFileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            String param = "image=" + imgParam + "&top_num=" + 6;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = BaiduAuthService.getAuth();

            String result = HttpUtil.post(url, accessToken, param);
            AnimalOrPlant animalOrPlant = new AnimalOrPlant();
            animalOrPlant = GsonUtils.fromJson(result, AnimalOrPlant.class);
            listener.onSuccess(animalOrPlant);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface AnimalOrPlantRecognizeListener {
        public void onSuccess(AnimalOrPlant animalOrPlant);
    }

    /**
     * 植物识别
     *
     * @param filePath
     * @param listener
     */
    public static void plantRecognize(String filePath, AnimalOrPlantRecognizeListener listener) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/plant";
        try {
            byte[] imgData = BaiduFileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            String param = "image=" + imgParam;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = BaiduAuthService.getAuth();

            String result = HttpUtil.post(url, accessToken, param);
            AnimalOrPlant animalOrPlant = new AnimalOrPlant();
            animalOrPlant = GsonUtils.fromJson(result, AnimalOrPlant.class);
            listener.onSuccess(animalOrPlant);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 菜品识别
     *
     * @param filePath
     * @return 03-13 14:29:43.360 18891-24920/com.example.jakera.smartchat I/System.out: {"log_id": 3368823822981853804, "result_num": 1, "result": [{"calorie": "0", "has_calorie": true, "name": "非菜", "probability": "0.670065"}]}
     */
    public static void dishRecognize(String filePath, DishRecognizeListener listener) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/image-classify/v2/dish";
        try {

            byte[] imgData = BaiduFileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            String param = "image=" + imgParam + "&top_num=" + 5;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = BaiduAuthService.getAuth();

            String result = HttpUtil.post(url, accessToken, param);

            Dish dish = new Dish();
            dish = GsonUtils.fromJson(result, Dish.class);
            listener.onSuccess(dish);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface DishRecognizeListener {
        public void onSuccess(Dish dish);
    }









}
