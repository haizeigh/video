package com.westwell.server.service.impl;

import com.google.common.base.Strings;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyContainer;
import com.westwell.server.dto.CompareSimilarityDto;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.service.FeatureService;
import com.westwell.server.service.IdentifyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class IdentifyServiceImpl implements IdentifyService {


    @Resource
    FeatureService featureService;

    @Resource
    RedisUtils redisUtils;

    @Resource
    IdentifyContainer identifyContainer;


    @Override
    public boolean identifyFaces(TaskDetailInfoDto task, List<String> faceKeyList) throws Exception {

        return identifyPic(task, faceKeyList);
    }



    @Override
    public boolean identifyBody(TaskDetailInfoDto task, List<String> bodyKeyList)  throws Exception {

        return identifyPic(task, bodyKeyList);
    }

    @Override
    public boolean identifyPic(TaskDetailInfoDto task, List<String> picKeyList) throws Exception {
        if (CollectionUtils.isEmpty(picKeyList)) {
            return false;
        }

        identifyContainer.initBucket(picKeyList, task);

        for (String picKey : picKeyList) {

//            把图归队
            CompareSimilarityDto compareSimilarityDto = featureService.comparePicWithCollection(task, picKey);
            if (compareSimilarityDto == null) {
                continue;
            }

            String tempPicColleKey = compareSimilarityDto.getPicColleKey();
//            识别出出来了具体人物
            if (!Strings.isNullOrEmpty(tempPicColleKey)
                    && identifyContainer.containsKey(tempPicColleKey, task)
                    && !Strings.isNullOrEmpty(identifyContainer.getIdentify(tempPicColleKey, task))) {
                String identify = identifyContainer.getIdentify(tempPicColleKey, task);
                log.debug("find the student" + identify);
                redisUtils.putHash(picKey, DataConfig.STUDENT_ID, identify);
            }


//            继续构建底库
            if (compareSimilarityDto.isOverSimilarity()) {
//             丢弃
                continue;
            }

            if (!Strings.isNullOrEmpty(tempPicColleKey)) {
//             归队
                log.info(picKey + "加入底库" + tempPicColleKey);
                identifyContainer.addPicToExistBucket(picKey, tempPicColleKey, task);
            } else {
//             增加新的集合
                log.info(picKey + "创建新的底库");
                identifyContainer.addPicToNewBucket(picKey, task);
            }
        }
        return true;
    }


}
