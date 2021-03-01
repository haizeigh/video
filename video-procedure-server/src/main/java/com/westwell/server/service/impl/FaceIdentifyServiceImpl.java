package com.westwell.server.service.impl;

import com.google.common.base.Strings;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyFacesContainer;
import com.westwell.server.container.VideoContainer;
import com.westwell.server.dto.CompareSimilarityDto;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.service.FaceFeatureService;
import com.westwell.server.service.FaceIdentifyService;
import com.westwell.server.service.VideoMediaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class FaceIdentifyServiceImpl implements FaceIdentifyService {


    @Resource
    FaceFeatureService faceFeatureService;

    @Resource
    RedisUtils redisUtils;

    @Resource
    IdentifyFacesContainer identifyFacesContainer;

    @Resource
    VideoMediaService videoMediaService;

    @Override
    public boolean identifyFaces(TaskDetailInfoDto task, List<String> faceKeyList) throws Exception {

        if (CollectionUtils.isEmpty(faceKeyList)) {
            return false;
        }

        identifyFacesContainer.initBucket(faceKeyList, task);

        for (String faceKey : faceKeyList) {

//            把面部图归队
            CompareSimilarityDto compareSimilarityDto = faceFeatureService.compareFaceWithCollection(task, faceKey);
            if (compareSimilarityDto == null) {
                continue;
            }

            String tempFaceColleKey = compareSimilarityDto.getPicColleKey();
//            识别出出来了具体人物
            String identify = identifyFacesContainer.getIdentify(tempFaceColleKey, task);
            if (!Strings.isNullOrEmpty(tempFaceColleKey)
                    && identifyFacesContainer.containsKey(tempFaceColleKey, task)
                    && !Strings.isNullOrEmpty(identify)) {
                log.debug("find the student" + identify);
                redisUtils.putHash(faceKey, DataConfig.STUDENT_ID, identify);
            }


//            继续构建底库
            if (compareSimilarityDto.isOverSimilarity()) {
//             丢弃
                continue;
            }

            if (!Strings.isNullOrEmpty(tempFaceColleKey)) {
//             归队
                log.info( faceKey + "加入底库" + tempFaceColleKey);
                identifyFacesContainer.addPicToExistBucket(faceKey, tempFaceColleKey, task);
            } else {
//             增加新的集合
                log.info(faceKey + "创建新的底库");
                identifyFacesContainer.addPicToNewBucket(faceKey, task);
            }
        }
        return true;
    }


    public void clearVideoCache(TaskDetailInfoDto task) {
        log.info("格式化单次任务");

        VideoContainer videoContainer = identifyFacesContainer.getIdentifyMap().get(task.getTaskCameraPrefix());
        if (videoContainer == null){
            return;
        }

        List<String> sortedFaceKeys = identifyFacesContainer.getSortedFaceKeys(task);
        videoMediaService.clearListInRedis(sortedFaceKeys);

        List<String> list = identifyFacesContainer.faceColleKeys(task);
        videoMediaService.clearListInRedis(list);
        identifyFacesContainer.getIdentifyMap().remove(task.getTaskCameraPrefix());

    }



}
