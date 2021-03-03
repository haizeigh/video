package com.westwell.server.controller;


import com.westwell.server.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 任务记录表
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */
@Slf4j
@RestController
@RequestMapping("wctask")
public class WcTaskController {

    @Resource
    WcTaskService wcTaskService;

    @Resource
    VideoMediaService videoMediaService;

    @Resource
    DetectionService detectionService;

    @Resource
    FeatureService featureService;

    @Resource
    IdentifyService identifyService;

    @Resource
    ResultDumpService resultDumpService;

    @Resource
    VideoProcessService videoProcessService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    WcTaskManagerService taskManagerService;

    /**
     * 列表
     */
    @RequestMapping("/start")
    public String list(@RequestParam Map<String, Object> params) {


        List<Integer> cameraNos = new ArrayList<>();
        cameraNos.add(1);
        taskManagerService.routerCamera(cameraNos);

        return "success";
    }




}
