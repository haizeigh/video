package com.westwell.backend.modules.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.protobuf.ProtocolStringList;
import com.westwell.api.*;
import com.westwell.api.common.utils.ImgTransitionUtil;
import com.westwell.backend.common.exception.VPBException;
import com.westwell.backend.common.utils.PageUtils;
import com.westwell.backend.common.utils.Query;
import com.westwell.backend.common.utils.RedisUtils;
import com.westwell.backend.modules.generator.dao.StudentBaseInfoDao;
import com.westwell.backend.modules.generator.entity.StudentBaseInfoEntity;
import com.westwell.backend.modules.generator.service.StudentBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("studentBaseInfoService")
public class StudentBaseInfoServiceImpl extends ServiceImpl<StudentBaseInfoDao, StudentBaseInfoEntity> implements StudentBaseInfoService {

    @Resource
    FeatureServiceGrpc.FeatureServiceBlockingStub featureServiceBlockingStub;

    @Resource
    DetectionServiceGrpc.DetectionServiceBlockingStub detectionServiceBlockingStub;

    @Resource
    RedisUtils redisUtils;

    public static final String SMARTK_BASE = "wellcare:base";


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<StudentBaseInfoEntity> page = this.page(
                new Query<StudentBaseInfoEntity>().getPage(params),
                new QueryWrapper<StudentBaseInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveByPath(String path) throws Exception {


        File sourcePathFile = new File(path);
        File[] files = sourcePathFile.listFiles();
        Map<String, List<String>> studentPicMap = new HashedMap();
        Map<String, String> studentNameMap = new HashedMap();

        for (File file : files) {
            String[] split = file.getName().split("_");
            String num = split[0];
            String name = split[1];
            if (studentPicMap.get(num) == null){
                List<String> paths = new ArrayList<>();
                paths.add(file.getAbsolutePath());
                studentPicMap.put(num, paths);

            }else {
                studentPicMap.get(num).add(file.getAbsolutePath());
            }
            studentNameMap.put(num,name);
        }

//        清理底库
        log.info("清理底库数据");
        redisUtils.delete(SMARTK_BASE);

        studentPicMap.forEach( (num, paths) -> {
            StudentBaseInfoEntity studentBaseInfo = new StudentBaseInfoEntity();
            studentBaseInfo.setStudentNum(num);
            studentBaseInfo.setStudentName(studentNameMap.get(num));

            studentBaseInfo.setPicsUrl(getPathString(paths));

            studentBaseInfo.setCreatTime(new Date());
            try {
                save(studentBaseInfo);
                saveStudentPic(studentBaseInfo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
        //通知更新全部学生
        log.info("通知更新全部学生底库数据");
        StudentPicUpdateResponse studentPicUpdateResponse = featureServiceBlockingStub.studentsAllUpdate(null);
        if ( !studentPicUpdateResponse.getSuccess().equals("success")){
            redisUtils.delete(SMARTK_BASE);
            throw new VPBException("通知特征服务更新异常");
        }

    }

    private String getPathString(List<String> paths) {
        StringBuilder allThisPath = new StringBuilder("");
        for (String thisPath : paths) {
            allThisPath.append(",").append(thisPath);
        }
        return allThisPath.substring(1);
    }

    @Override
    public boolean saveStudentPic(StudentBaseInfoEntity studentBaseInfo) throws Exception {

        String filePathsString = studentBaseInfo.getPicsUrl();

//        一个学生多张图
//        List<String> filePaths = JSON.parseArray(filePathsString, String.class);
//        List<String> filePaths = new ArrayList<>();
//        filePaths.add(filePathsString);
        String[] filePaths = filePathsString.split(",");

        List<String> imageKeyList = new ArrayList<String>();
        List<String> faceKeysList = new ArrayList<String>();

        for (String filePath : filePaths) {
            File file = new File(filePath);
            //保存原图到reids, filePath 作为key
            String imageString = ImgTransitionUtil.imageFileToBase64(file);
            log.info("原图到redis");
            String filePrefix = filePath.substring(0, filePath.lastIndexOf("/"));
            String fileName = filePath.substring(filePath.lastIndexOf("/"));
            String[] split = fileName.split("_");

            String name = filePrefix + "_" + split[0] + "_" + split[2];
            imageKeyList.add(name);
            redisUtils.set(name, imageString);

//            每张图片检测 自取最大面积的小图
            List<String> childFaceList = new ArrayList<String>();
            childFaceList.add(name);

            //截取小图
            PicsInRedisRequest.Builder picBuilder = PicsInRedisRequest.newBuilder();
            picBuilder.addAllPickeysReq(childFaceList);
            DetectPicsInRedisResponse detectPicsInRedisResponse = detectionServiceBlockingStub.detectPicsInRedis(picBuilder.build());
            ProtocolStringList picKeysList = detectPicsInRedisResponse.getPickeysResList();
            if ( CollectionUtils.isEmpty(picKeysList) ){
                log.info("存在检测不到小图的情况， redis的key = " + name);
                continue;
            }else if ( picKeysList.size() > 1){
//                处理多余一个的情况
                String bigPicKey = "" ;
                Integer area = 0;

                for (int i = 0; i < picKeysList.size(); i++) {
                    String thisPic = picKeysList.get(i);
                    String location = redisUtils.getHash(thisPic, "location").toString();
                    String[] locationSplit = location.split("_");
                    int x1 = Integer.parseInt(locationSplit[0]);
                    int y1 = Integer.parseInt(locationSplit[1]);
                    int x2 = Integer.parseInt(locationSplit[2]);
                    int y2 = Integer.parseInt(locationSplit[3]);
                    Integer thisArea = (x2 - x1) * (y2 -y1);
                    if ( thisArea > area){
                        area = thisArea;
                        bigPicKey = thisPic;
                    }
                }
                faceKeysList.add(bigPicKey);
            }else {
                faceKeysList.addAll(picKeysList);
            }
        }



        log.info("清理原图 redis");
        imageKeyList.stream().forEach(filePath -> redisUtils.delete(filePath));


//            抽取特征
        log.info("抽取特征");
        FacePicsInRedisRequest facePicsInRedisRequest = FacePicsInRedisRequest.newBuilder().addAllPicKeys(faceKeysList).build();
        featureServiceBlockingStub.extractFeatureInRedis(facePicsInRedisRequest);
//             if (extractFeatureInRedisResponse.getSuccess().equals();)
//            保存到redis
        log.info("抽取保存小图redis ");
        redisUtils.putHash(SMARTK_BASE, studentBaseInfo.getId().toString(), getPathString(faceKeysList));
        return true;
    }

    @Override
    public boolean deletePicByIds(List<Integer> ids) {
        ids.forEach(id ->  redisUtils.hDelete(SMARTK_BASE, id.toString()));
        return true;
    }

    @Override
    @Transactional
    public void saveInfo(StudentBaseInfoEntity studentBaseInfo) throws Exception {

        log.info("保存学生底库数据");
        save(studentBaseInfo);
        saveStudentPic(studentBaseInfo);
        //通知更新单个学生
        notifyUpdate(studentBaseInfo.getId().toString());

    }

    private void notifyUpdate(String id) {

        log.info("通知更新学生底库数据");
        StudentPicUpdateRequest request = StudentPicUpdateRequest.newBuilder().addStudentNums(id).build();
        StudentPicUpdateResponse studentPicUpdateResponse = featureServiceBlockingStub.studentPicUpdate(request);
        if ( !studentPicUpdateResponse.getSuccess().equals("success")){
            throw new VPBException("通知特征服务更新异常");
        }
    }

    @Override
    @Transactional
    public void updateInfoById(StudentBaseInfoEntity studentBaseInfo) throws Exception {

        log.info("更新学生底库数据");
        updateById(studentBaseInfo);
        saveStudentPic(studentBaseInfo);
        //通知更新单个学生
        notifyUpdate(studentBaseInfo.getId().toString());
    }

    @Override
    @Transactional
    public void removeInfoByIds(List<Integer> ids) {

        log.info("删除学生底库数据");
        removeByIds(ids);
        ids.forEach(id -> {
            redisUtils.hDelete(SMARTK_BASE, id.toString());
            //通知更新单个学生
            notifyUpdate(id.toString());
        });

    }

    @Override
    public void syncUnit(String unit) {

        Map<String, Object> params = new HashedMap();
        params.put("limit", Integer.MAX_VALUE);
        PageUtils pageUtils = queryPage(params);

        pageUtils.getList().stream().forEach( entity -> {
            StudentBaseInfoEntity studentBaseInfoEntity = (StudentBaseInfoEntity) entity;
            notifyUpdate(studentBaseInfoEntity.getId().toString());
        });



    }


}
