package com.westwell.server.common.configs;

import com.westwell.api.common.utils.DateUtils;

import java.util.Date;

public class DataConfig {

    public static Integer CAMERA_NO = 1;

    public static String VIDEO_START_TIME = "2021-02-01 00:00:00";

    public static String VIDEO_END_TIME = "2021-02-01 00:00:02";

    public static Integer FRAME = 2;

    public static String PIC_CACHE_PATH = "/home/westwell/java/file/pics";

    public static String FFMPEG_PATH = "/usr/local/ffmpeg/bin/ffmpeg";

    public static Integer CLUSTER_NUM = 70;

    public static String TASK_PREFIX = "wellcare";

    public static String SUCCESS = "success";

    public static double MAX_SIMILARITY = 0.9;

    public static double MIN_SIMILARITY = 0.1;

//    代表学生身份的field
    public static String STUDENT_ID = "student_id";

//    保存输出文件的地址
    public static String IDENTIFY_CACHE_PATH = "/home/westwell/java/file/identify";



    public static void main(String[] args) {
        System.out.println(DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
    }

}
