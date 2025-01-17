package com.westwell.server.service;

import com.westwell.server.dto.TaskDetailInfoDto;

import java.util.List;

public interface VideoMediaService {

    //    剪切视频的帧
    boolean cutVideoToPics(TaskDetailInfoDto task);

    //    把图片保存到redis
    List<String> writePicsToRedis(TaskDetailInfoDto task) throws Exception;

    void readPicsFromRedis(TaskDetailInfoDto task, List<String> picKeys, String path) throws Exception;

    boolean clearListInRedis(List<String> picKeyList);

    void readPicCollesFromRedis(TaskDetailInfoDto task);

    void readBasePicsFromRedis(TaskDetailInfoDto task);

    //    下载视频
    boolean readVideo(TaskDetailInfoDto task);

    void readLabelPicCollesFromRedis(TaskDetailInfoDto task);

    //读取单个图片
    void readPicFromRedis(String picKey, String path) throws Exception;
}
