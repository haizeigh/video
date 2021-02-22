package com.westwell.backend.modules.generator.service.impl;

import com.alibaba.fastjson.JSON;
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
import java.util.*;

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

        studentPicMap.forEach( (num, paths) -> {
            StudentBaseInfoEntity studentBaseInfo = new StudentBaseInfoEntity();
            studentBaseInfo.setStudentNum(num);
            studentBaseInfo.setStudentName(studentNameMap.get(num));

            studentBaseInfo.setPicsUrl(JSON.toJSONString(paths));
            studentBaseInfo.setCreatTime(new Date());
            try {
                saveInfo(studentBaseInfo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
    }

    @Override
    public boolean saveStudentPic(StudentBaseInfoEntity studentBaseInfo) throws Exception {

        String filePathsString = studentBaseInfo.getPicsUrl();
        log.info("保存db数据");
        save(studentBaseInfo);

//        一个学生多张图
        List<String> filePaths = JSON.parseArray(filePathsString, String.class);
        List<String> filePathList = new ArrayList<String>();

        for (String filePath : filePaths) {
            File file = new File(filePath);
            //保存原图到reids, filePath 作为key
            String imageString = ImgTransitionUtil.imageFileToBase64(file);
            log.info("原图到redis");
            redisUtils.set(filePath, imageString);
            filePathList.add(filePath);

        }

        PicsInRedisRequest.Builder picBuilder = PicsInRedisRequest.newBuilder();
        picBuilder.addAllPickeysReq(filePathList);
        DetectPicsInRedisResponse detectPicsInRedisResponse = detectionServiceBlockingStub.detectPicsInRedis(picBuilder.build());
        ProtocolStringList picKeysList = detectPicsInRedisResponse.getPickeysResList();

        log.info("清理原图 redis");
        filePaths.stream().forEach(filePath -> redisUtils.delete(filePath));

        if (CollectionUtils.isEmpty(picKeysList)) {
            //图片有问题  下一个
            log.info("检测不到人面部图，还原db");
            throw new VPBException("检测不到人面部图");
        }

//            抽取特征
        log.info("抽取特征");
        FacePicsInRedisRequest facePicsInRedisRequest = FacePicsInRedisRequest.newBuilder().addAllPicKeys(picKeysList).build();
        featureServiceBlockingStub.extractFeatureInRedis(facePicsInRedisRequest);
//             if (extractFeatureInRedisResponse.getSuccess().equals();)
//            保存到redis
        log.info("抽取保存小图redis ");
        redisUtils.putHash(SMARTK_BASE, studentBaseInfo.getId().toString(), picKeysList.toString());
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

        save(studentBaseInfo);
        saveStudentPic(studentBaseInfo);

    }

    @Override
    @Transactional
    public void updateInfoById(StudentBaseInfoEntity studentBaseInfo) throws Exception {

        updateById(studentBaseInfo);
        saveStudentPic(studentBaseInfo);
    }

    @Override
    @Transactional
    public void removeInfoByIds(List<Integer> ids) {

        removeByIds(ids);
        deletePicByIds(ids);

    }


}
