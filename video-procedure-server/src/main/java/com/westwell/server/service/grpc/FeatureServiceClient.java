package com.westwell.server.service.grpc;


import com.google.protobuf.ProtocolStringList;
import com.westwell.api.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.List;

public class FeatureServiceClient {

    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("10.66.64.16", 50051)
                .usePlaintext(true)
                .build();

        FeatureServiceGrpc.FeatureServiceBlockingStub stub = FeatureServiceGrpc.newBlockingStub(channel);

        DetectionServiceGrpc.DetectionServiceBlockingStub detectionServiceBlockingStub = DetectionServiceGrpc.newBlockingStub(channel);

        List nums = new ArrayList();
        nums.add("1001");
        nums.add("1002");
        nums.add("1003");

        PicsInRedisRequest.Builder picbuilder = PicsInRedisRequest.newBuilder();
        picbuilder.addAllPickeysReq(nums);

        DetectPicsInRedisResponse detectPicsInRedisResponse = detectionServiceBlockingStub.detectPicsInRedis(picbuilder.build());
        ProtocolStringList pickeysResList = detectPicsInRedisResponse.getPickeysResList();

        for (String s : pickeysResList) {
            System.out.println(s);
        }
//        System.out.println(pickeysResList.get(0));
/*

        StudentPicUpdateRequest.Builder builder = StudentPicUpdateRequest.newBuilder();

        builder.addAllStudentNums(nums);
//        builder.setStudentNums(0, "1001");
//        builder.setStudentNums(1, "1002");
        StudentPicUpdateResponse studentPicUpdateResponse = stub.studentPicUpdate(builder.build());
        System.out.println(studentPicUpdateResponse.getSuccess());

        StudentPicUpdateResponse studentPicUpdateResponse1 = stub.studentsAllUpdate(null);
        System.out.println(studentPicUpdateResponse1.getSuccess());

        List<String> picColles = new ArrayList<>();
        picColles.add("picColles");
        ContrastPicWithCollesResponse contrastPicWithCollesResponse = stub.contrastPicWithCollesInRedis(
                ContrastPicWithCollesRequest.newBuilder()
                        .addAllPicColles(picColles)
                        .setPicKey("no 1")
                        .build());
        System.out.println(contrastPicWithCollesResponse.getSimilarityOrderedList(0));
*/

        channel.shutdown();
    }
}
