package com.westwell.server.dto;

import com.westwell.server.entity.WcCameraInfoEntity;
import com.westwell.server.entity.WcTaskEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailInfoDto {

    private WcTaskEntity taskEntity;

    private WcCameraInfoEntity cameraInfoEntity;
}
