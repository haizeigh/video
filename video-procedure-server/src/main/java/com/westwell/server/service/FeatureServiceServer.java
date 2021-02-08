package com.westwell.server.service;


import com.westwell.api.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

public class FeatureServiceServer {

    public static void main(String[] args) throws Exception {


        Server server = ServerBuilder
                .forPort(8080)
                .addService(new FeatureServiceServer(). new MyFeatureServiceServer()).build();

        server.start();

        System.out.println("Server started.");
        server.awaitTermination();
    }

    class MyFeatureServiceServer extends FeatureServiceGrpc.FeatureServiceImplBase{

        public void studentPicUpdate(StudentPicUpdateRequest request,
                                     StreamObserver<StudentPicUpdateResponse> responseObserver) {
            System.out.println(request.getStudentNumsList());

            StudentPicUpdateResponse studentPicUpdateResponse = StudentPicUpdateResponse.newBuilder().setSuccess("success").build();

            responseObserver.onNext(studentPicUpdateResponse);
            responseObserver.onCompleted();
        }

        public void studentsAllUpdate(com.westwell.api.NullRequest request,
                                      io.grpc.stub.StreamObserver<com.westwell.api.StudentPicUpdateResponse> responseObserver) {
            StudentPicUpdateResponse studentPicUpdateResponse = StudentPicUpdateResponse.newBuilder().setSuccess("success").build();

            responseObserver.onNext(studentPicUpdateResponse);
            responseObserver.onCompleted();
        }

        public void contrastPicWithCollesInRedis(com.westwell.api.ContrastPicWithCollesRequest request,
                                                 io.grpc.stub.StreamObserver<com.westwell.api.ContrastPicWithCollesResponse> responseObserver) {

            System.out.println(request.getPicKey());
            System.out.println(request.getPicColles(0));

            ContrastPicWithCollesResponse.Builder builder = ContrastPicWithCollesResponse.newBuilder();
            List<ColleSimilarityResult> list = new ArrayList<>();
//            ColleSimilarityResult colleSimilarityResult = new ColleSimilarityResult();

            list.add(ColleSimilarityResult.newBuilder().setSimilarity((float) 0.8).setPicColle("test").build());

            builder.addAllSortedSimilarityResult(list);

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }


    }
}
