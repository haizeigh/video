package com.westwell.server.controller;


import com.westwell.api.common.utils.DateUtils;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.entity.WcTaskEntity;
import com.westwell.server.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 任务记录表
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */
@RestController
@RequestMapping("wctask")
public class WcTaskController {

    @Autowired
    private WcTaskService wcTaskService;


    @Resource
    VideoMediaService videoMediaService;

    @Resource
    FaceDetectionService faceDetectionService;

    @Resource
    FaceFeatureService faceFeatureService;

    @Resource
    FaceIdentifyService faceIdentifyService;

    @Resource
    ResultDumpService resultDumpService;
    /**
     * 列表
     */
    @RequestMapping("/list")
    public WcTaskEntity list(@RequestParam Map<String, Object> params) {

        WcTaskEntity wcTaskEntity = wcTaskService.queryOne();

        long start = System.currentTimeMillis();
        try {
//            处理截图
            TaskDetailInfoDto task = wcTaskService.getOneTaskDetailInfoDto();

//            IdentifyFacesContainer.addPicToNewBucket("33", task);


            String picPath = DataConfig.PIC_CACHE_PATH
                    + "/" + DateUtils.format(new Date(), DateUtils.DATE_PATTERN
                    + "/" + task.getTaskEntity().getCameraNo());
            videoMediaService.cutVideoToPics(task, picPath);
            List<String> picKeyList = videoMediaService.writePicsToRedis(task, picPath);
            System.out.println(picKeyList);

//            检测人脸
            List<String> faceKeyList = faceDetectionService.detectFacesInPic(picKeyList);
//            保存面部图
            faceDetectionService.storeFaces(faceKeyList);
            videoMediaService.clearPicsInRedis(picKeyList);

//            特征提取
            faceFeatureService.extractFaceFeature(faceKeyList);

//            底库创建以及识别人物
            faceIdentifyService.identifyFaces(task, faceKeyList);

//            导出数据
            resultDumpService.dumpTaskTemptResult(task);
            resultDumpService.dumpTaskFinalResult(task);



        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("总耗时 ：" + (end - start));
        return wcTaskEntity;
    }


}
