package com.westwell.server.dto;

import com.google.common.base.Strings;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.entity.WcCameraInfoEntity;
import com.westwell.server.entity.WcTaskEntity;
import lombok.*;

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
}
