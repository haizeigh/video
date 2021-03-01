package com.westwell.server.dto;

import com.google.common.base.Strings;
import com.westwell.api.common.utils.DateUtils;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.entity.WcCameraInfoEntity;
import com.westwell.server.entity.WcTaskEntity;
import lombok.*;

import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskDetailInfoDto {

    private WcTaskEntity taskEntity;

    private WcCameraInfoEntity cameraInfoEntity;

    private String taskCameraPrefix;

    public String getTaskCameraPrefix(){

        if (!Strings.isNullOrEmpty(taskCameraPrefix)){
            return taskCameraPrefix;
        }
        StringBuilder stringBuilder = new StringBuilder();
        String taskCamera = stringBuilder.append(DataConfig.TASK_PREFIX)
                .append(":")
                .append(taskEntity.getTaskNo())
                .append(":")
                .append(taskEntity.getCameraNo()).toString();
        this.taskCameraPrefix = taskCamera;
        return taskCamera;
    }

    private String taskPath;



    private String taskTemptPath;

    private String picPath;
    private String picName;

    private String videoPath;
    private String videoName;


    public String getTaskPath() {

        if (Strings.isNullOrEmpty(taskPath)){
            synchronized (this){
                if (Strings.isNullOrEmpty(taskPath)){
                    String taskPath = DataConfig.IDENTIFY_CACHE_PATH
                            + "/" + DateUtils.format(new Date(), DateUtils.DATE_PATTERN)
                            + "/" + taskEntity.getCameraNo()
                            + "/" + DateUtils.format(taskEntity.getVideoStartTime(), DateUtils.DATE_TIME)
                            + "-" + DateUtils.format(taskEntity.getVideoEndTime(), DateUtils.DATE_TIME) ;
                    this.taskPath = taskPath;
                    return taskPath;
                }
            }

        }
        return taskPath;
    }

    public String getPicPath() {
        return getTaskPath() + "/pic";
    }

    public String getVideoPath() {
        return getTaskPath() + "/video";
    }

    public String getPicName() {
        return "%09d.jpeg";
    }

    public String getVideoName() {
        return "video.avi";
    }

    public String getTaskTemptPath() {
        return getTaskPath() + "/tempt";
    }


}
