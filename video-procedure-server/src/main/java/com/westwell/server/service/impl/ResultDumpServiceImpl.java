package com.westwell.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.utils.ExportUtil;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyContainerManager;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.dto.TaskFinalResultDto;
import com.westwell.server.service.ResultDumpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ResultDumpServiceImpl implements ResultDumpService {

    @Resource
    RedisUtils redisUtils;

    @Resource
    IdentifyContainerManager identifyContainerManager;

    @Override
    public void dumpFrameResult(TaskDetailInfoDto task, String textPath) throws Exception {

        List<String> sortedPicKeys = identifyContainerManager.getSortedPicKeys(task);
        if (CollectionUtils.isEmpty(sortedPicKeys)){
            log.info("任务no={}没有帧数据",task.getTaskEntity().getTaskNo() );
            return;
        }
        String taskNo = task.getTaskEntity().getTaskNo().toString();
        String cameraNo = task.getTaskEntity().getCameraNo().toString();

//        String mapKey = "task_no,camera_no,pic_time,frame_no,location,pic,feature,student_id,student_name";
        String mapKey = "task_no,camera_no,pic_time,frame_no,location,student_id,student_name,picKey";
        List<Map<String, String>> dataList = new ArrayList<>();
        List<String> queryList = new ArrayList<>();
        queryList.add(DataConfig.LOCATION);
        queryList.add(DataConfig.STUDENT_ID);

        for (String picKey : sortedPicKeys) {

            List<Object> queryListObjects = redisUtils.multiGetHash(picKey, queryList);
            String location = queryListObjects.get(0).toString();
            String studentId = queryListObjects.get(1) == null ? null : queryListObjects.get(1).toString() ;

            Map<String, String> map = new HashedMap();
            redisUtils.getHash(picKey).forEach((k, v) -> map.put(k, v.toString()));

            String[] split = picKey.split(":");

            map.put("task_no", taskNo);
            map.put("camera_no", cameraNo);
            map.put("pic_time", split[3]);
            map.put("frame_no", split[4]);

            map.put("location", location);
            map.put("student_id", studentId);
            map.put("picKey", picKey);
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

        Map<String, String> identifyMap = identifyContainerManager.getVideoIdentifyMap(task);
        if (MapUtils.isEmpty(identifyMap)){
            log.info("任务no={}没有推理数据",task.getTaskEntity().getTaskNo() );
            return;
        }

        Integer taskNo = task.getTaskEntity().getTaskNo();
        Integer cameraNo = task.getTaskEntity().getCameraNo();

        List<TaskFinalResultDto> dataList = new ArrayList<>();
        String mapKey = "task_no,camera_no,student_id,student_name,start_time,end_time,locations";

        Map<String, List<String>> studentyMap = new HashedMap();
//        根据学生分组
        identifyMap.forEach( ( picColleKey, studentId ) -> {

            List<String> colleKeyList = studentyMap.get(studentId);
            if (CollectionUtils.isEmpty(colleKeyList)){
                colleKeyList = new ArrayList<>();
                colleKeyList.add(picColleKey);
                studentyMap.put(studentId, colleKeyList);
                return;
            }
            colleKeyList.add(picColleKey);

        } );

        studentyMap.forEach( ( studentId, colleKeyList ) -> {
            //            遍历所有人
            colleKeyList.stream().forEach(picColleKey -> {

                List<String> picKeys = identifyContainerManager.getPicsFromBucket(picColleKey);
                Collections.sort(picKeys);

//            遍历所有小图
                picKeys.forEach(picKey -> {

                    String[] split = picKey.split(":");
                    String location = redisUtils.getHash(picKey, "location").toString();
                    String thisStudentId = studentId;
                    long picSec = Long.parseLong(split[3]);
                    int frameNum = Integer.parseInt(split[4]);

//                初始化
                    if (CollectionUtils.isEmpty(dataList)){
                        insertFinalResult(taskNo, cameraNo, dataList, location, thisStudentId, picSec);
                        return;
                    }
                    TaskFinalResultDto lastTaskFinalResultDto = dataList.get(dataList.size() - 1);

//               和前一个对比 整合
                    if (
                            lastTaskFinalResultDto.getStudent_id().equals(thisStudentId)
                            && picSec - lastTaskFinalResultDto.getEnd_time() > 0
                    ){
                        lastTaskFinalResultDto.setEnd_time(picSec);
                        lastTaskFinalResultDto.getLocations().add(location);
                    }else if (
                            lastTaskFinalResultDto.getStudent_id().equals(thisStudentId)
                                    && picSec - lastTaskFinalResultDto.getEnd_time() <= 0
                    ){
                        log.info("同一个人的位置信息，在一秒内的数据不再整合");
                    }else {
//                    新插入数据
                        insertFinalResult(taskNo, cameraNo, dataList, location, thisStudentId, picSec);
                    }
                });
            } );
        });


        File file = new File(textPath);
        if (!file.exists()){
            file.mkdirs();
        }

        List<Map<String, String>> collect = new ArrayList<>();
        for (TaskFinalResultDto taskFinalResultDto : dataList) {
            Map<String, String> describe = BeanUtils.describe(taskFinalResultDto);
            describe.put("locations", JSON.toJSONString(taskFinalResultDto.getLocations()));
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

    public static void main(String[] args) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        TaskFinalResultDto taskFinalResultDto = new TaskFinalResultDto();
        List<String> locations = new ArrayList<>();
        locations.add("location1");
        locations.add("location2");
        taskFinalResultDto.setLocations(locations);

        Map<String, String> describe = BeanUtils.describe(taskFinalResultDto);
        describe.put("locations", JSON.toJSONString(taskFinalResultDto.getLocations()));

        System.out.println(JSON.toJSONString(describe));
    }

}

