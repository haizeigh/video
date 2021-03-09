package com.westwell.server.common.configs;

import com.westwell.api.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component(value = "DataConfig")
public class DataConfig {

//    摄像头编号
    public static Integer CAMERA_NO = 1;

//    视频开始时间
    public static String VIDEO_START_TIME = "2021-02-23 11:30:00";

    public static String VIDEO_END_TIME = "2021-02-23 11:30:30";

    //    一段视频切分的间隔
    public static int INTERVAL_MINUTE = 60;

//    一秒截图数
    public static Double FRAME = 0.2;

    public static String DASH = "-";


//    ffmpeg地址
    public static String FFMPEG_PATH;
    @Value("${ffmpeg.path}")
    public void setFFMPEG_PATH(String ffmpegPath) {
        DataConfig.FFMPEG_PATH = ffmpegPath;
    }


    //    保存输出文件的地址
    public static String IDENTIFY_CACHE_PATH ;
    @Value("${identify.cache.path}")
    public void setIDENTIFY_CACHE_PATH(String identifyCachePath) {
        DataConfig.IDENTIFY_CACHE_PATH = identifyCachePath;
    }

    public static String TEST_VIDEO ;
    @Value("${test.video}")
    public void setTEST_VIDEO(String testVideo) {
        DataConfig.TEST_VIDEO = testVideo;
    }


//    临时底库的群数目
    public static Integer CLUSTER_NUM = 40;

//    业务前缀
    public static String TASK_PREFIX = "wellcare";

    public static String SUCCESS = "success";
    public static int SUCCESS_CODE = 0;

//    图像分辨率
    public static String PIXELS = "2048x1536";

//    人物相似度的阀值
    public static double MAX_SIMILARITY = 0.67;

    public static double MIN_SIMILARITY = 0.6;

    public static double MIN_IOU = 0.04;

    public static double STUDENT_SIMILARITY = 0.6;

//    比对底库的频率
    public static int INTER_FRE = 1;

//    代表学生身份的field
    public static String STUDENT_ID = "student_id";

    //   redis中保存图片的字段field
    public static String PIC = "pic";



    public static String WELL_CARE_BASE = "wellcare:base";

//    redis的location字段 保存坐标
    public static String LOCATION = "location";




    public static void main(String[] args) {
        System.out.println(DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
    }

}
