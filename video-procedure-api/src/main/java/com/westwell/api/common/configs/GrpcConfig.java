package com.westwell.api.common.configs;

import com.westwell.api.DetectionServiceGrpc;
import com.westwell.api.FeatureServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {

    @Bean
    public ManagedChannel managedChannel(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
                .usePlaintext(true)
                .build();
        return channel;
    }

    @Bean
    public FeatureServiceGrpc.FeatureServiceBlockingStub featureServiceBlockingStub(ManagedChannel managedChannel){
        return FeatureServiceGrpc.newBlockingStub(managedChannel);
    }


    @Bean
    public DetectionServiceGrpc.DetectionServiceBlockingStub detectionServiceBlockingStub(ManagedChannel managedChannel){
        return DetectionServiceGrpc.newBlockingStub(managedChannel);
    }
}
