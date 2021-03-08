package com.westwell.server.service.impl;

import com.google.common.base.Strings;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyContainerManager;
import com.westwell.server.dto.CompareSimilarityDto;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.service.FeatureService;
import com.westwell.server.service.IdentifyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class IdentifyServiceImpl implements IdentifyService {


    @Resource
    FeatureService featureService;

    @Resource
    RedisUtils redisUtils;

    @Resource
    IdentifyContainerManager identifyContainerManager;


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

        boolean initBucket = identifyContainerManager.initBucket(picKeyList, task);

        if (initBucket){
            List<String> newFaceKeys = new ArrayList<>();
            for (int i = 1; i < picKeyList.size(); i++) {
                newFaceKeys.add(picKeyList.get(i));
            }
            picKeyList = newFaceKeys;
        }

        for (String picKey : picKeyList) {

//            把图归队
            CompareSimilarityDto compareSimilarityDto = featureService.comparePicWithCollection(task, picKey);
            if (compareSimilarityDto == null) {
                continue;
            }

            String tempPicColleKey = compareSimilarityDto.getPicColleKey();
//            识别出出来了具体人物
            if (!Strings.isNullOrEmpty(tempPicColleKey)
                    && identifyContainerManager.containsKey(tempPicColleKey, task)
                    && !Strings.isNullOrEmpty(identifyContainerManager.getIdentify(tempPicColleKey, task))) {
                String identify = identifyContainerManager.getIdentify(tempPicColleKey, task);
                log.info("{}find the student={}" , picKey,  identify);
                redisUtils.putHash(picKey, DataConfig.STUDENT_ID, identify);
            }


//            继续构建底库
/*            if (compareSimilarityDto.isOverSimilarity()) {

                log.info(picKey + "加入底库" + tempPicColleKey);
                identifyContainerManager.addPicToExistBucket(picKey, tempPicColleKey, task);

                continue;
            }*/

            if (!Strings.isNullOrEmpty(tempPicColleKey)) {
//             归队
                log.info(picKey + "加入底库" + tempPicColleKey);
                identifyContainerManager.addPicToExistBucket(picKey, tempPicColleKey, task);
            } else {
//             增加新的集合
                log.info(picKey + "创建新的底库");
                identifyContainerManager.addPicToNewBucket(picKey, task);
            }

            //判断加入特征底库 仅仅是刚相似
            if ( !Strings.isNullOrEmpty(tempPicColleKey) && !compareSimilarityDto.isOverSimilarity() ){
                identifyContainerManager.addPicToSpecialBucket(picKey, tempPicColleKey, task);
            }

        }
        return true;
    }


}
