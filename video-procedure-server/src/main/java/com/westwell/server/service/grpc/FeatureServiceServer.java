package com.westwell.server.service.grpc;


import com.westwell.api.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

public class FeatureServiceServer {

    public static void main(String[] args) throws Exception {


        Server server = ServerBuilder
                .forPort(8087)
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

        public void studentsAllUpdate(NullRequest request,
                                      StreamObserver<StudentPicUpdateResponse> responseObserver) {
            StudentPicUpdateResponse studentPicUpdateResponse = StudentPicUpdateResponse.newBuilder().setSuccess("success").build();

            responseObserver.onNext(studentPicUpdateResponse);
            responseObserver.onCompleted();
        }

        public void contrastPicWithCollesInRedis(ContrastPicWithCollesRequest request,
                                                 StreamObserver<ContrastPicWithCollesResponse> responseObserver) {

            System.out.println(request.getPicKey());
            System.out.println(request.getPicColles(0));

            ContrastPicWithCollesResponse.Builder builder = ContrastPicWithCollesResponse.newBuilder();
//            List<ColleSimilarityResult> list = new ArrayList<>();
//            ColleSimilarityResult colleSimilarityResult = new ColleSimilarityResult();

//            list.add(ColleSimilarityResult.newBuilder().setSimilarity((float) 0.8).setPicColle("test").build());

            List<Double> sim = new ArrayList<>();
            sim.add(0.9);
            sim.add(0.8);
            builder.addAllSimilarityOrderedList(sim);

            List<String> coll = new ArrayList<>();
            coll.add(request.getPicColles(0));
//            builder.addAllPicColles(coll);

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }


        public void contrastCollesWithBaseInfoInRedis(com.westwell.api.ContrastCollesWithBaseInfoRequest request,
                                                      io.grpc.stub.StreamObserver<com.westwell.api.ContrastCollesWithBaseInfoResponse> responseObserver) {

/*

            ColleWithStudentResult result = ColleWithStudentResult.newBuilder().setStudentNum("1").setPicColle("ded").build();
            List<> list = new ArrayList<>();
            list.add(result);

            ContrastCollesWithBaseInfoResponse build1 = ContrastCollesWithBaseInfoResponse.newBuilder().addAllColleWithStudentResults("list").build();

            responseObserver.onNext(build1);
            responseObserver.onCompleted();
*/


        }


    }
}
