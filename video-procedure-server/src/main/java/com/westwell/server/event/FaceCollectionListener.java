package com.westwell.server.event;

import com.google.common.base.Strings;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.utils.LocationUtil;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyContainerManager;
import com.westwell.server.container.VideoContainer;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.service.FeatureService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FaceCollectionListener {

    @Resource
    FeatureService featureService;

    @Resource
    RedisUtils redisUtils;

    @Resource
    IdentifyContainerManager identifyContainerManager;


    @EventListener
    public void listener(FaceCollectionChangeEvent event) throws Exception {
        String colleKey = event.getColleKey();
        String picKey = event.getPicKey();
        TaskDetailInfoDto task = (TaskDetailInfoDto) event.getSource();
        log.info("colleKey={} Collection change", colleKey);
        if (!Strings.isNullOrEmpty(identifyContainerManager.getIdentify(colleKey, task))) {
            log.info("has identify, ignore it ");
            return;
        }

        List<String> colleValue = identifyContainerManager.getPicsFromBucket(colleKey);
        try {

            if (colleValue.size() % DataConfig.INTER_FRE == 0 && task.getTaskType() == TaskDetailInfoDto.TaskType.FACE) {
                log.info("compare face Collection");
                String studentId = featureService.compareCollectionWithStudent(task, colleKey);
                if (Strings.isNullOrEmpty(studentId)) {
                    log.info("compare face Collection {} 无结果", colleKey);
                    return;
                }
                identifyContainerManager.addIdentify(colleKey, studentId, task);

                colleValue.stream().forEach(faceKey -> redisUtils.putHash(faceKey, DataConfig.STUDENT_ID, studentId));

            } else if (colleValue.size() % DataConfig.INTER_FRE == 0 && task.getTaskType() == TaskDetailInfoDto.TaskType.BODY) {
                log.info("检查body和face是不是有绑定关系");
                List<String> faces = task.getFaces();
                List<String> bodies = task.getBodies();
                if (CollectionUtils.isEmpty(faces)) {
                    log.info("当前任务id={}没有检测到faces", task.getTaskEntity().getId());
                    return;
                }


                String framePrefix = picKey.split("body")[0];
                List<String> frameFaces = faces.stream().filter(face -> face.startsWith(framePrefix)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(frameFaces)) {
                    log.info("当前任务id={}的帧{}没有检测到faces", task.getTaskEntity().getId(), framePrefix);
                    return;
                }

                //计算iou
                String bodyLocation = redisUtils.getHash(picKey, DataConfig.LOCATION).toString();
                List<Double> iouList = frameFaces.stream().map(face -> {
                    String faceLocation = redisUtils.getHash(face, DataConfig.LOCATION).toString();
                    return LocationUtil.calculateLocationIOU(faceLocation, bodyLocation);
                }).collect(Collectors.toList());

                Optional<Double> bigIOU = iouList.stream().filter(iou -> iou > DataConfig.MIN_IOU).max((i1, i2) -> (int) (i1 - i2));
                if (!bigIOU.isPresent()) {
                    log.info("当前任务id={}，body={}查找faces{},计算iou{}，阈值{}，没有找到body和face的关系",
                            task.getTaskEntity().getId(), picKey, frameFaces, iouList, DataConfig.MIN_IOU);
                    return;
                }

                int index = iouList.indexOf(bigIOU.get());
                String rightFace = frameFaces.get(index);
                log.info("当前任务id={}，body={}绑定了face={},计算的iou={}, 阈值={}",
                        task.getTaskEntity().getId(), picKey, rightFace, bigIOU.get(), DataConfig.MIN_IOU);

                String facePicColleKey = checkPicColle(task, TaskDetailInfoDto.TaskType.FACE, rightFace);
                if (Strings.isNullOrEmpty(facePicColleKey)) {
                    log.info("当前任务id={}，face={}找不到对应集合", task.getTaskEntity().getId(), rightFace);
                    return;
                }
                VideoContainer faceVideoContainer = identifyContainerManager.getVideoContainerByType(task, TaskDetailInfoDto.TaskType.FACE);
                String student = faceVideoContainer.getIdentifyMap().get(facePicColleKey);
                if (Strings.isNullOrEmpty(student)) {
                    log.info("当前任务id={}，facePicColleKey={}找不到对应学生", task.getTaskEntity().getId(), facePicColleKey);
                    return;
                }

                String bodyPicColleKey = colleKey;
                if (Strings.isNullOrEmpty(bodyPicColleKey)) {
                    log.info("当前任务id={}，body={}找不到对应集合", task.getTaskEntity().getId(), picKey);
                    return;
                }
                identifyContainerManager.addBodyColleAndFaceColleRelation(task, bodyPicColleKey, facePicColleKey);

//            这里校验是不是已经存在关系
                String studentIdentify = identifyContainerManager.getIdentify(bodyPicColleKey, task);
                if (Strings.isNullOrEmpty(studentIdentify)) {
                    log.info("当前任务id={}, type={}，为bodyPicColleKey={}增加学生标记{}",
                            task.getTaskEntity().getId(), task.getTaskType(), bodyPicColleKey, student);
                    identifyContainerManager.addIdentify(bodyPicColleKey, student, task);
                    colleValue.stream().forEach(faceKey -> redisUtils.putHash(faceKey, DataConfig.STUDENT_ID, student));
                }

            }

        } catch (Exception e) {
            log.info("处理集合变更事件异常, picKey={}, colleKey={}", picKey, colleKey, e);
        }

    }



    private String checkPicColle(TaskDetailInfoDto task, TaskDetailInfoDto.TaskType taskType, String picKey) throws Exception {

        VideoContainer videoContainerByType = identifyContainerManager.getVideoContainerByType(task, taskType);
        List<String> picCollection = videoContainerByType.getPicCollection();
        for (String picColle : picCollection) {
            List<String> picsFromBucket = identifyContainerManager.getPicsFromBucket(picColle);
            if (CollectionUtils.isNotEmpty(picsFromBucket) && picsFromBucket.contains(picKey)){
                log.info("当前任务id={}，类别{},pic={}查找集合,结果={}",
                        task.getTaskEntity().getId(), taskType, picKey, picColle);
                return picColle;
            }

        }
        log.info("当前任务id={}，类别{},pic={}查找不到集合",task.getTaskEntity().getId(), taskType, picKey);
        return null;

    }
}
