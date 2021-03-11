package com;

import com.westwell.api.*;
import com.westwell.api.common.utils.ImgTransitionUtil;
import com.westwell.server.ServerApplication;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyContainerManager;
import com.westwell.server.service.*;
import com.westwell.server.service.impl.VideoAsyncServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = {ServerApplication.class})
@RunWith( SpringRunner.class)
public class TestCompare {

    @Resource
    RedisUtils redisUtils;

    @Resource
    WcTaskService wcTaskService;

    @Resource
    VideoMediaService videoMediaService;

    @Resource
    DetectionService detectionService;

    @Resource
    FeatureService featureService;

    @Resource
    IdentifyService identifyService;

    @Resource
    IdentifyContainerManager identifyContainerManager;

    @Resource
    FeatureServiceGrpc.FeatureServiceBlockingStub featureServiceBlockingStub;

    @Resource
    VideoAsyncServiceImpl videoAsyncService;

    @Test
    public void test1() throws Exception {

        String path = "/home/westwell/java/file/identify/2021-03-09/1-117/113000-113030/tempt/wellcare:base/";
        String f1= "19401_0.jpg:0";
        String f2= "19402_0.jpg:0";
        String f3= "19403_0.jpg:0";
        String f4= "19403_0.jpg:0";

        List<String> picKeyList = new ArrayList<>();
        picKeyList.add(path + f1);
        picKeyList.add(path + f2);
        picKeyList.add(path + f3);
        picKeyList.add(path + f4);

        for (String key : picKeyList) {
            String fileToBase64 = ImgTransitionUtil.imageFileToBase64(new File(key));
            redisUtils.set(key, fileToBase64);
        }


        List<String> faceKeyList = detectionService.detectFacesInPic(picKeyList);
        System.out.println("检测的face={}"+ faceKeyList);

        FacePicsInRedisRequest facePicsInRedisRequest = FacePicsInRedisRequest.newBuilder().addAllPicKeys(faceKeyList).build();
        ExtractFeatureInRedisResponse extractFeatureInRedisResponse = featureServiceBlockingStub.extractFeatureInRedis(facePicsInRedisRequest);
        System.out.println("完成面部特征提取");

        String face1 = faceKeyList.get(0);
        String face2 = faceKeyList.get(1);
        String face3 = faceKeyList.get(2);
        String face4 = faceKeyList.get(3);

        ArrayList<String> picColle = new ArrayList<>();
        picColle.add("set1");

        System.out.println("*******************************");
        System.out.println("开始测试对比, face1  和  3个face1");
        redisUtils.delete("set1");
        redisUtils.lPush("set1", face1);
        redisUtils.lPush("set1", face1);
        redisUtils.lPush("set1", face1);
        compare(face1, picColle);
        System.out.println("对比集合"+ face1+ "/" + face1+ "/"  + face1 +"和底库");
        student(picColle);


        System.out.println("*******************************");
        System.out.println("开始测试对比, face1  和  2个face1，1个face2");
        redisUtils.delete("set1");
        redisUtils.lPush("set1", face1);
        redisUtils.lPush("set1", face1);
        redisUtils.lPush("set1", face2);
        compare(face1, picColle);
        System.out.println("对比集合"+ face1+ "/" + face1+ "/"  + face2 +"和底库");
        student(picColle);

        System.out.println("*******************************");
        System.out.println("开始测试对比, face1  和  1个face1，1个face2,1个face3");
        redisUtils.delete("set1");
        redisUtils.lPush("set1", face1);
        redisUtils.lPush("set1", face2);
        redisUtils.lPush("set1", face3);
        compare(face1, picColle);
        System.out.println("对比集合"+ face1+ "/" + face2+ "/"  + face3 +"和底库");
        student(picColle);

        System.out.println("*******************************");
        System.out.println("开始测试对比, face1  和  2个face2,1个face3");
        redisUtils.delete("set1");
        redisUtils.lPush("set1", face2);
        redisUtils.lPush("set1", face2);
        redisUtils.lPush("set1", face3);
        compare(face1, picColle);
        System.out.println("对比集合"+ face2+ "/" + face2+ "/"  + face3 +"和底库");
        student(picColle);


    }

    private void student(ArrayList<String> picColle) {
        ContrastCollesWithBaseInfoRequest build = ContrastCollesWithBaseInfoRequest.newBuilder()
                .addAllPicColles(picColle)
                .build();
        ContrastCollesWithBaseInfoResponse contrastCollesWithBaseInfoResponse =
                featureServiceBlockingStub.contrastCollesWithBaseInfoInRedis(build);
        System.out.println("对比底库的结果" + contrastCollesWithBaseInfoResponse.getColleWithStudentResultsList() );
    }

    private void compare(String face1, ArrayList<String> picColle) {
        ContrastPicWithCollesRequest build = ContrastPicWithCollesRequest.newBuilder()
                .setPicKey(face1)
                .addAllPicColles(picColle)
                .build();

        ContrastPicWithCollesResponse contrastPicWithCollesResponse = featureServiceBlockingStub.contrastPicWithCollesInRedis(build);

        List<Double> similarityListForQuery = contrastPicWithCollesResponse.getSimilarityOrderedListList();
        System.out.println("对比结果是"+ similarityListForQuery);
    }
}
