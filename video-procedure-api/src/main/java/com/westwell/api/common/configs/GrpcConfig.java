package com.westwell.api.common.configs;

import com.westwell.api.DetectionServiceGrpc;
import com.westwell.api.FeatureServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {

    @Bean("detect")
    public ManagedChannel managedChannel1(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
                .usePlaintext(true)
                .build();
        return channel;
    }

    @Bean("feature")
    public ManagedChannel managedChannel2(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8082)
                .usePlaintext(true)
                .build();
        return channel;
    }


    @Bean
    public FeatureServiceGrpc.FeatureServiceBlockingStub featureServiceBlockingStub(@Qualifier("feature") ManagedChannel managedChannel){
        return FeatureServiceGrpc.newBlockingStub(managedChannel);
    }


    @Bean
    public DetectionServiceGrpc.DetectionServiceBlockingStub detectionServiceBlockingStub(@Qualifier("detect") ManagedChannel managedChannel){
        return DetectionServiceGrpc.newBlockingStub(managedChannel);
    }
}
