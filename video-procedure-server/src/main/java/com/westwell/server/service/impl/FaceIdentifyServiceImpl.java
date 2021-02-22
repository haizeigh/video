package com.westwell.server.service.impl;

import com.google.common.base.Strings;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyFacesContainer;
import com.westwell.server.dto.CompareSimilarityDto;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.service.FaceFeatureService;
import com.westwell.server.service.FaceIdentifyService;
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

    @Override
    public boolean identifyFaces(TaskDetailInfoDto task, List<String> faceKeyList) {

        if (CollectionUtils.isEmpty(faceKeyList)) {
            return false;
        }

        IdentifyFacesContainer.addPicToNewBucket(faceKeyList.get(0), task);

        for (String faceKey : faceKeyList) {

//            把面部图归队
            CompareSimilarityDto compareSimilarityDto = faceFeatureService.compareFaceWithCollection(faceKey);
            if (compareSimilarityDto == null){
                continue;
            }

            String tempFaceColleKey = compareSimilarityDto.getPicColleKey();
//            识别出出来了具体人物
            if (!Strings.isNullOrEmpty(tempFaceColleKey)
                    && IdentifyFacesContainer.containsKey(tempFaceColleKey)
                    && !Strings.isNullOrEmpty(IdentifyFacesContainer.getIdentify(tempFaceColleKey))){
                log.debug("find student");
                redisUtils.putHash(faceKey, DataConfig.STUDENT_ID, IdentifyFacesContainer.getIdentify(tempFaceColleKey));
//                continue;
            }


//            继续构建底库
            if (compareSimilarityDto.isOverSimilarity()) {
//             丢弃
                continue;
            }

            if (!Strings.isNullOrEmpty(tempFaceColleKey)) {
//             归队
                IdentifyFacesContainer.addPicToExistBucket(faceKey, tempFaceColleKey);
            } else {
//             增加新的集合
                IdentifyFacesContainer.addPicToNewBucket(tempFaceColleKey, task);
            }
        }
        return true;
    }

}
