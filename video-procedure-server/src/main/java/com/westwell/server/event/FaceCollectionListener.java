package com.westwell.server.event;

import com.google.common.base.Strings;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyFacesContainer;
import com.westwell.server.service.FaceFeatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class FaceCollectionListener {

    @Resource
    FaceFeatureService faceFeatureService;

    @Resource
    RedisUtils redisUtils;

    @EventListener
    public void listener(FaceCollectionChangeEvent event)
    {
        String colleKey = event.getColleKey();
        log.info("colleKey={} Collection change", colleKey);
        if (!Strings.isNullOrEmpty(IdentifyFacesContainer.getIdentify(colleKey))){
            log.info("has identify, ignore it ");
        }

        List<String> colleValue = IdentifyFacesContainer.FACE_COLLECTION_MAP.get(colleKey);
        if (colleValue.size() % 5 == 0) {
            log.info("compare face Collection");
            String studentId = faceFeatureService.compareCollectionWithStudent(colleKey);
            if (Strings.isNullOrEmpty(studentId)){
                return;
            }
            IdentifyFacesContainer.addIdentify(colleKey, studentId);

            colleValue.stream().forEach(faceKey -> redisUtils.putHash(faceKey, DataConfig.STUDENT_ID, studentId));

        }

    }
}
