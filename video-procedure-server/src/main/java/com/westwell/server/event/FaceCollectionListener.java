package com.westwell.server.event;

import com.google.common.base.Strings;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyContainer;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.service.FeatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class FaceCollectionListener {

    @Resource
    FeatureService featureService;

    @Resource
    RedisUtils redisUtils;

    @Resource
    IdentifyContainer identifyContainer;

    @EventListener
    public void listener(FaceCollectionChangeEvent event)
    {
        String colleKey = event.getColleKey();
        TaskDetailInfoDto task = (TaskDetailInfoDto)event.getSource();
        log.info("colleKey={} Collection change", colleKey);
        if (!Strings.isNullOrEmpty(identifyContainer.getIdentify(colleKey, task))){
            log.info("has identify, ignore it ");
        }

        List<String> colleValue = identifyContainer.getPicsFromBucket(colleKey);
        if (colleValue.size() % DataConfig.INTER_FRE == 0 &&  task.getTaskType() == TaskDetailInfoDto.TaskType.FACE) {
            log.info("compare face Collection");
            String studentId = featureService.compareCollectionWithStudent(colleKey);
            if (Strings.isNullOrEmpty(studentId)){
                return;
            }
            identifyContainer.addIdentify(colleKey, studentId, task);

            colleValue.stream().forEach(faceKey -> redisUtils.putHash(faceKey, DataConfig.STUDENT_ID, studentId));

        }else if (  task.getTaskType() == TaskDetailInfoDto.TaskType.BODY ){
            log.info("no need compare body Collection");
            //todo 新的body加入  判断是否具有face关系 然后标记
        }

    }
}
