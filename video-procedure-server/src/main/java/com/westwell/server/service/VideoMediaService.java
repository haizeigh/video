package com.westwell.server.service;

import com.westwell.server.dto.TaskDetailInfoDto;

import java.util.List;

public interface VideoMediaService {

    //    剪切视频的帧
    boolean cutVideoToPics(TaskDetailInfoDto task);

    //    把图片保存到redis
    List<String> writePicsToRedis(TaskDetailInfoDto task) throws Exception;

    void readPicsFromRedis(TaskDetailInfoDto task, List<String> faceKeys) throws Exception;

    boolean clearListInRedis(List<String> picKeyList);

    void readPicCollesFromRedis(TaskDetailInfoDto task);

    void readBasePicsFromRedis(TaskDetailInfoDto task);

    //    下载视频
    boolean readVideo(TaskDetailInfoDto task);

    void readfacesCollesFromRedis(TaskDetailInfoDto task);
}
