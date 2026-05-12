package com.sunyard.framework.video.util;

import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.encode.enums.X264_PROFILE;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.info.VideoInfo;

import java.io.File;

/**
 * @author zyl
 */
public class VideoUtil {

    /**
     * 将别的格式的文件，转换为mp4的文件
     *
     * @param originalFilePath 原路径
     * @param resultFilePath   暂存路径
     * @param bitRate          压缩倍率
     */
    public static void convertVideoToMP4(String originalFilePath, String resultFilePath, Integer bitRate) {
        File originalFile = new File(originalFilePath);
        MultimediaObject multimediaObject = new MultimediaObject(originalFile);
        MultimediaInfo info = null;
        try {
            info = multimediaObject.getInfo();
            VideoInfo videoInfo = info.getVideo();
            //视频
            VideoAttributes video = new VideoAttributes();
            //设置视频编码
            video.setCodec("h264");

            video.setX264Profile(X264_PROFILE.MAIN);
            File resultFile = new File(resultFilePath);
            //音频
            AudioAttributes audio = new AudioAttributes();
            //设置编码器名称
            audio.setCodec("aac");
            EncodingAttributes attrs = new EncodingAttributes();

            //设置转换后的格式
            attrs.setOutputFormat("mp4");
            attrs.setAudioAttributes(audio);
            attrs.setVideoAttributes(video);
            Encoder encoder = new Encoder();
            encoder.encode(multimediaObject, resultFile, attrs);
        } catch (EncoderException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将别的格式的文件，转换为mp3的文件
     *
     * @param originalFilePath
     * @param resultFilePath
     * @throws EncoderException
     */
    public static void convertAudioToMP3(String originalFilePath, String resultFilePath) {

        File originalFile = new File(originalFilePath);
        MultimediaObject multimediaObject = new MultimediaObject(originalFile);

        // Audio Attributes 音频编码属性
        AudioAttributes audio = new AudioAttributes();

        File resultFile = new File(resultFilePath);
        // Encoding attributes 编码属性
        EncodingAttributes attrs = new EncodingAttributes();
        //转换格式
        attrs.setOutputFormat("mp3");
        attrs.setAudioAttributes(audio);

        // Encode编码
        Encoder encoder = new Encoder();
        try {
            encoder.encode(multimediaObject, resultFile, attrs);
        } catch (EncoderException e) {
            throw new RuntimeException(e);
        }
    }

}
