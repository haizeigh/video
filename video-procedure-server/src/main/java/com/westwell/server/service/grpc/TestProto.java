package com.westwell.server.service.grpc;

import com.westwell.api.FeatureServiceGrpc;
import com.westwell.api.StudentPicUpdateRequest;
import com.westwell.api.StudentPicUpdateResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.List;

public class TestProto  {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext(true)
                .build();
        FeatureServiceGrpc.FeatureServiceBlockingStub stub = FeatureServiceGrpc.newBlockingStub(channel);

        StudentPicUpdateRequest.Builder builder = StudentPicUpdateRequest.newBuilder();
        List nums = new ArrayList();
        nums.add("1001");
        nums.add("1002");
        nums.add("1003");
        builder.addAllStudentNums(nums);
//        builder.setStudentNums(0, "1001");
//        builder.setStudentNums(1, "1002");
        StudentPicUpdateResponse studentPicUpdateResponse = stub.studentPicUpdate(builder.build());

        System.out.println(studentPicUpdateResponse.getSuccess());
//        channel.shutdown();
    }

}
