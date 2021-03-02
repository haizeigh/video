package com.westwell.server.service.impl;

import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.utils.ExportUtil;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyContainer;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.dto.TaskFinalResultDto;
import com.westwell.server.service.ResultDumpService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ResultDumpServiceImpl implements ResultDumpService {

    @Resource
    RedisUtils redisUtils;

    @Resource
    IdentifyContainer identifyContainer;

    @Override
    public void dumpFrameResult(TaskDetailInfoDto task, String textPath) throws Exception {
        List<String> sortedFaceKeys = identifyContainer.getSortedFaceKeys(task);

        String taskNo = task.getTaskEntity().getTaskNo().toString();
        String cameraNo = task.getTaskEntity().getCameraNo().toString();

        String mapKey = "task_no,camera_no,pic_time,frame_no,location,pic,feature,student_id,student_name";
        List<Map<String, String>> dataList = new ArrayList<>();
        for (String faceKey : sortedFaceKeys) {

//            BeanUtils.copyProperties(map, redisUtils.getHash(faceKey));
            Map<String, Object> hash = redisUtils.getHash(faceKey);
            Map<String, String> map = new HashedMap();
            redisUtils.getHash(faceKey).forEach((k, v) -> map.put(k, v.toString()));

            String[] split = faceKey.split(":");

            map.put("task_no", taskNo);
            map.put("camera_no", cameraNo);
            map.put("pic_time", split[split.length - 3]);
            map.put("frame_no", split[split.length - 2]);
            dataList.add(map);
        }

        File file = new File(textPath);
        if (!file.exists()){
            file.mkdirs();
        }

        try {
            OutputStream outputStream = new FileOutputStream(textPath+ "/" + "frame.csv");
            ExportUtil.doExport(dataList, mapKey, outputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void dumpTaskTemptResult(TaskDetailInfoDto task, String textPath) {

    }

    @Override
    public void dumpTaskFinalResult(TaskDetailInfoDto task, String textPath) throws Exception {

        Map<String, String> identifyMap = identifyContainer.getVideoIdentifyMap(task);
        Integer taskNo = task.getTaskEntity().getTaskNo();
        Integer cameraNo = task.getTaskEntity().getCameraNo();

        List<TaskFinalResultDto> dataList = new ArrayList<>();
        String mapKey = "task_no,camera_no,student_id,student_name,start_time,end_time,locations";

        identifyMap.forEach( ( faceColleKey, studentId ) -> {
//            遍历所有人
            List<String> faceKeys = identifyContainer.getPicsFromBucket(faceColleKey);
            Collections.sort(faceKeys);

//            遍历所有小图
            faceKeys.forEach(faceKey -> {

                String[] split = faceKey.split(":");
                String location = redisUtils.getHash(faceKey, "location").toString();
                String thisStudentId = redisUtils.getHash(faceKey, DataConfig.STUDENT_ID).toString();
                long faceSec = Long.parseLong(split[split.length - 3]) / 1000;

//                初始化
                if (CollectionUtils.isEmpty(dataList)){
                    insertFinalResult(taskNo, cameraNo, dataList, location, thisStudentId, faceSec);
                    return;
                }
                TaskFinalResultDto lastTaskFinalResultDto = dataList.get(dataList.size() - 1);

//               和前一个对比 小于间隔的话整合
                if (lastTaskFinalResultDto != null
                        && lastTaskFinalResultDto.getStudent_id().equals(thisStudentId)
                        && faceSec - lastTaskFinalResultDto.getEnd_time() < 300 ){
                    lastTaskFinalResultDto.setEnd_time(faceSec);
                    lastTaskFinalResultDto.getLocations().add(location);
                }else {
//                    新插入数据
                    insertFinalResult(taskNo, cameraNo, dataList, location, thisStudentId, faceSec);
                }
            });
        } );


        File file = new File(textPath);
        if (!file.exists()){
            file.mkdirs();
        }

        List<Map<String, String>> collect = new ArrayList<>();
        for (TaskFinalResultDto taskFinalResultDto : dataList) {
            Map<String, String> describe = BeanUtils.describe(taskFinalResultDto);
            collect.add(describe);
        }

        OutputStream outputStream = new FileOutputStream(textPath+ "/" + "final.csv");
        ExportUtil.doExport(collect, mapKey, outputStream);


    }

    private void insertFinalResult(Integer taskNo, Integer cameraNo, List<TaskFinalResultDto> dataList, String location, String thisStudentId, long faceSec) {
        List<String> locations = new ArrayList<>();
        locations.add(location);
        TaskFinalResultDto taskFinalResultDto = TaskFinalResultDto.builder()
                .task_no(taskNo.toString())
                .camera_no(cameraNo.toString())
                .student_id(thisStudentId)
//                            .label("")
                .start_time(faceSec)
                .end_time(faceSec)
                .locations(locations).build();
        dataList.add(taskFinalResultDto);
    }
}
