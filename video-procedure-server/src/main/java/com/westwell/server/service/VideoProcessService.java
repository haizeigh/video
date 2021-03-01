package com.westwell.server.service;

import com.westwell.server.dto.TaskDetailInfoDto;

import java.util.Date;
import java.util.concurrent.Future;

public interface VideoProcessService {

    Future<TaskDetailInfoDto> detectVideo(Integer cameraNo, Date videoStartTime, Date videoEndTime) ;

}
