package com.westwell.server.service.grpc;


import com.westwell.api.DetectPicsInRedisResponse;
import com.westwell.api.DetectionServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class DeteServiceServer {

    public static void main(String[] args) throws Exception {


        Server server = ServerBuilder
                .forPort(8082)
                .addService(new DeteServiceServer(). new MyFeatureServiceServer()).build();

        server.start();

        System.out.println("Server started.");
        server.awaitTermination();
    }

    class MyFeatureServiceServer extends DetectionServiceGrpc.DetectionServiceImplBase{


        public void detectPicsInRedis(com.westwell.api.PicsInRedisRequest request,
                                      io.grpc.stub.StreamObserver<com.westwell.api.DetectPicsInRedisResponse> responseObserver) {

            DetectPicsInRedisResponse build = DetectPicsInRedisResponse.newBuilder().addAllPickeysRes(request.getPickeysReqList()).build();
            System.out.println(request.getPickeysReqList());
            responseObserver.onNext(build);
            responseObserver.onCompleted();
        }

    }
}
