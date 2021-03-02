package com.westwell.server.service;

import com.westwell.server.dto.RouterCameraResultDto;

import java.util.concurrent.Future;

/**
 * 任务记录表
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */
public interface WcVideoManagerService {


    Future<RouterCameraResultDto> routerGap(Integer cameraNo, Integer taskNo);
}

